package com.ofek2608.deep_pocket.api.pocket_process.old;

import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.RecipeRequest;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public interface PocketProcessManager {
	@UnmodifiableView List<PocketProcessUnit> getUnits();
	PocketProcessUnit requestRecipe(RecipeRequest[] types);
	
	long supplyItem(ElementType item, long amount);
	PocketProcessManager recreate();
	
	@ApiStatus.Internal
	void executeCrafters(Pocket pocket);
}
