package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.struct.Pocket;
import com.ofek2608.deep_pocket.api.struct.WorldCraftingPattern;

public interface PatternSupportedBlockEntity {
	public boolean executePattern(Pocket pocket, WorldCraftingPattern pattern, long max);
}
