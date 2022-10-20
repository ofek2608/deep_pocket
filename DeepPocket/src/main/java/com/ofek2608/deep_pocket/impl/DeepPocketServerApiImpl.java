package com.ofek2608.deep_pocket.impl;

import com.mojang.logging.LogUtils;
import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.struct.*;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import com.ofek2608.deep_pocket.registry.MenuWithPocket;
import com.ofek2608.deep_pocket.registry.items.PocketItem;
import com.ofek2608.deep_pocket.registry.pocket_screen.PocketMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Stream;

class DeepPocketServerApiImpl extends DeepPocketApiImpl implements DeepPocketServerApi {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final MinecraftServer server;
	private final Map<ServerPlayer, Set<UUID>> viewedPockets = new HashMap<>();
	private final Map<UUID, PlayerKnowledge.Snapshot> knowledge = new HashMap<>();

	DeepPocketServerApiImpl(MinecraftServer server, ItemConversions conversions) {
		this.server = server;
		this.conversions = conversions;
	}

	DeepPocketServerApiImpl(MinecraftServer server, ItemConversions conversions, CompoundTag tag) {
		this(server, conversions);
		boolean errors = false;
		// Loading: Pockets
		boolean allowPublicPockets = DeepPocketConfig.Common.ALLOW_PUBLIC_POCKETS.get();
		for (Tag savedPocket : tag.getList("pockets", 10)) {
			try {
				Pocket readPocket = new Pocket(server, conversions, allowPublicPockets, (CompoundTag)savedPocket);
				pocketSnapshots.put(readPocket.getPocketId(), readPocket.createSnapshot());
			} catch (Exception e) {
				errors = true;
			}
		}
		// Loading: PlayerNameCache
		for (Tag savedCache : tag.getList("playerNameCache", 10)) {
			try {
				super.cachePlayerName(((CompoundTag)savedCache).getUUID("id"), ((CompoundTag)savedCache).getString("name"));
			} catch (Exception e) {
				errors = true;
			}
		}
		// Loading: Knowledge
		for (Tag savedKnowledge : tag.getList("knowledge", 10)) {
			try {
				PlayerKnowledge readKnowledge = new PlayerKnowledge(conversions, (CompoundTag)savedKnowledge);
				knowledge.put(readKnowledge.getPlayer(), readKnowledge.createSnapshot());
			} catch (Exception e) {
				errors = true;
			}
		}
		if (errors)
			LOGGER.warn("There was error while trying to load Deep Pocket's saved data");
	}

	public CompoundTag save(CompoundTag tag) {
		// Saving: Pockets
		ListTag savedPockets = new ListTag();
		getPockets().map(Pocket::save).forEach(savedPockets::add);
		tag.put("pockets", savedPockets);
		// Saving: PlayerNameCache
		ListTag savedPlayerNameCache = new ListTag();
		for (var entry : playerNameCache.entrySet()) {
			CompoundTag singleCache = new CompoundTag();
			singleCache.putUUID("id", entry.getKey());
			singleCache.putString("name", entry.getValue());
			savedPlayerNameCache.add(singleCache);
		}
		tag.put("playerNameCache", savedPlayerNameCache);
		// Saving: Knowledge
		ListTag savedKnowledge = new ListTag();
		for (PlayerKnowledge.Snapshot snapshot : knowledge.values())
			savedKnowledge.add(snapshot.getKnowledge().save());
		tag.put("knowledge", savedKnowledge);
		return tag;
	}

	@Nullable
	@Override
	public Pocket createPocket(UUID pocketId, UUID owner, PocketInfo info) {
		Pocket pocket = super.createPocket(pocketId, owner, info);
		if (pocket != null)
			DeepPocketPacketHandler.cbCreatePocket(PacketDistributor.ALL.noArg(), pocketId, owner, info);
		return pocket;
	}

	@Override
	public boolean destroyPocket(UUID pocketId) {
		boolean changed = super.destroyPocket(pocketId);
		if (changed)
			DeepPocketPacketHandler.cbDestroyPocket(PacketDistributor.ALL.noArg(), pocketId);
		return changed;
	}

	@Override
	public void clearPockets() {
		super.clearPockets();
		DeepPocketPacketHandler.cbClearPockets(PacketDistributor.ALL.noArg());
	}

