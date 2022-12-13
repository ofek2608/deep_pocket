package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.struct.ElementConversions;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

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
	void clear();
	int getSize();
	Snapshot createSnapshot();
	PocketContent copy();
	
	@ApiStatus.Internal
	void setType(int index, ElementType type);
	
	
	interface Snapshot {
		@UnmodifiableView Set<Integer> getChangedTypes();
		@UnmodifiableView Set<Integer> getChangedCount();
	}
}
