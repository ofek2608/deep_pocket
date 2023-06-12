package com.ofek2608.deep_pocket.impl;

import com.mojang.logging.LogUtils;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DPServerAPI;
import com.ofek2608.deep_pocket.api.RandomUtils;
import com.ofek2608.deep_pocket.api.ServerConfig;
import com.ofek2608.deep_pocket.api.enums.PocketAccess;
import com.ofek2608.deep_pocket.api.pocket.ModifiablePocket;
import com.ofek2608.deep_pocket.api.types.EntryType;
import com.ofek2608.deep_pocket.def.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;

import java.util.*;

public final class ServerAPIImpl extends SavedData implements DPServerAPI {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final EntryType[] POSSIBLE_ICONS = {
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
	public static ServerAPIImpl instance;
	
	
	public final MinecraftServer server;
	public final Random random = new Random();
	public final Map<UUID, PocketImpl> pockets = new HashMap<>();
	public final Map<UUID, Set<ServerPlayer>> viewingPlayers = new HashMap<>();
	public final ServerConfigImpl serverConfig = new ServerConfigImpl(
			DeepPocketConfig.Common.ALLOW_PUBLIC_POCKETS.get(),
			DeepPocketConfig.Common.REQUIRE_POCKET_FACTORY.get()
	);
	public boolean valid;
	
	public ServerAPIImpl(MinecraftServer server) {
		this.server = server;
	}
	
	public ServerAPIImpl(MinecraftServer server, CompoundTag saved) {
		this(server);
		ListTag savedPockets = saved.getList("pockets", Tag.TAG_COMPOUND);
		for (int i = 0; i < savedPockets.size(); i++) {
			try {
				CompoundTag savedPocket = savedPockets.getCompound(i);
				PocketImpl pocket = PocketImpl.load(savedPocket);
				pockets.put(pocket.properties.getPocketId(), pocket);
			} catch (Exception e) {
				LOGGER.error("Couldn't load a pocket", e);
			}
		}
	}
	
	@Override
	public CompoundTag save(CompoundTag saved) {
		ListTag savedPockets = new ListTag();
		for (PocketImpl pocket : pockets.values()) {
			savedPockets.add(PocketImpl.save(pocket));
		}
		saved.put("pockets", savedPockets);
		
		return saved;
	}
	
