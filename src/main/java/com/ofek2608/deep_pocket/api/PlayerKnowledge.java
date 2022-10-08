package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.struct.ItemType;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Set;

public interface PlayerKnowledge {
	boolean contains(ItemType type);
	void add(ItemType ... types);
	void remove(ItemType ... types);
	@UnmodifiableView Set<ItemType> asSet();
	void clear();
}
