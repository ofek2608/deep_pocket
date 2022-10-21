package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.struct.ItemType;

import java.util.Set;

public interface Knowledge {
	boolean contains(ItemType type);
	void add(ItemType ... types);
	void remove(ItemType ... types);
	Set<ItemType> asSet();
	Snapshot createSnapshot();
	Knowledge copy();





	interface Snapshot {
		Knowledge getKnowledge();
		ItemType[] getRemoved();
		ItemType[] getAdded();
	}
}
