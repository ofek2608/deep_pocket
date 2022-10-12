package com.ofek2608.deep_pocket_conversions.api;

import com.ofek2608.deep_pocket_conversions.Configs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public final class DPCRecipeLoader {
	private DPCRecipeLoader() {}
	private static final List<IRecipeLoader<?>> REGISTERED_RECIPE_LOADERS = new ArrayList<>();

	public static void registerLoader(IRecipeLoader<?> loader) {
		REGISTERED_RECIPE_LOADERS.add(loader);
	}



	@SuppressWarnings({"unchecked", "rawtypes"})
	public static List<ICompilableRecipe> loadRecipes(MinecraftServer server) {
		if (!Configs.Common.RECIPE_LOADERS_ALL.get())
			return new ArrayList<>();

		RecipeManager recipeManager = server.getRecipeManager();
		List<ICompilableRecipe> result = new ArrayList<>();

		for (IRecipeLoader<?> registeredRecipeLoader : REGISTERED_RECIPE_LOADERS) {
			RecipeType<?> type = ForgeRegistries.RECIPE_TYPES.getValue(registeredRecipeLoader.getId());
			if (type == null)
				continue;
			List<? extends Recipe<?>> recipes = recipeManager.getAllRecipesFor((RecipeType)type);
			for (Recipe<?> recipe : recipes)
				((IRecipeLoader<Recipe<?>>)registeredRecipeLoader).load(result::add, recipe);
		}

		return result;
	}
}
