package com.ofek2608.deep_pocket_conversions;

import com.ofek2608.deep_pocket_conversions.api.BasicRecipeLoader;
import com.ofek2608.deep_pocket_conversions.api.DPCRecipeLoader;
import com.ofek2608.deep_pocket_conversions.registry.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

@Mod(value = DPCMod.ID)
public class DPCMod {
	public static final String ID = "deep_pocket_conversions";
	public static ResourceLocation loc(String path) {
		return new ResourceLocation(ID, path);
	}


	public DPCMod() {
		ModRegistry.loadClass();
		DPCConfigs.loadClass();
		registerDefaultRecipesLoaders();
		// TODO output and input values from config folder
		// TODO add option to give costs for item tags
		//  - make sure to give proper priority when multiple item tags have the same item
	}

	private static void registerDefaultRecipesLoaders() {
		DPCRecipeLoader.registerLoader(new BasicRecipeLoader<>(new ResourceLocation("crafting"), DPCConfigs.Common.RECIPE_LOADERS_CRAFTING));
		DPCRecipeLoader.registerLoader(new BasicRecipeLoader<>(new ResourceLocation("smelting"), DPCConfigs.Common.RECIPE_LOADERS_SMELTING));
		DPCRecipeLoader.registerLoader(new BasicRecipeLoader<>(new ResourceLocation("blasting"), DPCConfigs.Common.RECIPE_LOADERS_BLASTING));
		DPCRecipeLoader.registerLoader(new BasicRecipeLoader<>(new ResourceLocation("smoking"), DPCConfigs.Common.RECIPE_LOADERS_SMOKING));
		DPCRecipeLoader.registerLoader(new BasicRecipeLoader<>(new ResourceLocation("campfire_cooking"), DPCConfigs.Common.RECIPE_LOADERS_CAMPFIRE));
		DPCRecipeLoader.registerLoader(new BasicRecipeLoader<>(new ResourceLocation("stonecutting"), DPCConfigs.Common.RECIPE_LOADERS_STONECUTTING));
		DPCRecipeLoader.registerLoader(new BasicRecipeLoader<>(new ResourceLocation("smithing"), DPCConfigs.Common.RECIPE_LOADERS_SMITHING));
	}
}
