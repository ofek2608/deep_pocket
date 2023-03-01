package com.ofek2608.deep_pocket.api;

import java.util.Set;

public interface Knowledge {
	boolean contains(int type);
	void add(int ... types);
	void remove(int ... types);
	Set<Integer> asSet();
	Snapshot createSnapshot();
	Knowledge copy();





	interface Snapshot {
		Knowledge getKnowledge();
		int[] getRemoved();
		int[] getAdded();
	}
}