	@Override
	public boolean cachePlayerName(UUID id, String name) {
		boolean changed = super.cachePlayerName(id, name);
		if (changed)
			DeepPocketPacketHandler.cbSetPlayersName(PacketDistributor.ALL.noArg(), Map.of(id, name));
		return changed;
	}

	@Override
	public @UnmodifiableView Set<UUID> getViewedPockets(ServerPlayer player) {
		return Collections.unmodifiableSet(viewedPockets.get(player));
	}

	@Override
	public PlayerKnowledge getKnowledge(UUID playerId) {
		return knowledge.computeIfAbsent(playerId, id->new PlayerKnowledge(conversions, id).createSnapshot()).getKnowledge();
	}

	@Override
	public void openPocket(ServerPlayer player, UUID pocketId) {
		if (player.containerMenu != player.inventoryMenu)
			return;
		Pocket pocket = getPocket(pocketId);
		if (pocket == null)
			return;
		NetworkHooks.openScreen(player, PocketMenu.MENU_PROVIDER);
		if (player.containerMenu instanceof MenuWithPocket menu)
			menu.setPocket(pocket);
		DeepPocketPacketHandler.cbSetViewedPocket(PacketDistributor.PLAYER.with(()->player), pocket.getPocketId());
	}

	private static @Nullable InteractionHand getHandForItem(Player player, Item item) {
		ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
		ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
		if (!main.isEmpty() && main.is(item))
			return InteractionHand.MAIN_HAND;
		if (!off.isEmpty() && off.is(item))
			return InteractionHand.MAIN_HAND;
		return null;
	}

	@Override
	public void selectPocketFor(ServerPlayer player, UUID pocketId) {
		InteractionHand hand = getHandForItem(player, DeepPocketRegistry.POCKET_LINK_ITEM.get());
		if (hand == null)
			return;
		Pocket pocket = getPocket(pocketId);
		if (pocket == null || !pocket.canAccess(player))
			return;

		ItemStack newPocketItem = PocketItem.createStack(pocketId);
		ItemStack itemInHand = player.getItemInHand(hand);
		if (itemInHand.getCount() == 1) {
			player.setItemInHand(hand, newPocketItem);
			return;
		}
		itemInHand.shrink(1);
		player.setItemInHand(hand, itemInHand);
		if (player.getInventory().add(newPocketItem))
			return;
		ItemEntity droppedPocketItem = player.drop(newPocketItem, false);
		if (droppedPocketItem == null)
			return;
		droppedPocketItem.setNoPickUpDelay();
		droppedPocketItem.setOwner(player.getUUID());
	}

	@Override
	public void createPocketFor(ServerPlayer player, PocketInfo info) {
		InteractionHand hand = getHandForItem(player, DeepPocketRegistry.POCKET_FACTORY_ITEM.get());
		if (hand == null)
			return;
		if (info.securityMode == PocketSecurityMode.PUBLIC && DeepPocketConfig.Common.ALLOW_PUBLIC_POCKETS.get())
			info.securityMode = PocketSecurityMode.TEAM;
		Pocket newPocket;
		do {
			newPocket = createPocket(UUID.randomUUID(), player.getUUID(), info);
		} while (newPocket == null);
		player.setItemInHand(hand, PocketItem.createStack(newPocket.getPocketId()));
	}

	@Override
	public void changePocketSettingsFrom(ServerPlayer player, UUID pocketId, PocketInfo info) {
		if (info.securityMode == PocketSecurityMode.PUBLIC && DeepPocketConfig.Common.ALLOW_PUBLIC_POCKETS.get())
			info.securityMode = PocketSecurityMode.TEAM;
		Pocket pocket = getPocket(pocketId);
		if (pocket == null || !player.getUUID().equals(pocket.getOwner()))
			return;
		pocket.setInfo(info);
	}

	@Override
	public void destroyPocketFor(ServerPlayer player, UUID pocketId) {
		Pocket pocket = getPocket(pocketId);
		if (pocket == null || !player.getUUID().equals(pocket.getOwner()))
			return;
		destroyPocket(pocketId);
	}





