package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.struct.ElementType;

import java.util.Set;

public interface Knowledge {
	boolean contains(ElementType type);
	void add(ElementType ... types);
	void remove(ElementType ... types);
	Set<ElementType> asSet();
	Snapshot createSnapshot();
	Knowledge copy();





	interface Snapshot {
		Knowledge getKnowledge();
		ElementType[] getRemoved();
		ElementType[] getAdded();
	}
}
