package com.ofek2608.deep_pocket.api.struct.server;

import com.ofek2608.deep_pocket.api.struct.CraftingPattern;
import com.ofek2608.deep_pocket.api.struct.LevelBlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ServerCraftingPattern {
	public final UUID id;
	public final CraftingPattern pattern;
	public final List<LevelBlockPos> positions;
	
	public ServerCraftingPattern(UUID id, CraftingPattern pattern) {
		this.id = id;
		this.pattern = pattern;
		this.positions = new ArrayList<>();
	}
}
