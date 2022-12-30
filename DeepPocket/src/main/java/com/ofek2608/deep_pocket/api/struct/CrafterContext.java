package com.ofek2608.deep_pocket.api.struct;

import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.ProvidedResources;
import com.ofek2608.deep_pocket.api.pocket_process.old.PocketProcessCrafter;
import com.ofek2608.deep_pocket.api.pocket_process.old.PocketProcessRecipe;

public final class CrafterContext {
	public final Pocket pocket;
	public final WorldCraftingPatternOld pattern;
	public final PocketProcessCrafter crafter;
	public final PocketProcessRecipe recipe;
	public final ProvidedResources resources;

	public CrafterContext(Pocket pocket, WorldCraftingPatternOld pattern, PocketProcessCrafter crafter, PocketProcessRecipe recipe, ProvidedResources resources) {
		this.pocket = pocket;
		this.pattern = pattern;
		this.crafter = crafter;
		this.recipe = recipe;
		this.resources = resources;
	}
}
