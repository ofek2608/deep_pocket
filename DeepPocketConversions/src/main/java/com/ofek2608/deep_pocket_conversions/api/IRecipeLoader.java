package com.ofek2608.deep_pocket_conversions.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import java.util.function.Consumer;

public interface IRecipeLoader<R extends Recipe<?>> {
	public ResourceLocation getId();
	public void load(Consumer<ICompilableRecipe> recipeRegister, R recipe);
}
