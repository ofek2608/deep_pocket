package com.ofek2608.deep_pocket.api.struct;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public final class ItemConversions {
	public static final ItemConversions EMPTY = new ItemConversions(new ItemType[0], Collections.emptyMap());
	private final ItemType[] baseItems;
	private final Map<ItemType,long[]> values;

	private ItemConversions(ItemType[] baseItems, Map<ItemType,long[]> values) {
		this.baseItems = baseItems;
		this.values = values;
	}


	public static void encode(FriendlyByteBuf buf, ItemConversions conversions) {buf.writeVarInt(conversions.baseItems.length);
		for (ItemType baseItem : conversions.baseItems)
			ItemType.encode(buf, baseItem);
		for (var entry : conversions.values.entrySet()) {
			ItemType.encode(buf, entry.getKey());
			for (long baseItemCost : entry.getValue())
				buf.writeVarLong(baseItemCost);
		}
		ItemType.encode(buf, ItemType.EMPTY);
	}

	public static ItemConversions decode(FriendlyByteBuf buf) {
		int baseItemCount = buf.readVarInt();
		ItemType[] baseItems = new ItemType[baseItemCount];
		for (int i = 0; i < baseItemCount; i++) {
			baseItems[i] = ItemType.decode(buf);
			if (baseItems[i].isEmpty())
				throw new IllegalArgumentException("ItemConversions-BaseItem is empty.");
		}

		Map<ItemType,long[]> values = new HashMap<>();
		ItemType readItemType;
		while (!(readItemType = ItemType.decode(buf)).isEmpty()) {
			if (List.of(baseItems).contains(readItemType))
				throw new IllegalArgumentException("ItemConversions-BaseItem has a value.");
			long[] value = new long[baseItemCount];
			for (int i = 0; i < baseItemCount; i++)
				value[i] = buf.readVarLong();
			values.put(readItemType, value);
		}
		return new ItemConversions(baseItems, values);
	}

	public ItemType[] getBaseItems() {
		return Arrays.copyOf(baseItems, baseItems.length);
	}

	public static final class Builder {
		public final Map<ItemType,ItemValueBuilder> values = new HashMap<>();

		public ItemValueBuilder item(ItemType item) {
			return values.computeIfAbsent(item, p->new ItemValueBuilder());
		}

		public ItemValueBuilder item(ItemStack item) { return item(new ItemType(item)); }
		public ItemValueBuilder item(Item item) { return item(new ItemType(item)); }

		public ItemConversions build() {
			//coping so there won't be any async stuff changing it while building (ensuring the class will never corrupt)
			Map<ItemType,Map<ItemType,Long>> valuesCopy = new HashMap<>();
			for (var entry0 : values.entrySet()) {
				if (entry0.getKey().isEmpty())
					continue;
				Map<ItemType,Long> valueCopy = new HashMap<>();
				for (var entry1 : entry0.getValue().amounts.entrySet()) {
					if (entry1.getKey().isEmpty())
						continue;
					valueCopy.put(entry1.getKey(), entry1.getValue());
				}
				if (valueCopy.size() > 0)
					valuesCopy.put(entry0.getKey(), valueCopy);
			}
			return build(valuesCopy);
		}



		private static ItemConversions build(Map<ItemType,Map<ItemType,Long>> requirementMap) {
			Map<ItemType,List<ItemType>> requiredByMap = getRequiredByMap(requirementMap);
			Map<ItemType,Integer> requirementCountMap = getRequirementCountMap(requirementMap);
			ItemType[] baseItems = findBaseItems(requirementCountMap);
			List<ItemType> toSolveList = new ArrayList<>(Arrays.asList(baseItems));

			Map<ItemType,long[]> values = new HashMap<>();


			for (int i = 0; i < toSolveList.size(); i++) {
				ItemType solving = toSolveList.get(i);
				values.put(solving, i < baseItems.length ? createUnitVector(baseItems.length, i) : buildValue(baseItems.length, values, requirementMap.get(solving)));
				for (ItemType requiredBy : requiredByMap.get(toSolveList.get(i))) {
					int requiredByRequirementCount = requirementCountMap.get(requiredBy) - 1;
					requirementCountMap.put(requiredBy, requiredByRequirementCount);
					//If all requirement are done, now we can solve requiredBy
					if (requiredByRequirementCount == 0)
						toSolveList.add(requiredBy);
				}
			}


			//Removing all the base items from the values
			for (ItemType baseItem : baseItems)
				values.remove(baseItem);

			return new ItemConversions(baseItems, values);
		}

		private static Map<ItemType,List<ItemType>> getRequiredByMap(Map<ItemType,Map<ItemType,Long>> requirementMap) {
			Map<ItemType,List<ItemType>> requiredByMap = new HashMap<>();
			requirementMap.forEach((type,value)->{
				for (ItemType requirement : value.keySet())
					requiredByMap.computeIfAbsent(requirement, p->new ArrayList<>()).add(type);
			});
			return requiredByMap;
		}

		private static Map<ItemType,Integer> getRequirementCountMap(Map<ItemType,Map<ItemType,Long>> requirementMap) {
			Map<ItemType,Integer> requirementCountMap = new HashMap<>();
			requirementMap.forEach((type,value) -> requirementCountMap.put(type, value.size()));
			return requirementCountMap;
		}

		private static ItemType[] findBaseItems(Map<ItemType,Integer> requirementCountMap) {
			return requirementCountMap.entrySet().stream().filter(entry->entry.getValue() > 0).map(Map.Entry::getKey).unordered().distinct().toArray(ItemType[]::new);
		}

		private static long[] createUnitVector(int length, int index) {
			long[] vec = new long[length];
			vec[index] = 1;
			return vec;
		}

		private static long[] buildValue(int baseItemCount, Map<ItemType,long[]> values, Map<ItemType,Long> requirements) {
			long[] result = new long[baseItemCount];
			requirements.forEach((requiredItem,requiredCount) -> {
				long[] requiredValue = values.get(requiredItem);
				for (int i = 0; i < baseItemCount; i++)
					result[i] = sumMultiplied(result[i], requiredCount, requiredValue[i]);
			});
			//Cleanup
			for (int i = 0; i < baseItemCount; i++)
				if (result[i] < 0)
					result[i] = -1;
			return result;
		}

		/**
		 * @return a + b * c
		 */
		private static long sumMultiplied(long a, long b, long c) {
			if (b == 0 || c == 0) return a;
			if (a < 0 || b < 0 || c < 0) return -1;
			try {
				return a + Math.multiplyExact(b, c);//sum overflow will be negative
			} catch (Exception e) {
				return -1;
			}
		}
	}

	public static final class ItemValueBuilder {
		public final Map<ItemType,Long> amounts = new HashMap<>();

		public ItemValueBuilder set(ItemType type, long amount) {
			amounts.put(type, amount);
			return this;
		}

		public ItemValueBuilder add(ItemType type, long amount) {
			long oldAmount = amounts.getOrDefault(type, 0L);
			long newAmount = amount < 0 || oldAmount < 0 ? -1 : amount + oldAmount;//Overflow will be negative
			return set(type, newAmount);
		}

		public ItemValueBuilder set(ItemStack item, long amount) { return set(new ItemType(item), amount); }
		public ItemValueBuilder set(Item item, long amount) { return set(new ItemType(item), amount); }
		public ItemValueBuilder add(ItemStack item, long amount) { return add(new ItemType(item), amount); }
		public ItemValueBuilder add(Item item, long amount) { return add(new ItemType(item), amount); }
	}
}
