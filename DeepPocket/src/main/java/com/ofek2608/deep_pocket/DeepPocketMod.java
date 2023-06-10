package com.ofek2608.deep_pocket;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

@Mod(DeepPocketMod.ID)
public final class DeepPocketMod {
	public static final String ID = "deep_pocket";
	
	public static ResourceLocation loc(String path) {
		return new ResourceLocation(ID, path);
	}
}
