package com.ofek2608.deep_pocket.client;

import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.impl.ClientAPIImpl;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DeepPocketMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
final class PocketScreenOpener {
	private PocketScreenOpener() {
	}
	
	@SubscribeEvent
	public static void event(TickEvent.ClientTickEvent event) {
		if (!KeyBinds.KEY_OPEN_POCKET.consumeClick()) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.screen != null) {
			return;
		}
		ClientAPIImpl api = ClientAPIImpl.instance;
		if (api == null) {
			return;
		}
		minecraft.setScreen(new PocketSelectionScreen(api));
	}
}
