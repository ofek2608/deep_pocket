package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.events.DPClientAPIEvent;
import com.ofek2608.deep_pocket.api.events.DPServerAPIEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class APIs {
	private APIs() {}
	
	private static Optional<DPClientAPI> client;
	private static Optional<DPServerAPI> server;
	
	public static Optional<DPClientAPI> getClient() {
		return client;
	}
	
	public static Optional<DPServerAPI> getServer() {
		return server;
	}
	
	@Mod.EventBusSubscriber(modid = DeepPocketMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	private static final class Events {
		@SubscribeEvent
		public static void event(DPClientAPIEvent event) {
			client = event.getApi();
		}
		
		@SubscribeEvent
		public static void event(DPServerAPIEvent event) {
			server = event.getApi();
		}
	}
}
