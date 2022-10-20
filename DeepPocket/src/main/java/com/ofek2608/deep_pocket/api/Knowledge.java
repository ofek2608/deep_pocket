package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.struct.ItemType;

import java.util.Set;

public interface Knowledge {
	public boolean contains(ItemType type);
	public void add(ItemType ... types);
	public void remove(ItemType ... types);
	public Set<ItemType> asSet();
	public Snapshot createSnapshot();
	public Knowledge copy();





	public interface Snapshot {
		public Knowledge getKnowledge();
		public ItemType[] getRemoved();
		public ItemType[] getAdded();
	}
}
