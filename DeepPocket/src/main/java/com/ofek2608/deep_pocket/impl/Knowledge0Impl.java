package com.ofek2608.deep_pocket.impl;

import com.ofek2608.collections.CaptureSet;
import com.ofek2608.deep_pocket.api.Knowledge0;
import com.ofek2608.deep_pocket.api.struct.ElementConversions;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.ItemType;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

final class Knowledge0Impl implements Knowledge0 {
	private final ElementSet elements;

	Knowledge0Impl(ElementConversions conversions) {
		this.elements = new ElementSet(conversions);
	}

	private Knowledge0Impl(Knowledge0Impl copy) {
		this.elements = new ElementSet(copy.elements.conversions);
		this.elements.addAll(copy.elements);
	}

	@Override public boolean contains(ElementType type) { return elements.contains(type) || !elements.conversions.hasValue(type); }
	@Override public void add(ElementType ... types) { elements.addAll(Arrays.asList(types)); }
	@Override public void remove(ElementType ... types) { Arrays.asList(types).forEach(elements::remove); }
	@Override public Set<ElementType> asSet() { return elements; }
	@Override public Snapshot createSnapshot() { return new SnapshotImpl(); }
	@Override public Knowledge0 copy() { return new Knowledge0Impl(this); }

	private final class SnapshotImpl implements Snapshot {
		private final CaptureSet<ElementType>.Snapshot internal = elements.createSnapshot();

		@Override
		public Knowledge0Impl getKnowledge() {
			return Knowledge0Impl.this;
		}

		@Override
		public ElementType[] getRemoved() {
			return internal.getRemoved().toArray(new ElementType[0]);
		}

		@Override
		public ElementType[] getAdded() {
			return internal.getAdded().toArray(new ElementType[0]);
		}
	}









	private static final class ElementSet extends CaptureSet<ElementType> {
		private final ElementConversions conversions;

		private ElementSet(ElementConversions conversions) {
			this.conversions = conversions;
		}

		@Override
		public void validateElement(ElementType type) throws IllegalArgumentException {
			Objects.requireNonNull(type);
			if (!this.conversions.hasValue(type))
				throw new IllegalArgumentException();
		}
	}
}