	@Override
	public boolean isValid() {
		return valid;
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
	public Optional<ModifiablePocket> payAndCreatePocket(ServerPlayer player) {
		OptionalInt pocketFactorySlot;
		if (serverConfig.requirePocketFactory()) {
			pocketFactorySlot = getPocketFactorySlot(player);
			if (pocketFactorySlot.isEmpty()) {
				return Optional.empty();
			}
		} else {
			pocketFactorySlot = OptionalInt.empty();
		}
		ModifiablePocket pocket = createPocket(player.getUUID());
		PacketDistributor.PacketTarget target = PacketDistributor.PLAYER.with(() -> player);
		PacketHandler.cbAddPocket(target, pocket.getProperties().getPocketId());
		//TODO send created packet packet
		
		if (pocketFactorySlot.isPresent()) {
			player.getInventory().setItem(
					pocketFactorySlot.getAsInt(),
					new ItemStack(Items.STONE) //TODO set as pocket item instead
			);
		}
		
		return Optional.of(pocket);
	}
	
	private OptionalInt getPocketFactorySlot(ServerPlayer player) {
		Inventory inventory = player.getInventory();
		Item pocketFactory = ModItems.POCKET_FACTORY.get();
		if (inventory.getItem(inventory.selected).is(pocketFactory)) {
			return OptionalInt.of(inventory.selected);
		}
		for (int i = inventory.getContainerSize() - 1; i >= 0; i--) {
			if (inventory.getItem(i).is(pocketFactory)) {
				return OptionalInt.of(i);
			}
		}
		return OptionalInt.empty();
	}
	
	@Override
	public void deletePocket(UUID pocketId) {
		PocketImpl pocket = pockets.remove(pocketId);
		viewingPlayers.remove(pocketId);
		if (pocket == null) {
			return;
		}
		PacketHandler.cbDeletePocket(PacketDistributor.ALL.noArg(), pocketId);
	}
	
	@Override
	public ServerConfig getServerConfig() {
		return serverConfig;
	}
	
	public void tick() {
		var onlinePlayers = server.getPlayerList().getPlayers();
		for (var entry : pockets.entrySet()) {
			UUID pocketId = entry.getKey();
			PocketImpl pocket = entry.getValue();
			sendPocket(onlinePlayers, pocketId, pocket);
		}
	}
	
	private void sendPocket(List<ServerPlayer> onlinePlayers, UUID pocketId, PocketImpl pocket) {
		PocketPropertiesImpl properties = pocket.properties;
		sendPropertiesUpdate(properties);
		var viewingPlayers = this.viewingPlayers.computeIfAbsent(pocketId, key -> new HashSet<>());
		
		List<Connection> connectionsClear = new ArrayList<>();
		List<Connection> connectionsUpdate = new ArrayList<>();
		List<Connection> connectionsInit = new ArrayList<>();
		
		for (ServerPlayer player : viewingPlayers.toArray(ServerPlayer[]::new)) {
			boolean canWatch = properties.getAccess().canWatch(properties.getOwner(), player);
			Connection connection = player.connection.connection;
			if (canWatch) {
				connectionsUpdate.add(connection);
			} else {
				connectionsClear.add(connection);
				viewingPlayers.remove(player);
			}
		}
		for (ServerPlayer player : onlinePlayers) {
			if (viewingPlayers.contains(player)) {
				continue;
			}
			if (!properties.getAccess().canWatch(properties.getOwner(), player)) {
				continue;
			}
			Connection connection = player.connection.connection;
			connectionsInit.add(connection);
			viewingPlayers.add(player);
		}
		
		sendPocketClear(connectionsClear, pocketId);
		sendPocketUpdate(connectionsUpdate, pocketId, pocket);
		sendPocketInit(connectionsInit, pocketId, pocket);
	}
	
	private void sendPropertiesUpdate(PocketPropertiesImpl properties) {
		if (!properties.isChanged) {
			return;
		}
		properties.isChanged = false;
		PacketHandler.cbAddProperties(PacketDistributor.ALL.noArg(), properties);
	}
	
	
	private void sendPocketClear(List<Connection> connections, UUID pocketId) {
		if (connections.size() == 0) {
			return;
		}
		PacketDistributor.PacketTarget target = PacketDistributor.NMLIST.with(() -> connections);
		PacketHandler.cbClearPocket(target, pocketId);
	}
	
	private void sendPocketUpdate(List<Connection> connections, UUID pocketId, PocketImpl pocket) {
		if (connections.size() == 0) {
			return;
		}
		PacketDistributor.PacketTarget target = PacketDistributor.NMLIST.with(() -> connections);
		for (TypeData typeData : pocket.typeData.values()) {
			if (typeData.changedCount) {
				PacketHandler.cbSetTypeCount(target, pocketId, typeData.type, typeData.count);
				typeData.changedCount = false;
			}
		}
	}
	
	private void sendPocketInit(List<Connection> connections, UUID pocketId, PocketImpl pocket) {
		if (connections.size() == 0) {
			return;
		}
		PacketDistributor.PacketTarget target = PacketDistributor.NMLIST.with(() -> connections);
		PacketHandler.cbAddPocket(target, pocketId);
		for (TypeData typeData : pocket.typeData.values()) {
			PacketHandler.cbSetTypeCount(target, pocketId, typeData.type, typeData.count);
		}
	}
	
	
	
	
	
	
	
	
	
	
	@Override
	public boolean isDirty() {
		return true;
	}
	
	@Mod.EventBusSubscriber(modid = DeepPocketMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	private static final class Events {
		@SubscribeEvent
		public static void event(ServerStartedEvent event) {
			if (instance != null) {
				LOGGER.error("ServerStartedEvent was called when instance is nonnull");
				instance.valid = false;
			}
			MinecraftServer server = event.getServer();
			var dataStorage = server.overworld().getDataStorage();
			instance = dataStorage.computeIfAbsent(
					saved -> new ServerAPIImpl(server, saved),
					() -> new ServerAPIImpl(server),
					DeepPocketMod.ID + "-server_api"
			);
		}
		
		@SubscribeEvent
		public static void event(ServerStoppedEvent event) {
			if (instance == null) {
				LOGGER.error("ServerStoppedEvent was called when instance is null");
			} else {
				instance.valid = false;
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
		
		@SubscribeEvent
		public static void event(PlayerEvent.PlayerLoggedInEvent event) {
			if (instance == null) {
				LOGGER.error("PlayerLoggedInEvent was called when instance is null");
				return;
			}
			if (!(event.getEntity() instanceof ServerPlayer player)) {
				return;
			}
			PacketDistributor.PacketTarget target = PacketDistributor.PLAYER.with(() -> player);
			PacketHandler.cbSetServerConfig(target, instance.serverConfig);
			for (PocketImpl pocket : instance.pockets.values()) {
				PacketHandler.cbAddProperties(target, pocket.properties);
			}
		}
	}
}
