package com.ofek2608.deep_pocket.api.struct.server;

import com.ofek2608.deep_pocket.api.struct.ElementType;

import java.util.*;

public final class ServerElementIndices {
	private final List<ElementType> types = new ArrayList<>();
	private final Map<ElementType, Integer> indices = new HashMap<>();
	
	public ServerElementIndices() {
		this.types.add(ElementType.empty());
		this.types.add(ElementType.energy());
	}
	
	public int size() {
		return types.size();
	}
	
	public ElementType getElement(int index) {
		return types.get(index);
	}
	
	public Optional<ElementType> getOptionalElement(int index) {
		return 0 <= index && index < types.size() ? Optional.of(types.get(index)) : Optional.empty();
	}
	
	public int getIndex(ElementType type) {
		return indices.getOrDefault(type, -1);
	}
	
	public int getIndexOrCreate(ElementType type) {
		return indices.computeIfAbsent(type, t -> {
			types.add(t);
			return types.size() - 1;
		});
	}
}
