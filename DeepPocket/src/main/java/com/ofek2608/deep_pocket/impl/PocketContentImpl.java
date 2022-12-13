package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.Knowledge;
import com.ofek2608.deep_pocket.api.pocket.PocketContent;
import com.ofek2608.deep_pocket.api.struct.ElementConversions;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.ElementTypeStack;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

import static com.ofek2608.deep_pocket.utils.AdvancedLongMath.*;

final class PocketContentImpl implements PocketContent {
	private ElementConversions conversions = ElementConversions.EMPTY;
	private final Map<ElementType,Integer> indexMap = new HashMap<>();
	private final List<ElementType> types = new ArrayList<>();
	private final List<Long> counts = new ArrayList<>();
	private SnapshotImpl lastSnapshot = new SnapshotImpl();
	
	PocketContentImpl() {}
	
	@Override
	public ElementConversions getConversions() {
		return conversions;
	}
	
	@Override
	public void setConversions(ElementConversions conversions) {
		this.conversions = conversions;
		for (int i = 0; i < types.size(); i++)
			if (conversions.hasValue(types.get(i)))
				remove(i--);
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
	public void insert(ElementType type, long count) {
		if (type.isEmpty())
			return;
		long[] valueCount = conversions.getValue(type);
		if (valueCount == null) {
			setCount(type, advancedSum(getCount(type), count));
			return;
		}
		for (int i = 0; i < valueCount.length; i++) {
			if (valueCount[i] != 0)
				continue;
			ElementType.TConvertible convertible = conversions.getBaseElement(i);
			setCount(
					convertible,
					advancedSum(getCount(convertible), advancedMul(count, valueCount[i]))
			);
		}
	}
	
	@Override
	public void insert(ElementTypeStack stack) {
		insert(stack.getType(), stack.getCount());
	}
	
	@Override
	public long extract(long[] baseElements, long count) {
		count = advancedMin(count, getMaxExtract(baseElements));
		if (count == 0)
			return 0;
		for (int i = 0; i < baseElements.length; i++) {
			ElementType.TConvertible type = conversions.getBaseElement(i);
			setCount(type, advancedSub(getCount(type), advancedMul(baseElements[i], count)));
		}
		return count;
	}
	
	@Override
	public long extract(long[] baseElements) {
		return extract(baseElements, 1);
	}
	
	@Override
	public long extract(ElementType type, long count) {
		if (type.isEmpty() || count == 0)
			return 0;
		
		long[] valueCount = conversions.getValue(type);
		if (valueCount != null)
			return extract(valueCount, count);
		
		long currentCount = getCount(type);
		count = advancedMin(count, currentCount);
		setCount(type, advancedSub(currentCount, count));
		return count;
	}
	
	@Override
	public long extract(ElementTypeStack stack) {
		return extract(stack.getType(), stack.getCount());
	}
	
	@Override
	public long getMaxExtract(long[] baseElements) {
		long maxExtract = -1;
		for (int i = 0; i < baseElements.length; i++) {
			if (baseElements[i] == 0)
				continue;
			long current = advancedDiv(getCount(conversions.getBaseElement(i)), baseElements[i]);
			maxExtract = advancedMin(maxExtract, current);
		}
		return maxExtract;
	}
	
	private long getMaxExtract0(@Nullable Knowledge knowledge, Map<ElementType, Long> counts) {
		long[] baseElementsRequirement = conversions.convertMapToArray(counts);
		long maxExtract = getMaxExtract(baseElementsRequirement);
		for (var entry : counts.entrySet()) {
			if (knowledge != null && !knowledge.contains(entry.getKey()))
				return 0L;
			long current = advancedDiv(getCount(entry.getKey()), entry.getValue());
			maxExtract = advancedMin(maxExtract, current);
		}
		return maxExtract;
	}
	
	@Override
	public long getMaxExtract(@Nullable Knowledge knowledge, Map<ElementType, Long> counts) {
		return getMaxExtract0(knowledge, new HashMap<>(counts));
	}
	
	@Override
	public long getMaxExtract(@Nullable Knowledge knowledge, ElementType... types) {
		Map<ElementType, Long> map = new HashMap<>();
		for (ElementType type : types)
			map.put(type, advancedSum(map.getOrDefault(type, 0L), 1L));
		return getMaxExtract0(knowledge, map);
	}
	
	@Override
	public long getMaxExtract(@Nullable Knowledge knowledge, ElementTypeStack... stacks) {
		Map<ElementType, Long> map = new HashMap<>();
		for (ElementTypeStack stack : stacks)
			map.put(stack.getType(), advancedSum(map.getOrDefault(stack.getType(), 0L), stack.getCount()));
		return getMaxExtract0(knowledge, map);
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
	
	@Override
	public void limitSize(int newSize) {
		while (getSize() > newSize)
			remove(getSize() - 1);
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