	private PlayerKnowledge.Snapshot getKnowledgeSnapshot(UUID playerId) {
		PlayerKnowledge.Snapshot snapshot = knowledge.get(playerId);
		if (snapshot == null) {
			snapshot = new PlayerKnowledge(conversions, playerId).createSnapshot();
			knowledge.put(playerId, snapshot);
		} else {
			knowledge.put(playerId, snapshot.getKnowledge().createSnapshot());
		}
		return snapshot;
	}

	void tickUpdate() {
		//======
		// Init
		//======
		List<Pocket> pockets = getPockets().toList();
		List<ServerPlayer> onlinePLayers = server.getPlayerList().getPlayers();

		//==================
		// Adding knowledge
		//==================

		for (ServerPlayer player : onlinePLayers) {
			PlayerKnowledge knowledge = getKnowledge(player.getUUID());
			for (ItemStack item : player.getInventory().items)
				knowledge.add(new ItemType(item));
			knowledge.add(new ItemType(player.containerMenu.getCarried()));
		}

		//===============================
		// Preparing for sorting packets
		//===============================

		//remove players that left
		for (ServerPlayer player : viewedPockets.keySet().toArray(new ServerPlayer[0]))
			if (player.hasDisconnected())
				viewedPockets.remove(player);
		//remembering who needs which data to minimize packet creations
		List<ServerPlayer> oldPlayers = new ArrayList<>();
		List<ServerPlayer> newPlayers = new ArrayList<>();
		List<Connection> toSendOnUpdate = new ArrayList<>();
		List<Connection> toSendOnJoin = new ArrayList<>();
		Map<Pocket.Snapshot,List<Connection>> toSendPocketUpdate = new HashMap<>();
		Map<Pocket.Snapshot,List<Connection>> toSendPocketClear = new HashMap<>();
		Map<Pocket,List<Connection>> toSendPocketFill = new HashMap<>();

		//=================================
		// Sorting players to send packets
		//=================================

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			Connection connection = player.connection.connection;
			if (viewedPockets.containsKey(player)) {
				//The player is old
				oldPlayers.add(player);
				toSendOnUpdate.add(connection);
			} else {
				//The player is new
				newPlayers.add(player);
				toSendOnJoin.add(connection);
				viewedPockets.put(player, new HashSet<>());
			}
			Set<UUID> playerViewedPockets = viewedPockets.get(player);
			//Clear and Update pockets that the player already viewing
			for (UUID pocketId : playerViewedPockets.toArray(new UUID[0])) {
				Pocket.Snapshot pocketSnapshot = pocketSnapshots.get(pocketId);
				if (pocketSnapshot == null)
					continue;
				if (pocketSnapshot.getPocket().canAccess(player)) {
					toSendPocketUpdate.computeIfAbsent(pocketSnapshot, p -> new ArrayList<>()).add(connection);
				} else {
					toSendPocketClear.computeIfAbsent(pocketSnapshot, p->new ArrayList<>()).add(connection);
					playerViewedPockets.remove(pocketId);
				}
			}
			//Fill pockets that the player doesn't view
			for (Pocket pocket : pockets) {
				UUID pocketId = pocket.getPocketId();
				if (!playerViewedPockets.contains(pocketId) && pocket.canAccess(player)) {
					toSendPocketFill.computeIfAbsent(pocket, p -> new ArrayList<>()).add(connection);
					playerViewedPockets.add(pocketId);
				}
			}
		}

		//=================
		// Sending packets
		//=================

