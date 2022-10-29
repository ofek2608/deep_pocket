package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.ProvidedResources;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

final class ProvidedResourcesImpl implements ProvidedResources {
	private final @Nullable ProvidedResourcesImpl parent;
	private final ItemType[] types;
	private final long[] provided;
	private final int[] indexes;

	ProvidedResourcesImpl(ItemType[] types) {
		this(null, types.clone(), new int[types.length]);
		for (ItemType type : types)
			if (type.isEmpty())
				throw new IllegalArgumentException("types");
	}

	private ProvidedResourcesImpl(@Nullable ProvidedResourcesImpl parent, ItemType[] types, int[] indexes) {
		this.parent = parent;
		this.types = types;
		this.provided = new long[types.length];
		this.indexes = indexes;
	}

	@Override
	public ItemType[] getTypes() {
		return types.clone();
	}

	@Override
	public int getTypeCount() {
		return types.length;
	}

	@Override
	public ItemType getType(int index) {
		return index < 0 || types.length <= index ? ItemType.EMPTY : types[index];
	}

	@Override
	public long getProvided(int index) {
		return index < 0 || provided.length <= index ? 0 : provided[index];
	}

	@Override
	public void provide(int index, long amount) {
		if (index < 0 || types.length <= index)
			return;
		provided[index] = DeepPocketUtils.advancedSum(provided[index], amount);
	}

	@Override
	public long take(int index, long amount) {
		if (index < 0 || types.length <= index)
			return 0;
		long current = provided[index];
		if (current < 0)
			return amount < 0 ? -1 : amount;
		if (0 <= amount && amount <= current) {
			provided[index] = current - amount;
			return amount;
		}
		provided[index] = 0;
		return current;
	}

	@Override
	public long getMaxRequestFromParents(int index) {
		if (index < 0 || types.length <= index)
			return 0;
		long amount = 0;
		ProvidedResourcesImpl parent = this.parent;
		while (parent != null) {
			index = this.indexes[index];
			amount = DeepPocketUtils.advancedSum(amount, parent.getProvided(index));
			parent = parent.parent;
		}
		return amount;
	}

	@Override
	public long requestFromParent(int index, long amount) {
		if (index < 0 || types.length <= index)
			return 0;
		long totalTaken = 0;
		ProvidedResourcesImpl parent = this.parent;
		while (parent != null) {
			index = this.indexes[index];
			long taken = parent.take(index, amount);
			totalTaken = DeepPocketUtils.advancedSum(totalTaken, taken);
			amount = taken < 0 ? 0 : amount < 0 ? -1 : amount - taken;
			parent = parent.parent;
		}
		return totalTaken;
	}

	private long getMaxRequestMultiplier(long[] amounts, long maxMultiplier) {
		if (maxMultiplier < 0)
			maxMultiplier = -1;
		for (int i = 0; i < types.length; i++) {
			long maxRequest = getMaxRequestFromParents(i);
			if (maxRequest < 0)
				continue;
			if (amounts[i] < 0)
				return 0;
			if (amounts[i] == 0)
				continue;
			long limit = maxRequest / amounts[i];
			if (maxMultiplier < 0 || limit < maxMultiplier)
				maxMultiplier = maxRequest;
		}
		return maxMultiplier;
	}

	@Override
	public long requestFromParent(long[] amounts, long maxMultiplier) {
		maxMultiplier = getMaxRequestMultiplier(amounts, maxMultiplier);
		if (maxMultiplier == 0)
			return 0;

		for (int i = 0; i < types.length; i++) {
			if (amounts[i] == 0)
				continue;
			long amount = amounts[i] < 0 || maxMultiplier < 0 ? -1 : amounts[i] * maxMultiplier;
			requestFromParent(i, amount);
			provided[i] = DeepPocketUtils.advancedSum(provided[i], amount);
		}
		return maxMultiplier;
	}

	@Override
	public void returnToParent(int index, long amount) {
		if (parent == null || index < 0 || types.length <= index)
			return;
		long current = provided[index];
		if (current < 0) {
			parent.provide(index, amount);
			return;
		}
		if (amount < 0 || current <= amount)
			amount = current;
		parent.provide(index, amount);
		provided[index] = current - amount;
	}

	@Override
	public void returnAllToParent() {
		for (int i = 0; i < types.length; i++)
			returnToParent(i, -1L);
	}

	@Override
	public ProvidedResources subProvidedResources(int[] indexes) {
		indexes = indexes.clone();

		int len = indexes.length;
		ItemType[] types = new ItemType[len];
		for (int i = 0; i < len; i++)
			types[i] = this.types[indexes[i]];

		return new ProvidedResourcesImpl(this, types, indexes);
	}

	@Override
	public ProvidedResources subProvidedResources() {
		int len = types.length;
		int[] indexes = new int[len];
		for (int i = 0; i < len; i++)
			indexes[i] = i;

		return new ProvidedResourcesImpl(this, types, indexes);
	}

	@Override
	public void load(Tag saved) {
		if (!(saved instanceof ListTag savedList))
			return;
		for (Tag savedElement : savedList) {
			if (!(savedElement instanceof CompoundTag savedElementCompound))
				continue;
			ItemTypeAmount typeAmount = new ItemTypeAmount(savedElementCompound);
			ItemType type = typeAmount.getItemType();
			for (int i = 0; i < types.length; i++) {
				if (types[i].equals(type)) {
					provide(i, typeAmount.getAmount());
					break;
				}
			}
		}
	}

	@Override
	public Tag save() {
		ListTag saved = new ListTag();
		for (int i = 0; i < types.length; i++)
			saved.add(new ItemTypeAmount(types[i], provided[i]).save());
		return saved;
	}
}
