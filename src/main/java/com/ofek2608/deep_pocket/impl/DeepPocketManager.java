package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.*;
import com.ofek2608.deep_pocket.api.events.DeepPocketServerStartedEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

public final class DeepPocketManager {
	private DeepPocketManager() {}

	private static final DeepPocketClientApiImpl clientApi = new DeepPocketClientApiImpl();
	private static DeepPocketServerApiImpl serverApi;

	public static DeepPocketClientApi getClientApi() { return clientApi; }
	public static @Nullable DeepPocketServerApi getServerApi() { return serverApi; }

	@Mod.EventBusSubscriber(modid = DeepPocketMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	private static final class ForgeEvents {
		private ForgeEvents() {}

		@SubscribeEvent
		public static void event(ServerStartedEvent event) {
			var dataStorage = event.getServer().overworld().getDataStorage();
			DeepPocketSavedData savedData = dataStorage.computeIfAbsent(DeepPocketSavedData::new, DeepPocketSavedData::new, DeepPocketMod.ID + ":server_api");
			serverApi = savedData.api;
			MinecraftForge.EVENT_BUS.post(new DeepPocketServerStartedEvent(serverApi));
		}

		@SubscribeEvent
		public static void event(ServerStoppedEvent event) {
			serverApi = null;
		}

		@SubscribeEvent
		public static void event(PlayerEvent.PlayerLoggedInEvent event) {
			if (!(event.getEntity() instanceof ServerPlayer player))
				return;
			DeepPocketServerApi api = DeepPocketManager.getServerApi();
			if (api == null)
				return;
			api.cachePlayerName(player.getUUID(), player.getGameProfile().getName());
		}

		@SubscribeEvent
		public static void event(TickEvent.ServerTickEvent event) {
			if (event.phase != TickEvent.Phase.END || serverApi == null)
				return;
			serverApi.tickUpdate(event.getServer());
		}
	}

	@Mod.EventBusSubscriber(modid = DeepPocketMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
	private static final class ModEvents {
		private ModEvents() {}
	}

	private static final class DeepPocketSavedData extends SavedData {
		private final DeepPocketServerApiImpl api;

		private DeepPocketSavedData() {
			this.api = new DeepPocketServerApiImpl();
		}

		private DeepPocketSavedData(CompoundTag tag) {
			this.api = new DeepPocketServerApiImpl(tag);
		}

		@Override
		public CompoundTag save(CompoundTag tag) {
			return api.save(tag);
		}

		@Override
		public boolean isDirty() {
			return true;
		}
	}
}
