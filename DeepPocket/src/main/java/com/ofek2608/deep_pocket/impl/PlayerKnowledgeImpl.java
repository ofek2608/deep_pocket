package com.ofek2608.deep_pocket.impl;

import com.ofek2608.collections.CaptureSet;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.struct.ItemConversions;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.PlayerKnowledge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

final class PlayerKnowledgeImpl implements PlayerKnowledge {
	private final ItemSet items;

	PlayerKnowledgeImpl(ItemConversions conversions) {
		this.items = new ItemSet(conversions);
	}

	private PlayerKnowledgeImpl(PlayerKnowledgeImpl copy) {
		this.items = new ItemSet(copy.items.conversions);
		this.items.addAll(copy.items);
	}

	public PlayerKnowledgeImpl(ItemConversions conversions, CompoundTag saved) {
		this.items = new ItemSet(conversions);
		for (Tag itemTag : saved.getList("items", 10)) {
			ItemType item = ItemType.load((CompoundTag) itemTag);
			if (conversions.getValue(item) != null)
				items.add(item);
		}
	}

	@Override
	public CompoundTag save() {
		CompoundTag saved = new CompoundTag();
		ListTag items = new ListTag();
		for (ItemType item : this.items)
			items.add(item.save());
		saved.put("items", items);
		return saved;
	}

	@Override public boolean contains(ItemType type) { return items.contains(type) || !items.conversions.hasValue(type); }
	@Override public Stream<ItemType> getPocketItems(Pocket pocket) { return Stream.concat(pocket.getItemsMap().keySet().stream(), items.stream()); }
	@Override public void add(ItemType ... types) { items.addAll(Arrays.asList(types)); }
	@Override public void remove(ItemType ... types) { Arrays.asList(types).forEach(items::remove); }
	@Override public void clear() { items.clear(); }
	@Override public @UnmodifiableView Set<ItemType> asSet() { return Collections.unmodifiableSet(items); }
	@Override public Snapshot createSnapshot() { return new SnapshotImpl(); }
	@Override public PlayerKnowledge copy() { return new PlayerKnowledgeImpl(this); }

	private final class SnapshotImpl implements Snapshot {
		private final CaptureSet<ItemType>.Snapshot internal = items.createSnapshot();

		@Override
		public PlayerKnowledgeImpl getKnowledge() {
			return PlayerKnowledgeImpl.this;
		}

		@Override
		public ItemType[] getRemoved() {
			return internal.getRemoved().toArray(new ItemType[0]);
		}

		@Override
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
