package com.ofek2608.deep_pocket_conversions;

import com.ofek2608.deep_pocket.DeepPocketMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

@Mod(value = DeepPocketModConversions.ID)
public class DeepPocketModConversions {
	public static final String ID = "deep_pocket_conversions";
	public static ResourceLocation loc(String path) {
		return new ResourceLocation(ID, path);
	}

	public DeepPocketModConversions() {
		System.out.println("Hello World, i am " + ID + " and i am acknowledged of " + DeepPocketMod.ID + "!");
	}
}
