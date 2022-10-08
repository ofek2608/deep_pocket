package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.*;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.api.events.DeepPocketServerStartedEvent;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
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
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.UUID;

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
			UUID playerId = player.getUUID();
			api.cachePlayerName(playerId, player.getGameProfile().getName());

			PacketDistributor.PacketTarget packetTarget = PacketDistributor.PLAYER.with(()->player);
			//permit public key
			DeepPocketPacketHandler.cbPermitPublicPocket(packetTarget, DeepPocketConfig.Common.ALLOW_PUBLIC_POCKETS.get());
			//values
			DeepPocketPacketHandler.cbClearItemValues(packetTarget);
			DeepPocketPacketHandler.cbSetItemValue(packetTarget, api.getItemValues());
			//player name cache
			DeepPocketPacketHandler.cbSetPlayersName(packetTarget, api.getPlayerNameCache());
			//knowledge
			DeepPocketPacketHandler.cbClearKnowledge(packetTarget);
			DeepPocketPacketHandler.cbAddKnowledge(packetTarget, api.getKnowledge(playerId).asSet().toArray(new ItemType[0]));
			//pockets
			DeepPocketPacketHandler.cbClearPockets(packetTarget);
			for (Pocket pocket : api.getPockets().values()) {
				UUID pocketId = pocket.getPocketId();
				UUID owner = pocket.getOwner();
				String name = pocket.getName();
				ItemType icon = pocket.getIcon();
				int color = pocket.getColor();
				PocketSecurityMode securityMode = pocket.getSecurityMode();

				DeepPocketPacketHandler.cbCreatePocket(packetTarget, pocketId, owner, name, icon, color, securityMode);
				if (pocket.canAccess(player))
					DeepPocketPacketHandler.cbPocketSetItemCount(packetTarget, pocketId, pocket.getItems());
			}
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
