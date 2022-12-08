package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.ItemType;

import java.util.Set;

public interface Knowledge0 {
	boolean contains(ElementType type);
	void add(ElementType ... types);
	void remove(ElementType ... types);
	Set<ElementType> asSet();
	Snapshot createSnapshot();
	Knowledge0 copy();





	interface Snapshot {
		Knowledge0 getKnowledge();
		ElementType[] getRemoved();
		ElementType[] getAdded();
	}
}
