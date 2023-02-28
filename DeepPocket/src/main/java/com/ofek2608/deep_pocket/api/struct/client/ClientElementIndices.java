package com.ofek2608.deep_pocket.api.struct.client;

import com.ofek2608.deep_pocket.api.struct.ElementType;

import java.util.HashMap;
import java.util.Map;

public final class ClientElementIndices {
	private final Map<Integer, ElementType> types = new HashMap<>();
	private final Map<ElementType,Integer> indices = new HashMap<>();
	
	public ElementType getType(int index) {
		return types.get(index);
	}
	
	public int getIndex(ElementType type) {
		return indices.getOrDefault(type, -1);
	}
	
	public void add(int index, ElementType type) {
		types.put(index, type);
		indices.put(type, index);
	}
}
