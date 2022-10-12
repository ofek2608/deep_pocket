package com.ofek2608.deep_pocket_conversions.api;

import com.ofek2608.deep_pocket.api.struct.ItemType;
import org.jetbrains.annotations.Nullable;

public class EmptyCompilableRecipe implements ICompilableRecipe {
	public static final EmptyCompilableRecipe INSTANCE = new EmptyCompilableRecipe();

	@Override
	public ItemType getResultType() {
		return ItemType.EMPTY;
	}

	@Override
	public @Nullable ValueRule compile(IMVCalculationCtx ctx) {
		return null;
	}
}
