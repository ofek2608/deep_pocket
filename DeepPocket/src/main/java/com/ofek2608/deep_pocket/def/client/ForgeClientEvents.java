package com.ofek2608.deep_pocket.def.client;

import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.APIs;
import com.ofek2608.deep_pocket.api.DPClientAPI;
import com.ofek2608.deep_pocket.api.events.DPClientAPIEvent;
import com.ofek2608.deep_pocket.def.client.tabs.ItemsTab;
import com.ofek2608.deep_pocket.def.client.tabs.SettingsTab;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = DeepPocketMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
final class ForgeClientEvents {
	private ForgeClientEvents() {}
	
	@SubscribeEvent
	public static void event(TickEvent.ClientTickEvent event) {
		if (!KeyBinds.KEY_OPEN_POCKET.consumeClick()) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.screen != null) {
			return;
		}
		Optional<DPClientAPI> api = APIs.getClient();
		if (api.isEmpty()) {
			return;
		}
		minecraft.setScreen(new PocketSelectionScreen(api.get()));
	}
	
	@SubscribeEvent
	public static void event(DPClientAPIEvent event) {
		Optional<DPClientAPI> apiOpt = event.getApi();
		if (apiOpt.isEmpty()) {
			return;
		}
		DPClientAPI api = apiOpt.get();
		api.registerPocketTab(DeepPocketMod.loc("items"), ItemsTab.INSTANCE, "aa");
		api.registerPocketTab(DeepPocketMod.loc("settings"), SettingsTab.INSTANCE, "z");
	}
}
