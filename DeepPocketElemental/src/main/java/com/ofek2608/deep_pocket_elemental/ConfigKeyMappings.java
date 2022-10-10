package com.ofek2608.deep_pocket_elemental;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DPEMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
final class ConfigKeyMappings {
	private ConfigKeyMappings() {}

	private static final KeyMapping OVERLAY_POSITION = new KeyMapping("deep_pocket_elemental.key.overlay_position", InputConstants.UNKNOWN.getValue(), "key.categories.inventory");
	private static final KeyMapping OVERLAY_DIRECTION = new KeyMapping("deep_pocket_elemental.key.overlay_direction", InputConstants.UNKNOWN.getValue(), "key.categories.inventory");

	@Mod.EventBusSubscriber(modid = DPEMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	private static final class Reg {
		private Reg() {}

		@SubscribeEvent
		public static void event(RegisterKeyMappingsEvent event) {
			event.register(OVERLAY_POSITION);
			event.register(OVERLAY_DIRECTION);
		}
	}

	@SubscribeEvent
	public static void event(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;
		boolean clickChangePosition = OVERLAY_POSITION.consumeClick();
		boolean clickChangeDirection = OVERLAY_DIRECTION.consumeClick();
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null)
			return;
		if (minecraft.screen != null)
			return;
		if (clickChangePosition) {
			int index = Configs.Client.OVERLAY_LOCATION.get().ordinal();
			index++;
			if (index >= OverlayLocation.values().length)
				index = 0;
			Configs.Client.OVERLAY_LOCATION.set(OverlayLocation.values()[index]);
		}
		if (clickChangeDirection)
			Configs.Client.OVERLAY_DIRECTION.set(!Configs.Client.OVERLAY_DIRECTION.get());
	}
}
