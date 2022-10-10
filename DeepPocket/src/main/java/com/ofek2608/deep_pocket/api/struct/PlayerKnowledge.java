package com.ofek2608.deep_pocket.api.struct;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public final class PlayerKnowledge {
	private final ItemConversions conversions;
	private final UUID player;
	private final Set<ItemType> items;
	private Snapshot lastSnapshot;

	public PlayerKnowledge(ItemConversions conversions, UUID player) {
		this.conversions = conversions;
		this.player = player;
		this.items = new HashSet<>();
		this.lastSnapshot = new Snapshot();
	}

	public PlayerKnowledge(PlayerKnowledge copy) {
		this.conversions = copy.conversions;
		this.player = copy.player;
		this.items = new HashSet<>(copy.items);
		this.lastSnapshot = new Snapshot();
	}

	public PlayerKnowledge(ItemConversions conversions, CompoundTag saved) {
		this.conversions = conversions;
		this.player = saved.getUUID("player");
		this.items = new HashSet<>();
		for (Tag itemTag : saved.getList("items", 10)) {
			ItemType item = ItemType.load((CompoundTag) itemTag);
			if (conversions.getValue(item) != null)
				items.add(item);
		}
		this.lastSnapshot = new Snapshot();
	}

	public CompoundTag save() {
		CompoundTag saved = new CompoundTag();
		saved.putUUID("player", player == null ? net.minecraft.Util.NIL_UUID : player);
		ListTag items = new ListTag();
		for (ItemType item : this.items)
			items.add(item.save());
		saved.put("items", items);
		return saved;
	}

	public UUID getPlayer() {
		return player;
	}

	public boolean contains(ItemType type) {
		return items.contains(type) || conversions.getValue(type) == null;
	}

	public Stream<ItemType> getPocketItems(Pocket pocket) {
		return Stream.concat(pocket.getItems().keySet().stream(), items.stream());
	}

	public void add(ItemType ... types) {
		for (ItemType type : types) {
			if (conversions.getValue(type) == null)
				continue;
			if (items.add(type)) {
				lastSnapshot.removed.remove(type);
				lastSnapshot.added.add(type);
			}
		}
	}

	public void remove(ItemType ... types) {
		for (ItemType type : types) {
			if (conversions.getValue(type) == null)
				continue;
			if (items.remove(type)) {
				lastSnapshot.removed.add(type);
				lastSnapshot.added.remove(type);
			}
		}
	}

	public void clear() {
		items.clear();
		lastSnapshot.cleared = true;
		lastSnapshot.removed.clear();
		lastSnapshot.added.clear();
	}

	public @UnmodifiableView Set<ItemType> asSet() {
		return Collections.unmodifiableSet(items);
	}


	public Snapshot createSnapshot() {
		return this.lastSnapshot = this.lastSnapshot.next = new Snapshot();
	}





	public final class Snapshot {
		private boolean cleared;
		private final Set<ItemType> removed = new HashSet<>();
		private final Set<ItemType> added = new HashSet<>();
		private Snapshot next;

		private Snapshot() {}

		public PlayerKnowledge getKnowledge() {
			return PlayerKnowledge.this;
		}

		public boolean didClear() {
			simplify();
			return cleared || next != null && next.cleared;
		}

		public ItemType[] getRemoved() {
			return removed.toArray(new ItemType[0]);
		}

		public ItemType[] getAdded() {
			return added.toArray(new ItemType[0]);
		}

		private void simplify() {
			if (next == null)
				return;
			while (next.next != null) {
				if (next.cleared) {
					cleared = true;
					removed.clear();
					added.clear();
				}
				removed.addAll(next.removed);
				added.addAll(next.added);
				next = next.next;
			}
		}
	}
}