		//On Update
		if (toSendOnUpdate.size() > 0) {
			PacketDistributor.PacketTarget packetTarget = PacketDistributor.NMLIST.with(()->toSendOnUpdate);
			for (Pocket.Snapshot snapshot : pocketSnapshots.values())
				if (snapshot.didChangedInfo())
					DeepPocketPacketHandler.cbPocketInfo(packetTarget, snapshot.getPocket().getPocketId(), snapshot.getPocket().getInfo());
			for (ServerPlayer player : oldPlayers) {
				PacketDistributor.PacketTarget playerTarget = PacketDistributor.PLAYER.with(() -> player);
				PlayerKnowledge.Snapshot snapshot = getKnowledgeSnapshot(player.getUUID());
				ItemType[] removed = snapshot.getRemoved();
				ItemType[] added = snapshot.getAdded();
				if (removed.length > 0)
					DeepPocketPacketHandler.cbAddKnowledge(playerTarget, removed);
				if (added.length > 0)
					DeepPocketPacketHandler.cbAddKnowledge(playerTarget, added);
			}
		}
		//On Join
		if (toSendOnJoin.size() > 0) {
			PacketDistributor.PacketTarget packetTarget = PacketDistributor.NMLIST.with(()->toSendOnJoin);
			//permit public key
			DeepPocketPacketHandler.cbPermitPublicPocket(packetTarget, DeepPocketConfig.Common.ALLOW_PUBLIC_POCKETS.get());
			//item conversions
			DeepPocketPacketHandler.cbItemConversions(packetTarget, conversions);
			//player name cache
			DeepPocketPacketHandler.cbSetPlayersName(packetTarget, getPlayerNameCache());
			//knowledge
			for (ServerPlayer player : newPlayers)
				DeepPocketPacketHandler.cbAddKnowledge(PacketDistributor.PLAYER.with(() -> player), getKnowledgeSnapshot(player.getUUID()).getKnowledge().asSet().toArray(new ItemType[0]));
			//pockets
			DeepPocketPacketHandler.cbClearPockets(packetTarget);
			for (Pocket pocket : pockets)
				DeepPocketPacketHandler.cbCreatePocket(packetTarget, pocket.getPocketId(), pocket.getOwner(), pocket.getInfo());
		}
		//Pocket Update
		for (var entry : toSendPocketUpdate.entrySet()) {
			Pocket.Snapshot snapshot = entry.getKey();
			UUID pocketId = snapshot.getPocket().getPocketId();
			PacketDistributor.PacketTarget packetTarget = PacketDistributor.NMLIST.with(entry::getValue);
			//Changed items
			Map<ItemType,Long> changedItems = snapshot.getChangedItems();
			if (!changedItems.isEmpty())
				DeepPocketPacketHandler.cbPocketSetItemCount(packetTarget, pocketId, changedItems);
			//Update patterns
			CraftingPattern[] addedPatterns = snapshot.getAddedPatterns();
			UUID[] removedPatterns = snapshot.getRemovedPatterns();
			if (addedPatterns.length > 0 || removedPatterns.length > 0)
				DeepPocketPacketHandler.cbUpdatePatterns(packetTarget, pocketId, addedPatterns, removedPatterns);
		}
		//Pocket Clear
		for (var entry : toSendPocketClear.entrySet()) {
			PacketDistributor.PacketTarget packetTarget = PacketDistributor.NMLIST.with(entry::getValue);
			Pocket.Snapshot pocketSnapshot = entry.getKey();
			Pocket pocket = pocketSnapshot.getPocket();
			UUID pocketId = pocket.getPocketId();
			DeepPocketPacketHandler.cbPocketClearItems(packetTarget, pocketId);
			UUID[] patternsToRemove = Stream.concat(
							pocket.getPatterns().stream().map(CraftingPattern::getPatternId),
							Stream.of(pocketSnapshot.getRemovedPatterns())
			).toArray(UUID[]::new);
			if (patternsToRemove.length > 0)
				DeepPocketPacketHandler.cbUpdatePatterns(packetTarget, pocketId, new CraftingPattern[0], patternsToRemove);
		}
		//Pocket Fill
		for (var entry : toSendPocketFill.entrySet()) {
			PacketDistributor.PacketTarget packetTarget = PacketDistributor.NMLIST.with(entry::getValue);
			Pocket pocket = entry.getKey();
			UUID pocketId = pocket.getPocketId();
			DeepPocketPacketHandler.cbPocketSetItemCount(packetTarget, pocketId, pocket.getItems());
			Collection<CraftingPattern> patterns = pocket.getPatterns();
			if (patterns.size() > 0)
				DeepPocketPacketHandler.cbUpdatePatterns(packetTarget, pocketId, patterns.toArray(CraftingPattern[]::new), new UUID[0]);
		}

		//======
		// Post
		//======

		for (ServerPlayer newPlayer : newPlayers)
			cachePlayerName(newPlayer.getUUID(), newPlayer.getGameProfile().getName());

		this.pocketSnapshots.replaceAll((pocketId, pocketSnapshot)->pocketSnapshot.getPocket().createSnapshot());
	}
}
