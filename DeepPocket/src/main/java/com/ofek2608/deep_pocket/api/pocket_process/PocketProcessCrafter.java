package com.ofek2608.deep_pocket.api.pocket_process;

import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.ProvidedResources;

import java.util.UUID;

public interface PocketProcessCrafter {
	PocketProcessRecipe getParent();
	ProvidedResources getResources();
	UUID getPatternId();
	boolean executeCrafter(Pocket pocket);
}
