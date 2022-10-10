package com.ofek2608.deep_pocket_elemental;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

@Mod(value = DeepPocketModElemental.ID)
public class DeepPocketModElemental {
	public static final String ID = "deep_pocket_elemental";
	public static ResourceLocation loc(String path) {
		return new ResourceLocation(ID, path);
	}


	public DeepPocketModElemental() {
		DeepPocketRegistryElemental.loadClass();
		DeepPocketConfigElemental.loadClass();
	}
}
