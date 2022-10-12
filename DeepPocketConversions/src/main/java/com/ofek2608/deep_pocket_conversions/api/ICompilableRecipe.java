package com.ofek2608.deep_pocket_conversions.api;

import com.ofek2608.deep_pocket.api.struct.ItemType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.IntStream;

public interface ICompilableRecipe {
	ItemType getResultType();
	@Nullable ValueRule compile(IMVCalculationCtx ctx);

	static ValueRule compileIngredient(IMVCalculationCtx ctx, Ingredient ingredient) {
		if (ingredient.isEmpty())
			return ValueRule.constant0();
		var types = ctx.getTypes();
		return ValueRule.min(
						IntStream.range(0, types.size())
										.filter(i->{
											ItemStack stack = types.get(i).create();
											return !stack.hasCraftingRemainingItem() && ingredient.test(stack);
										})
										.mapToObj(ValueRule::item)
										.toArray(ValueRule[]::new)
		);
	}

	static ValueRule compileIngredients(IMVCalculationCtx ctx, Collection<Ingredient> ingredients) {
		return ValueRule.sum(
						ingredients.stream()
										.map(ingredient -> compileIngredient(ctx, ingredient))
										.toArray(ValueRule[]::new)
		);
	}

	static ValueRule compileIngredients(IMVCalculationCtx ctx, Ingredient ... ingredients) {
		return compileIngredients(ctx, Arrays.asList(ingredients));
	}
}
