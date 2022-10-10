package com.ofek2608.deep_pocket.registry;

import com.mojang.blaze3d.platform.InputConstants;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

class DeepPocketKeyMappings {
	public static final KeyMapping POCKET_KEY = new KeyMapping("deep_pocket.key.pocket", InputConstants.KEY_R, "key.categories.inventory");

	@Mod.EventBusSubscriber(modid = DeepPocketMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	private static class ForgeEvents {
		@SubscribeEvent
		public static void event(TickEvent.ClientTickEvent event) {
			if (event.phase != TickEvent.Phase.START)
				return;
			if (!POCKET_KEY.consumeClick())
				return;
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.screen != null)
				return;
			Player player = minecraft.player;
			if (player == null)
				return;
			DeepPocketPacketHandler.sbOpenPocket();
		}
	}

	@Mod.EventBusSubscriber(modid = DeepPocketMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
	private static class ModEvents {
		@SubscribeEvent
		public static void event(RegisterKeyMappingsEvent event) {
			event.register(POCKET_KEY);
		}
	}
}
