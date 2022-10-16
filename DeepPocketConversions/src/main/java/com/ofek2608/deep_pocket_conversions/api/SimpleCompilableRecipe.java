package com.ofek2608.deep_pocket_conversions.api;

import com.ofek2608.deep_pocket.api.struct.ItemType;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import javax.annotation.Nullable;
import java.util.List;

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
	public @Nullable ValueRule compile(IMVCalculationCtx ctx) {
		List<Ingredient> ingredients = recipe.getIngredients();
		ingredients = ingredients.stream().filter(ing->!ing.isEmpty()).toList();
		if (ingredients.size() == 0)
			return null; //Fix for fireworks and other stuff
		return ValueRule.divide(
						ICompilableRecipe.compileIngredients(ctx, ingredients),
						ProcessingNum.valueOf(recipe.getResultItem().getCount())
		);
	}
}
