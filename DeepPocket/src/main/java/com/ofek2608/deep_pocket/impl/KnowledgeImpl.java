package com.ofek2608.deep_pocket.impl;

import com.ofek2608.collections.CaptureSet;
import com.ofek2608.deep_pocket.api.Knowledge;
import com.ofek2608.deep_pocket.api.struct.ElementConversions;

import java.util.Objects;
import java.util.Set;

final class KnowledgeImpl implements Knowledge {
	private final ElementSet elements;

	KnowledgeImpl(ElementConversions conversions) {
		this.elements = new ElementSet(conversions);
	}

	private KnowledgeImpl(KnowledgeImpl copy) {
		this.elements = new ElementSet(copy.elements.conversions);
		this.elements.addAll(copy.elements);
	}

	@Override public boolean contains(int type) { return elements.contains(type) || !elements.conversions.hasValue(type); }
	
	@Override
	public void add(int ... types) {
		for (int type : types) {
			if (elements.conversions.hasValue(type)) {
				elements.add(type);
			}
		}
	}
	
	@Override
	public void remove(int ... types) {
		for (int type : types) {
			elements.remove(type);
		}
	}
	
	@Override public Set<Integer> asSet() { return elements; }
	@Override public Snapshot createSnapshot() { return new SnapshotImpl(); }
	@Override public Knowledge copy() { return new KnowledgeImpl(this); }

	private final class SnapshotImpl implements Snapshot {
		private final CaptureSet<Integer>.Snapshot internal = elements.createSnapshot();

		@Override
		public KnowledgeImpl getKnowledge() {
			return KnowledgeImpl.this;
		}

		@Override
		public int[] getRemoved() {
			return internal.getRemoved().stream().mapToInt(Integer::intValue).toArray();
		}

		@Override
		public int[] getAdded() {
			return internal.getAdded().stream().mapToInt(Integer::intValue).toArray();
		}
	}









	private static final class ElementSet extends CaptureSet<Integer> {
		private final ElementConversions conversions;

		private ElementSet(ElementConversions conversions) {
			this.conversions = conversions;
		}

		@Override
		public void validateElement(Integer type) throws IllegalArgumentException {
			Objects.requireNonNull(type);
			if (!this.conversions.hasValue(type))
				throw new IllegalArgumentException();
		}
	}
}
