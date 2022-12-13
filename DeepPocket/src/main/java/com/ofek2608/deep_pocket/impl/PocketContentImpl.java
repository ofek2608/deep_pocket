package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.PocketContent;
import com.ofek2608.deep_pocket.api.struct.ElementConversions;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public final class PocketContentImpl implements PocketContent {
	private ElementConversions conversions = ElementConversions.EMPTY;
	private final Map<ElementType,Integer> indexMap = new HashMap<>();
	private final List<ElementType> types = new ArrayList<>();
	private final List<Long> counts = new ArrayList<>();
	private SnapshotImpl lastSnapshot = new SnapshotImpl();
	
	@Override
	public ElementConversions getConversions() {
		return conversions;
	}
	
	@Override
	public void setConversions(ElementConversions conversions) {
		this.conversions = conversions;
	}
	
	@Override
	public int getIndex(ElementType type) {
		return indexMap.getOrDefault(type, -1);
	}
	
	@Override
	public ElementType getType(int index) {
		return 0 <= index && index < types.size() ? types.get(index) : ElementType.empty();
	}
	
	@Override
	public long getCount(int index) {
		return 0 <= index && index < counts.size() ? counts.get(index) : 0L;
	}
	
	@Override
	public long getCount(ElementType type) {
		return getCount(getIndex(type));
	}
	
	@Override
	public void setCount(int index, long count) {
		// canonicalize count
		if (count < 0)
			count = -1;
		// abort if there is no difference
		if (counts.get(index) == count)
			return;
		if (count == 0) {
			int lastIndex = types.size() - 1;
			ElementType lastType = types.remove(lastIndex);
			long lastCount = counts.remove(lastIndex);
			if (index < lastIndex) {
				ElementType removedType = types.set(index, lastType);
				counts.set(index, lastCount);
				indexMap.put(lastType, indexMap.remove(removedType));
				lastSnapshot.changedCounts.add(lastIndex);
				lastSnapshot.changedTypes.add(lastIndex);
			} else {
				indexMap.remove(lastType);
			}
			lastSnapshot.changedCounts.add(index);
			lastSnapshot.changedTypes.add(index);
		} else {
			counts.set(index, count);
			lastSnapshot.changedCounts.add(index);
		}
	}
	
	@Override
	public void setCount(ElementType type, long count) {
		int index = getIndex(type);
		if (index >= 0) {
			setCount(index, count);
			return;
		}
		if (count == 0)
			return;
		if (conversions.hasValue(type))
			throw new IllegalArgumentException("Tried to insert element type with conversion value: " + type);
		index = types.size();
		types.add(type);
		counts.add(count);
		indexMap.put(type, index);
		lastSnapshot.changedTypes.add(index);
		lastSnapshot.changedCounts.add(index);
	}
	
	@Override
	public void remove(int index) {
		if (index < 0 || types.size() <= index)
			return;
		setCount(index, 0);
	}
	
	@Override
	public void remove(ElementType type) {
		remove(getIndex(type));
	}
	
	@Override
	public void clear() {
		for (int index = 0; index < types.size(); index++) {
			lastSnapshot.changedTypes.add(index);
			lastSnapshot.changedCounts.add(index);
		}
		indexMap.clear();
		types.clear();
		counts.clear();
	}
	
	@Override
	public int getSize() {
		return types.size();
	}
	
	@Override
	public Snapshot createSnapshot() {
		return lastSnapshot = lastSnapshot.nextSnapshot = new SnapshotImpl();
	}
	
	@Override
	public PocketContent copy() {
		PocketContentImpl copy = new PocketContentImpl();
		copy.conversions = conversions;
		copy.indexMap.putAll(indexMap);
		copy.types.addAll(types);
		copy.counts.addAll(counts);
		return copy;
	}
	
	@Override
	public void setType(int index, ElementType type) {
		if (type.isEmpty())
			throw new IllegalArgumentException("Tried to insert empty element");
		if (conversions.hasValue(type))
			throw new IllegalArgumentException("Tried to insert element type with conversion value: " + type);
		
		if (index < 0 || index > types.size())
			throw new IndexOutOfBoundsException();
		indexMap.put(type, index);
		if (index == types.size()) {
			types.add(type);
			counts.add(0L);
			lastSnapshot.changedTypes.add(index);
			lastSnapshot.changedCounts.add(index);
		} else {
			types.set(index, type);
			lastSnapshot.changedTypes.add(index);
		}
	}
	
	private static final class SnapshotImpl implements Snapshot {
		private final Set<Integer> changedTypes = new HashSet<>();
		private final Set<Integer> changedCounts = new HashSet<>();
		private SnapshotImpl nextSnapshot;
		
		private void simplify() {
			if (nextSnapshot == null)
				return;
			while (nextSnapshot.nextSnapshot != null) {
				changedTypes.addAll(nextSnapshot.changedTypes);
				changedCounts.addAll(nextSnapshot.changedCounts);
				nextSnapshot = nextSnapshot.nextSnapshot;
			}
		}
		
		@Override
		public @UnmodifiableView Set<Integer> getChangedTypes() {
			simplify();
			return Collections.unmodifiableSet(changedTypes);
		}
		
		@Override
		public @UnmodifiableView Set<Integer> getChangedCount() {
			simplify();
			return Collections.unmodifiableSet(changedCounts);
		}
	}
}
