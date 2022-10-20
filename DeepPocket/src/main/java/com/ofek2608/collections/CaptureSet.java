package com.ofek2608.collections;

import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

@SuppressWarnings({"SuspiciousMethodCalls", "SuspiciousToArrayCall"})
public class CaptureSet<E> extends AbstractSet<E> {
	private final CaptureSetMap internal;

	public CaptureSet() {
		this.internal = new CaptureSetMap();
	}

	public CaptureSet(@NotNull @Flow(sourceIsContainer = true, targetIsContainer = true) Collection<? extends E> c) {
		this.internal = new CaptureSetMap(Math.max((int) (c.size()/.75f) + 1, 16));
		addAll(c);
	}

	public CaptureSet(@Range(from = 0, to = Integer.MAX_VALUE) int initialCapacity, float loadFactor) {
		this.internal = new CaptureSetMap(initialCapacity, loadFactor);
	}

	public CaptureSet(@Range(from = 0, to = Integer.MAX_VALUE) int initialCapacity) {
		this.internal = new CaptureSetMap(initialCapacity);
	}

	@Override public int size() { return internal.size(); }
	@Override public boolean isEmpty() { return internal.isEmpty(); }
	@Override public boolean contains(Object o) { return internal.containsKey(o); }
	@Override public @NotNull Iterator<E> iterator() { return internal.keySet().iterator(); }
	@Override public Object @NotNull [] toArray() { return internal.keySet().toArray(); }
	@Override public <T> T @NotNull [] toArray(T @NotNull [] a) { return internal.keySet().toArray(a); }
	@Override public boolean add(E e) { return internal.putIfCan(e, e); }
	@Override public boolean remove(Object o) { return internal.removeIfCan(o); }
	@Override public void clear() { internal.clear(); }

	public Snapshot createSnapshot() {
		return new Snapshot(internal.createSnapshot());
	}



	//For use in an extending class
	public void validateElement(E e) throws IllegalArgumentException {}

	public class Snapshot {
		private final CaptureMap<E,E>.Snapshot internal;

		private Snapshot(CaptureMap<E, E>.Snapshot internal) {
			this.internal = internal;
		}

		public @UnmodifiableView Set<E> getAdded() {
			return internal.getAddedKeys();
		}

		public @UnmodifiableView Set<E> getRemoved() {
			return internal.getRemovedKeys();
		}
	}

	private class CaptureSetMap extends CaptureMap<E,E> {
		public CaptureSetMap(@Range(from = 0, to = Integer.MAX_VALUE) int initialCapacity, float loadFactor) {
			super(initialCapacity, loadFactor);
		}

		public CaptureSetMap(@Range(from = 0, to = Integer.MAX_VALUE) int initialCapacity) {
			super(initialCapacity);
		}

		public CaptureSetMap() { }

		public void validateKey(E e) { validateElement(e); }
	}
}
