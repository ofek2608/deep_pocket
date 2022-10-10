package com.ofek2608.deep_pocket_elemental;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

@Mod(value = DPEMod.ID)
public final class DPEMod {
	public static final String ID = "deep_pocket_elemental";
	public static ResourceLocation loc(String path) {
		return new ResourceLocation(ID, path);
	}


	public DPEMod() {
		ModRegistry.loadClass();
		Configs.loadClass();
	}
}
