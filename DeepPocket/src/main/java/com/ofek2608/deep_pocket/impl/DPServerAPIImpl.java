package com.ofek2608.deep_pocket.impl;

import com.mojang.logging.LogUtils;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DPServerAPI;
import com.ofek2608.deep_pocket.api.RandomUtils;
import com.ofek2608.deep_pocket.api.enums.PocketAccess;
import com.ofek2608.deep_pocket.api.pocket.ModifiablePocket;
import com.ofek2608.deep_pocket.api.types.EntryType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.*;

public final class DPServerAPIImpl extends SavedData implements DPServerAPI {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final EntryType[] POSSIBLE_ICONS = new EntryType[] {
			new EntryType(EntryType.CATEGORY_ITEM, new ResourceLocation("minecraft:stone")),
			new EntryType(EntryType.CATEGORY_ITEM, new ResourceLocation("minecraft:dirt")),
			new EntryType(EntryType.CATEGORY_ITEM, new ResourceLocation("minecraft:cobblestone")),
			new EntryType(EntryType.CATEGORY_ITEM, new ResourceLocation("minecraft:deepslate")),
			new EntryType(EntryType.CATEGORY_ITEM, new ResourceLocation("minecraft:coal_block")),
			new EntryType(EntryType.CATEGORY_ITEM, new ResourceLocation("minecraft:crafting_table")),
			new EntryType(EntryType.CATEGORY_ITEM, new ResourceLocation("minecraft:chest")),
			new EntryType(EntryType.CATEGORY_ITEM, new ResourceLocation("minecraft:furnace")),
			new EntryType(EntryType.CATEGORY_ITEM, new ResourceLocation("minecraft:oak_log")),
			new EntryType(EntryType.CATEGORY_ITEM, new ResourceLocation("minecraft:oak_planks")),
	};
	private static DPServerAPIImpl instance;
	
	
	private final MinecraftServer server;
	private final Random random = new Random();
	private final Map<UUID, PocketImpl> pockets = new HashMap<>();
	private final Map<UUID, List<ServerPlayer>> viewingPlayers = new HashMap<>();
	
	public DPServerAPIImpl(MinecraftServer server) {
		this.server = server;
	}
	
	public DPServerAPIImpl(MinecraftServer server, CompoundTag saved) {
		this(server);
		
	}
	
	@Override
	public Optional<ModifiablePocket> getPocket(UUID pocketId) {
		return Optional.ofNullable(pockets.get(pocketId));
	}
	
	@Override
	public ModifiablePocket createPocket(UUID owner) {
		UUID pocketId = UUID.randomUUID();
		PocketPropertiesImpl properties = new PocketPropertiesImpl(
				pocketId,
				owner,
				"New Pocket",
				PocketAccess.PRIVATE,
				RandomUtils.fromList(random, POSSIBLE_ICONS),
				RandomUtils.randomHueColor(random)
		);
		PocketImpl pocket = new PocketImpl(properties);
		pockets.put(pocketId, pocket);
		return pocket;
	}
	
	@Override
	public void deletePocket(UUID pocketId) {
		pockets.remove(pocketId);
	}
	
	public void tick() {
		var players = server.getPlayerList().getPlayers();
		//TODO
	}
	
	@Override
	public boolean isDirty() {
		return true;
	}
	
	@Override
	public CompoundTag save(CompoundTag saved) {
		//TODO
		return saved;
	}
	
	@Mod.EventBusSubscriber(modid = DeepPocketMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	private static final class Events {
		@SubscribeEvent
		public static void event(ServerStartedEvent event) {
			if (instance != null) {
				LOGGER.error("ServerStartedEvent was called when instance is nonnull");
			}
			MinecraftServer server = event.getServer();
			var dataStorage = server.overworld().getDataStorage();
			instance = dataStorage.computeIfAbsent(
					saved -> new DPServerAPIImpl(server, saved),
					() -> new DPServerAPIImpl(server),
					DeepPocketMod.ID + "-server_api"
			);
		}
		
		@SubscribeEvent
		public static void event(ServerStoppedEvent event) {
			if (instance == null) {
				LOGGER.error("ServerStoppedEvent was called when instance is null");
			}
			instance = null;
		}
		
		@SubscribeEvent
		public static void event(TickEvent.ServerTickEvent event) {
			if (event.phase != TickEvent.Phase.END) {
				return;
			}
			
			if (instance == null) {
				LOGGER.error("ServerTickEvent was called when instance is null");
				return;
			}
			
			instance.tick();
		}
	}
}
