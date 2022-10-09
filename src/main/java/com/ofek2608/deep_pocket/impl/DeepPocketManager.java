package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.*;
import com.ofek2608.deep_pocket.api.events.DeepPocketBuildConversionsEvent;
import com.ofek2608.deep_pocket.api.events.DeepPocketServerStartedEvent;
import com.ofek2608.deep_pocket.api.struct.ItemConversions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
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
			MinecraftServer server = event.getServer();
			ItemConversions.Builder conversionsBuilder = new ItemConversions.Builder();
			MinecraftForge.EVENT_BUS.post(new DeepPocketBuildConversionsEvent(server, conversionsBuilder));
			ItemConversions conversions = conversionsBuilder.build();
			var dataStorage = server.overworld().getDataStorage();
			DeepPocketSavedData savedData = dataStorage.computeIfAbsent(
							tag->new DeepPocketSavedData(server, conversions, tag),
							()->new DeepPocketSavedData(server, conversions),
							DeepPocketMod.ID + ":server_api"
			);
			serverApi = savedData.api;
			MinecraftForge.EVENT_BUS.post(new DeepPocketServerStartedEvent(server, serverApi));
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
			serverApi.tickUpdate();
		}
	}

	@Mod.EventBusSubscriber(modid = DeepPocketMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
	private static final class ModEvents {
		private ModEvents() {}
	}

	private static final class DeepPocketSavedData extends SavedData {
		private final DeepPocketServerApiImpl api;

		private DeepPocketSavedData(MinecraftServer server, ItemConversions conversions) {
			this.api = new DeepPocketServerApiImpl(server, conversions);
		}

		private DeepPocketSavedData(MinecraftServer server, ItemConversions conversions, CompoundTag tag) {
			this.api = new DeepPocketServerApiImpl(server, conversions, tag);
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
