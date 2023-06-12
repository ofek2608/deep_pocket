package com.ofek2608.deep_pocket;

import com.ofek2608.deep_pocket.impl.client.KeyBinds;
import com.ofek2608.deep_pocket.impl.DeepPocketConfig;
import com.ofek2608.deep_pocket.impl.PacketHandler;
import com.ofek2608.deep_pocket.impl.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DeepPocketMod.ID)
public final class DeepPocketMod {
	public static final String ID = "deep_pocket";
	
	public static ResourceLocation loc(String path) {
		return new ResourceLocation(ID, path);
	}
	
	public DeepPocketMod() {
		PacketHandler.loadClass();
		DeepPocketConfig.loadClass();
		KeyBinds.loadClass();
		ModItems.REG.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
}
