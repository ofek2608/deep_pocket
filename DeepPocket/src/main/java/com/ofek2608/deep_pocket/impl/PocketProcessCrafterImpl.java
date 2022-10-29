package com.ofek2608.deep_pocket.impl;


import com.ofek2608.deep_pocket.api.*;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessCrafter;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessRecipe;
import com.ofek2608.deep_pocket.api.struct.CrafterContext;
import com.ofek2608.deep_pocket.api.struct.WorldCraftingPattern;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

final class PocketProcessCrafterImpl implements PocketProcessCrafter {
	private final PocketProcessRecipe parent;
	private final ProvidedResources resources;
	private final UUID patternId;

	PocketProcessCrafterImpl(PocketProcessRecipe parent, ProvidedResources resources, UUID patternId) {
		this.parent = parent;
		this.resources = resources;
		this.patternId = patternId;
	}

	@Override
	public PocketProcessRecipe getParent() {
		return parent;
	}

	@Override
	public ProvidedResources getResources() {
		return resources;
	}

	@Override
	public UUID getPatternId() {
		return patternId;
	}

	@Override
	public boolean executeCrafter(Pocket pocket) {
		if (pocket.getPattern(patternId) instanceof WorldCraftingPattern pattern && (pattern.getLevel().getBlockEntity(pattern.getPos()) instanceof PatternSupportedBlockEntity blockEntity)) {
			try {
				if (!blockEntity.executePattern(new CrafterContext(pocket, pattern, this, parent, resources)))
					return false;
			} catch (Exception ignored) {}
		}
		resources.returnAllToParent();
		return true;
	}
}
