package com.ofek2608.deep_pocket.api.struct;

import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.ProvidedResources;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessCrafter;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessRecipe;

public final class CrafterContext {
	public final Pocket pocket;
	public final WorldCraftingPattern pattern;
	public final PocketProcessCrafter crafter;
	public final PocketProcessRecipe recipe;
	public final ProvidedResources resources;

	public CrafterContext(Pocket pocket, WorldCraftingPattern pattern, PocketProcessCrafter crafter, PocketProcessRecipe recipe, ProvidedResources resources) {
		this.pocket = pocket;
		this.pattern = pattern;
		this.crafter = crafter;
		this.recipe = recipe;
		this.resources = resources;
	}
}
