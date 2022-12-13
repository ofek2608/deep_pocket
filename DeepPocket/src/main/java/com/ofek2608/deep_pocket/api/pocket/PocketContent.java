package com.ofek2608.deep_pocket.api.pocket;

import com.ofek2608.deep_pocket.api.Knowledge0;
import com.ofek2608.deep_pocket.api.struct.ElementConversions;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.ElementTypeStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;
import java.util.Set;

public interface PocketContent {
	ElementConversions getConversions();
	void setConversions(ElementConversions conversions);
	
	int getIndex(ElementType type);
	ElementType getType(int index);
	long getCount(int index);
	long getCount(ElementType type);
	void setCount(int index, long count);
	void setCount(ElementType type, long count);
	void remove(int index);
	void remove(ElementType type);
	void insert(ElementType type, long count);
	void insert(ElementTypeStack stack);
	long extract(long[] baseElements, long count);
	long extract(long[] baseElements);
	long extract(ElementType type, long count);
	long extract(ElementTypeStack stack);
	long getMaxExtract(long[] baseElements);
	long getMaxExtract(@Nullable Knowledge0 knowledge, Map<ElementType,Long> counts);
	long getMaxExtract(@Nullable Knowledge0 knowledge, ElementType ... types);
	long getMaxExtract(@Nullable Knowledge0 knowledge, ElementTypeStack ... stacks);
	void clear();
	int getSize();
	Snapshot createSnapshot();
	PocketContent copy();
	
	@ApiStatus.Internal
	void setType(int index, ElementType type);
	@ApiStatus.Internal
	void limitSize(int newSize);
	
	
	interface Snapshot {
		@UnmodifiableView Set<Integer> getChangedTypes();
		@UnmodifiableView Set<Integer> getChangedCount();
	}
}
