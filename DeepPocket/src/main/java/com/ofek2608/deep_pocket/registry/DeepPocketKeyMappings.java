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
	public static final KeyMapping PROCESSES_KEY = new KeyMapping("deep_pocket.key.processes", InputConstants.KEY_T, "key.categories.inventory");

	@Mod.EventBusSubscriber(modid = DeepPocketMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	private static class ForgeEvents {
		@SubscribeEvent
		public static void event(TickEvent.ClientTickEvent event) {
			if (event.phase != TickEvent.Phase.START)
				return;
			boolean openPocket = POCKET_KEY.consumeClick();
			boolean openProcesses = PROCESSES_KEY.consumeClick();
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.screen != null)
				return;
			Player player = minecraft.player;
			if (player == null)
				return;

			if (openPocket)
				DeepPocketPacketHandler.sbOpenPocket(0);
			if (openProcesses)
				DeepPocketPacketHandler.sbOpenPocket(1);
		}
	}

	@Mod.EventBusSubscriber(modid = DeepPocketMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
	private static class ModEvents {
		@SubscribeEvent
		public static void event(RegisterKeyMappingsEvent event) {
			event.register(POCKET_KEY);
			event.register(PROCESSES_KEY);
		}
	}
}
