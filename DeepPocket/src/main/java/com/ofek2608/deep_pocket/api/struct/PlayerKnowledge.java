package com.ofek2608.deep_pocket.api.struct;

import com.ofek2608.collections.CaptureSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.stream.Stream;

public final class PlayerKnowledge {
	private final UUID player;
	private final ItemSet items;

	public PlayerKnowledge(ItemConversions conversions, UUID player) {
		this.player = player;
		this.items = new ItemSet(conversions);
	}

	public PlayerKnowledge(PlayerKnowledge copy) {
		this.player = copy.player;
		this.items = new ItemSet(copy.items.conversions);
		this.items.addAll(copy.items);
	}

	public PlayerKnowledge(ItemConversions conversions, CompoundTag saved) {
		this.player = saved.getUUID("player");
		this.items = new ItemSet(conversions);
		for (Tag itemTag : saved.getList("items", 10)) {
			ItemType item = ItemType.load((CompoundTag) itemTag);
			if (conversions.getValue(item) != null)
				items.add(item);
		}
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
		return items.contains(type) || !items.conversions.hasValue(type);
	}

	public Stream<ItemType> getPocketItems(Pocket pocket) {
		return Stream.concat(pocket.getItems().keySet().stream(), items.stream());
	}

	public void add(ItemType ... types) {
		items.addAll(Arrays.asList(types));
	}

	public void remove(ItemType ... types) {
		Arrays.asList(types).forEach(items::remove);
	}

	public void clear() {
		items.clear();
	}

	public @UnmodifiableView Set<ItemType> asSet() {
		return Collections.unmodifiableSet(items);
	}


	public Snapshot createSnapshot() {
		return new Snapshot();
	}





	public final class Snapshot {
		private final CaptureSet<ItemType>.Snapshot internal = items.createSnapshot();

		private Snapshot() {}

		public PlayerKnowledge getKnowledge() {
			return PlayerKnowledge.this;
		}

		public ItemType[] getRemoved() {
			return internal.getRemoved().toArray(new ItemType[0]);
		}

		public ItemType[] getAdded() {
			return internal.getAdded().toArray(new ItemType[0]);
		}
	}









	private static final class ItemSet extends CaptureSet<ItemType> {
		private final ItemConversions conversions;

		private ItemSet(ItemConversions conversions) {
			this.conversions = conversions;
		}

		@Override
		public void validateElement(ItemType type) throws IllegalArgumentException {
			Objects.requireNonNull(type);
			if (!this.conversions.hasValue(type))
				throw new IllegalArgumentException();
		}
	}
}
