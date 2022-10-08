package com.ofek2608.deep_pocket.api.struct;

import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public class Pocket {
	private final UUID pocketId;
	private final UUID owner;
	private PocketInfo pocketInfo;
	private final Map<ItemType,Double> items;
	private Snapshot lastSnapshot;

	public Pocket(UUID pocketId, UUID owner, PocketInfo pocketInfo) {
		this.pocketId = pocketId;
		this.owner = owner;
		this.pocketInfo = pocketInfo;
		this.items = new HashMap<>();
		this.lastSnapshot = new Snapshot();
	}

	public Pocket(Pocket copy) {
		this.pocketId = copy.pocketId;
		this.owner = copy.owner;
		this.pocketInfo = copy.pocketInfo;
		this.items = new HashMap<>(copy.items);
		this.lastSnapshot = new Snapshot();
	}

	public Pocket(CompoundTag saved, boolean allowPublicPocket) {
		this.pocketId = saved.getUUID("pocketId");
		this.owner = saved.getUUID("owner");
		this.pocketInfo = new PocketInfo(saved.getCompound("info"));
		this.items = new HashMap<>();
		for (Tag itemCount : saved.getList("itemCounts", 10)) {
			ItemType type = ItemType.load(((CompoundTag) itemCount).getCompound("item"));
			double count = ((CompoundTag)itemCount).getDouble("count");
			if (count <= 0)
				continue;
			items.put(type, count);
		}
		this.lastSnapshot = new Snapshot();
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

	public double getCount(ItemType type) {
		return type.isEmpty() ? 0 : items.getOrDefault(type, 0.0);
	}

	public void setCount(ItemType type, double value) {
		if (type.isEmpty())
			return;
		if (value < 0)
			throw new IllegalArgumentException("value");
		boolean changed;
		if (value == 0)
			changed = items.remove(type) != null;
		else
			changed = !Objects.equals(items.put(type, value), value);
		if (changed)
			this.lastSnapshot.changedItems.add(type);
	}

	public void addCount(ItemType type, double value) {
		setCount(type, getCount(type) + value);
	}

	public @UnmodifiableView Map<ItemType, Double> getItems() {
		return Collections.unmodifiableMap(items);
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

	//TODO change to longs
	//	long getItemCount(ItemType type);
	//	void setItemCount(ItemType type, long count);
	//	void insertItem(ItemType type, long count);
	//	long extractItem(ItemType type, long count);
	//	long getMaxExtract(ItemType ... items);
	//	void clearItems();

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

		public Map<ItemType,Double> getChangedItems() {
			simplify();
			Map<ItemType,Double> result = new HashMap<>();
			if (next == null || !next.clearedItems)
				for (ItemType type : changedItems)
					result.put(type, Pocket.this.getCount(type));
			if (next != null)
				for (ItemType type : next.changedItems)
					result.put(type, Pocket.this.getCount(type));
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
