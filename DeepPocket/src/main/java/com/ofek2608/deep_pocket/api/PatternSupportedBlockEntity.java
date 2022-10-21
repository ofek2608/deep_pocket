package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.struct.WorldCraftingPattern;

public interface PatternSupportedBlockEntity {
	long executePattern(Pocket pocket, WorldCraftingPattern pattern, ProvidedResources resources, long max) throws IllegalArgumentException;
}
