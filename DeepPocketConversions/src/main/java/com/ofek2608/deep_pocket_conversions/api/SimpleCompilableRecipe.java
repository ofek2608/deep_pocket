package com.ofek2608.deep_pocket_conversions.api;

import com.ofek2608.deep_pocket.api.struct.ItemType;
import net.minecraft.world.item.crafting.Recipe;

public class SimpleCompilableRecipe implements ICompilableRecipe {
	private final Recipe<?> recipe;
	public SimpleCompilableRecipe(Recipe<?> recipe) {
		this.recipe = recipe;
	}

	@Override
	public ItemType getResultType() {
		return new ItemType(recipe.getResultItem());
	}

	@Override
	public ValueRule compile(IMVCalculationCtx ctx) {
		return ValueRule.divide(
						ICompilableRecipe.compileIngredients(ctx, recipe.getIngredients()),
						ProcessingNum.valueOf(recipe.getResultItem().getCount())
		);
	}
}
