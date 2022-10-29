package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.struct.CrafterContext;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.WorldCraftingPattern;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public interface PatternSupportedBlockEntity {
	boolean containsPattern(UUID patternId);
	boolean executePattern(CrafterContext ctx) throws IllegalArgumentException;

	static long[] getRequirements(WorldCraftingPattern pattern, ProvidedResources resources) throws IllegalArgumentException {
		var inputCountMap = pattern.getInputCountMap();
		Set<ItemType> left = new HashSet<>(inputCountMap.keySet());
		ItemType[] types = resources.getTypes();
		int count = types.length;
		long[] requirements = new long[count];
		for (int i = 0; i < count; i++) {
			requirements[i] = inputCountMap.getOrDefault(types[i], 0L);
			left.remove(types[i]);
		}
		if (left.size() > 0)
			throw new IllegalArgumentException();
		return requirements;
	}
}
