package com.ofek2608.deep_pocket_conversions;

import com.ofek2608.deep_pocket_conversions.api.BasicRecipeLoader;
import com.ofek2608.deep_pocket_conversions.api.DPCRecipeLoader;
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
		Configs.loadClass();
		//TODO add default VMs
		//TODO check blacklist
		registerDefaultRecipesLoaders();
	}

	private static void registerDefaultRecipesLoaders() {
		DPCRecipeLoader.registerLoader(new BasicRecipeLoader<>(new ResourceLocation("crafting"), Configs.Common.RECIPE_LOADERS_CRAFTING));
		DPCRecipeLoader.registerLoader(new BasicRecipeLoader<>(new ResourceLocation("smelting"), Configs.Common.RECIPE_LOADERS_SMELTING));
		DPCRecipeLoader.registerLoader(new BasicRecipeLoader<>(new ResourceLocation("blasting"), Configs.Common.RECIPE_LOADERS_BLASTING));
		DPCRecipeLoader.registerLoader(new BasicRecipeLoader<>(new ResourceLocation("smoking"), Configs.Common.RECIPE_LOADERS_SMOKING));
		DPCRecipeLoader.registerLoader(new BasicRecipeLoader<>(new ResourceLocation("campfire_cooking"), Configs.Common.RECIPE_LOADERS_CAMPFIRE));
		DPCRecipeLoader.registerLoader(new BasicRecipeLoader<>(new ResourceLocation("stonecutting"), Configs.Common.RECIPE_LOADERS_STONECUTTING));
		DPCRecipeLoader.registerLoader(new BasicRecipeLoader<>(new ResourceLocation("smithing"), Configs.Common.RECIPE_LOADERS_SMITHING));
	}
}
