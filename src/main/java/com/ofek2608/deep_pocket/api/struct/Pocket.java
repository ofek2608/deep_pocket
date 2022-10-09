package com.ofek2608.deep_pocket.api.struct;

import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public class Pocket {
	private final ItemConversions conversions;
	private final UUID pocketId;
	private final UUID owner;
	private PocketInfo pocketInfo;
	private final Map<ItemType,Long> items;
	private Snapshot lastSnapshot;

	public Pocket(ItemConversions conversions, UUID pocketId, UUID owner, PocketInfo pocketInfo) {
		this.conversions = conversions;
		this.pocketId = pocketId;
		this.owner = owner;
		this.pocketInfo = pocketInfo;
		this.items = new HashMap<>();
		this.lastSnapshot = new Snapshot();
	}

	public Pocket(Pocket copy) {
		this.conversions = copy.conversions;
		this.pocketId = copy.pocketId;
		this.owner = copy.owner;
		this.pocketInfo = copy.pocketInfo;
		this.items = new HashMap<>(copy.items);
		this.lastSnapshot = new Snapshot();
	}

	public Pocket(ItemConversions conversions, boolean allowPublicPocket, CompoundTag saved) {
		this.conversions = conversions;
		this.pocketId = saved.getUUID("pocketId");
		this.owner = saved.getUUID("owner");
		this.pocketInfo = new PocketInfo(saved.getCompound("info"));
		this.items = new HashMap<>();
		for (Tag itemCount : saved.getList("itemCounts", 10)) {
			ItemType type = ItemType.load(((CompoundTag) itemCount).getCompound("item"));
			long count = ((CompoundTag)itemCount).getLong("count");
			if (count <= 0)
				continue;
			items.put(type, count);
		}
		conversions.convertMap(items);
		this.lastSnapshot = new Snapshot();
		if (!allowPublicPocket && pocketInfo.securityMode == PocketSecurityMode.PUBLIC)
			pocketInfo.securityMode = PocketSecurityMode.TEAM;
	}

	public CompoundTag save() {
		CompoundTag saved = new CompoundTag();
		saved.putUUID("pocketId", pocketId);
		saved.putUUID("owner", owner);
		saved.put("info", pocketInfo.save());
		ListTag itemCounts = new ListTag();
		for (var entry : items.entrySet()) {
			CompoundTag itemCount = new CompoundTag();
			itemCount.put("item", entry.getKey().save());
			itemCount.putDouble("count", entry.getValue());
			itemCounts.add(itemCount);
		}
		saved.put("itemCounts", itemCounts);
		return saved;
	}

	public UUID getPocketId() { return pocketId; }
	public UUID getOwner() { return owner; }
	public PocketInfo getInfo() { return new PocketInfo(pocketInfo); }
	public String getName() { return pocketInfo.name; }
	public ItemType getIcon() { return pocketInfo.icon; }
	public int getColor() { return pocketInfo.color; }
	public PocketSecurityMode getSecurityMode() { return pocketInfo.securityMode; }

	public void setInfo(PocketInfo pocketInfo) {
		this.pocketInfo = new PocketInfo(pocketInfo);
		this.lastSnapshot.changedInfo = true;
	}


	public boolean canAccess(Player player) {
		return pocketInfo.securityMode.canAccess(player, getOwner());
	}

	public @UnmodifiableView Map<ItemType,Long> getItems() {
		return Collections.unmodifiableMap(items);
	}

	public long getItemCount(ItemType type) {
		return type.isEmpty() ? 0 : items.getOrDefault(type, 0L);
	}

	public void setItemCount(ItemType type, long count) {
		if (type.isEmpty())
			return;
		boolean changed;
		if (count == 0)
			changed = items.remove(type) != null;
		else
			changed = !Objects.equals(items.put(type, count < 0 ? -1 : count), count);
		if (changed)
			this.lastSnapshot.changedItems.add(type);
	}

	private void insertItem0(ItemType type, long count) {
		if (count < 0) {
			setItemCount(type, -1);
			return;
		}
		long currentCount = getItemCount(type);
		setItemCount(type, currentCount < 0 ? -1 : count + currentCount);
	}

	public void insertItem(ItemType type, long count) {
		if (type.isEmpty() || count == 0)
			return;
		long[] value = conversions.getValue(type, count);
		if (value == null) {
			insertItem0(type, count);
			return;
		}
		for (int i = 0; i < value.length; i++)
			if (value[i] != 0)
				insertItem0(conversions.getBaseItem(i), value[i]);
	}

	private long getMaxExtract0(Map<ItemType,Long> counts) {
		long min = -1;
		for (var entry : counts.entrySet()) {
			long v = entry.getValue();
			if (v == 0)
				continue;
			long existing = getItemCount(entry.getKey());
			if (existing < 0)
				continue;
			if (v < 0)
				return 0;//can't extract if you need infinity for each one
			if (existing == 0)
				return 0;
			long canMake = existing / v;
			if (canMake == 0)
				return 0;
			if (min < 0 || canMake < min)
				min = canMake;
		}
		return min;
	}

	public long getMaxExtract(Map<ItemType,Long> counts) {
		counts = new HashMap<>(counts);
		conversions.convertMap(counts);
		return getMaxExtract0(counts);
	}

	public long extract(Map<ItemType,Long> counts, long overallCount) {
		counts = new HashMap<>(counts);
		conversions.convertMap(counts);
		long maxExtract = getMaxExtract0(counts);
		if (0 <= maxExtract && maxExtract < overallCount)
			overallCount = maxExtract;
		for (var entry : counts.entrySet()) {
			long existing = getItemCount(entry.getKey());
			if (existing < 0)
				continue;
			long needed = DeepPocketUtils.advancedMul(entry.getValue(), overallCount);
			setItemCount(entry.getKey(), existing - needed);
		}
		return overallCount;
	}

	public long extractItem(ItemType type, long count) {
		return extract(Map.of(type, 1L), count);
	}

	public long getMaxExtract(ItemType ... items) {
		Map<ItemType,Long> itemsMap = new HashMap<>();
		for (ItemType item : items)
			itemsMap.put(item, itemsMap.getOrDefault(item, 0L) + 1);
		return getMaxExtract(itemsMap);
	}

	public void clearItems() {
		items.clear();
		this.lastSnapshot.clearedItems = true;
		this.lastSnapshot.changedItems.clear();
	}

	public Pocket copy() {
		return new Pocket(this);
	}

	public Snapshot createSnapshot() {
		return this.lastSnapshot = this.lastSnapshot.next = new Snapshot();
	}


	public final class Snapshot {
		private boolean changedInfo;
		private boolean clearedItems;
		private final Set<ItemType> changedItems = new HashSet<>();
		private Snapshot next;

		private Snapshot() {}

		public Pocket getPocket() {
			return Pocket.this;
		}

		public boolean didChangedInfo() {
			simplify();
			return changedInfo || next != null && next.changedInfo;
		}

		public boolean didClearedItems() {
			simplify();
			return clearedItems || next != null && next.clearedItems;
		}

		public Map<ItemType,Long> getChangedItems() {
			simplify();
			Map<ItemType,Long> result = new HashMap<>();
			if (next == null || !next.clearedItems)
				for (ItemType type : changedItems)
					result.put(type, Pocket.this.getItemCount(type));
			if (next != null)
				for (ItemType type : next.changedItems)
					result.put(type, Pocket.this.getItemCount(type));
			return result;
		}




		private void simplify() {
			if (next == null)
				return;
			while (next.next != null) {
				changedInfo = changedInfo || next.changedInfo;
				if (next.clearedItems) {
					clearedItems = true;
					changedItems.clear();
				}
				changedItems.addAll(next.changedItems);
				next = next.next;
			}
		}
	}
}
