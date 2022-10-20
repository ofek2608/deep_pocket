package com.ofek2608.deep_pocket.impl;

import com.ofek2608.collections.CaptureSet;
import com.ofek2608.deep_pocket.api.Knowledge;
import com.ofek2608.deep_pocket.api.struct.ItemConversions;
import com.ofek2608.deep_pocket.api.struct.ItemType;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

final class KnowledgeImpl implements Knowledge {
	private final ItemSet items;

	KnowledgeImpl(ItemConversions conversions) {
		this.items = new ItemSet(conversions);
	}

	private KnowledgeImpl(KnowledgeImpl copy) {
		this.items = new ItemSet(copy.items.conversions);
		this.items.addAll(copy.items);
	}

	@Override public boolean contains(ItemType type) { return items.contains(type) || !items.conversions.hasValue(type); }
	@Override public void add(ItemType ... types) { items.addAll(Arrays.asList(types)); }
	@Override public void remove(ItemType ... types) { Arrays.asList(types).forEach(items::remove); }
	@Override public Set<ItemType> asSet() { return items; }
	@Override public Snapshot createSnapshot() { return new SnapshotImpl(); }
	@Override public Knowledge copy() { return new KnowledgeImpl(this); }

	private final class SnapshotImpl implements Snapshot {
		private final CaptureSet<ItemType>.Snapshot internal = items.createSnapshot();

		@Override
		public KnowledgeImpl getKnowledge() {
			return KnowledgeImpl.this;
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
