package com.ofek2608.deep_pocket_elemental;

import com.ofek2608.deep_pocket.DeepPocketMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

@Mod(value = DeepPocketModElemental.ID)
public class DeepPocketModElemental {
	public static final String ID = "deep_pocket_elemental";
	public static ResourceLocation loc(String path) {
		return new ResourceLocation(ID, path);
	}

	public DeepPocketModElemental() {
		System.out.println("Hello World, i am " + ID + " and i am acknowledged of " + DeepPocketMod.ID + "!");
	}
}
