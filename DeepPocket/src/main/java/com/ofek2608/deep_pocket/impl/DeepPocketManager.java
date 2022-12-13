package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import com.ofek2608.deep_pocket.api.DeepPocketHelper;
import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.events.DeepPocketBuildConversionsEvent;
import com.ofek2608.deep_pocket.api.events.DeepPocketServerStartedEvent;
import com.ofek2608.deep_pocket.api.struct.ElementConversions;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
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

	private static DeepPocketHelperImpl helper;
	private static DeepPocketClientHelperImpl clientHelper;
	private static DeepPocketClientApiImpl clientApi;
	private static DeepPocketServerApiImpl serverApi;

	public static DeepPocketHelper getHelper() {
		DeepPocketHelper res = helper;
		return res == null ? helper = new DeepPocketHelperImpl() : res;
	}
	public static DeepPocketClientHelper getClientHelper() {
		DeepPocketClientHelper res = clientHelper;
		return res == null ? clientHelper = new DeepPocketClientHelperImpl() : res;
	}
	public static DeepPocketClientApi getClientApi() {
		DeepPocketClientApi res = clientApi;
		return res == null ? clientApi = new DeepPocketClientApiImpl(getClientHelper()) : res;
	}
	public static @Nullable DeepPocketServerApi getServerApi() {
		return serverApi;
	}

	@Mod.EventBusSubscriber(modid = DeepPocketMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	private static final class ForgeEvents {
		private ForgeEvents() {}

		@SubscribeEvent
		public static void event(ServerStartedEvent event) {
			MinecraftServer server = event.getServer();
			DeepPocketHelper helper = getHelper();
			ElementConversions.Builder conversionsBuilder = new ElementConversions.Builder();
			MinecraftForge.EVENT_BUS.post(new DeepPocketBuildConversionsEvent(server, conversionsBuilder));
			ElementConversions conversions = conversionsBuilder.build();
			var dataStorage = server.overworld().getDataStorage();
			DeepPocketSavedData savedData = dataStorage.computeIfAbsent(
							tag->new DeepPocketSavedData(helper, server, conversions, tag),
							()->new DeepPocketSavedData(helper, server, conversions),
							DeepPocketMod.ID + "-server_api"
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

		@SubscribeEvent
		public static void event(PlayerEvent.ItemCraftedEvent event) {
			Player player = event.getEntity();
			if (player == null || player.level.isClientSide || serverApi == null)
				return;
			serverApi.getKnowledge(player.getUUID()).add(ElementType.item(event.getCrafting()));
		}
	}

	@Mod.EventBusSubscriber(modid = DeepPocketMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
	private static final class ModEvents {
		private ModEvents() {}
	}

	private static final class DeepPocketSavedData extends SavedData {
		private final DeepPocketServerApiImpl api;

		private DeepPocketSavedData(DeepPocketHelper helper, MinecraftServer server, ElementConversions conversions) {
			this.api = new DeepPocketServerApiImpl(helper, server, conversions);
		}

		private DeepPocketSavedData(DeepPocketHelper helper, MinecraftServer server, ElementConversions conversions, CompoundTag tag) {
			this.api = new DeepPocketServerApiImpl(helper, server, conversions, tag);
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
