package com.ofek2608.deep_pocket.impl;

import com.mojang.logging.LogUtils;
import com.ofek2608.deep_pocket.api.DeepPocketHelper;
import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.Knowledge;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.api.struct.ElementConversions;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.PocketInfo;
import com.ofek2608.deep_pocket.api.struct.RecipeRequest;
import com.ofek2608.deep_pocket.api.struct.server.ServerElementIndices;
import com.ofek2608.deep_pocket.api.struct.server.ServerPocket;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import com.ofek2608.deep_pocket.registry.items.PocketItem;
import com.ofek2608.deep_pocket.registry.pocket_screen.PocketMenu;
import com.ofek2608.deep_pocket.registry.process_screen.ProcessMenu;
import net.minecraft.nbt.*;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

final class DeepPocketServerApiImpl extends DeepPocketApiImpl<DeepPocketHelper, ServerPocket> implements DeepPocketServerApi {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final MinecraftServer server;
	private final Map<ServerPlayer, Set<UUID>> viewedPockets = new HashMap<>();
	private final Map<UUID, Knowledge.Snapshot> knowledge = new HashMap<>();
	private final ServerElementIndices elementIndices;

	DeepPocketServerApiImpl(DeepPocketHelper helper, MinecraftServer server, ServerElementIndices elementIndices, ElementConversions conversions) {
		super(helper);
		this.server = server;
		this.conversions = conversions;
		this.elementIndices = elementIndices;
	}

	DeepPocketServerApiImpl(DeepPocketHelper helper, MinecraftServer server, ServerElementIndices elementIndices, ElementConversions conversions, CompoundTag tag) {
		this(helper, server, elementIndices, conversions);
		boolean errors = false;
		// Loading: OldElementIndexes
		Map<Integer,ElementType> oldElementIndexes = new HashMap<>();
		for (Tag savedIndex : tag.getList("elementIndexes", 10)) {
			try {
				oldElementIndexes.put(
						((CompoundTag)savedIndex).getInt("index"),
						ElementType.load(((CompoundTag)savedIndex).getCompound("type"))
				);
			} catch (Exception e) {
				errors = true;
			}
		}
		IntUnaryOperator elementIdGetter = oldId -> elementIndices.getIndexOrCreate(oldElementIndexes.getOrDefault(oldId, ElementType.empty()));
		// Loading: Pockets
		boolean allowPublicPockets = DeepPocketConfig.Common.ALLOW_PUBLIC_POCKETS.get();
		for (Tag savedPocket : tag.getList("pockets", 10)) {
			try {
				ServerPocket readPocket = ServerPocket.load((CompoundTag)savedPocket, allowPublicPockets, conversions, elementIdGetter);
				pockets.put(readPocket.getPocketId(), readPocket);
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
				UUID playerId = ((CompoundTag)savedKnowledge).getUUID("player");
				Knowledge readKnowledge = loadKnowledge((CompoundTag)savedKnowledge, elementIdGetter);
				knowledge.put(playerId, readKnowledge.createSnapshot());
			} catch (Exception e) {
				errors = true;
			}
		}
		if (errors)
			LOGGER.warn("There was error while trying to load Deep Pocket's saved data");
	}
	
	
	@Override
	public ServerElementIndices getElementIndices() {
		return elementIndices;
	}
	
	private Knowledge loadKnowledge(CompoundTag saved, IntUnaryOperator elementIdGetter) {
		Knowledge knowledge = helper.createKnowledge(conversions);
		knowledge.add(saved.getList("elements", Tag.TAG_INT)
				.stream()
				.mapToInt(tag -> tag instanceof NumericTag nTag ? nTag.getAsInt() : 0)
				.map(elementIdGetter)
				.toArray()
		);
		return knowledge;
	}







	public CompoundTag save(CompoundTag tag) {
		// Saving: Pockets
		ListTag savedPockets = new ListTag();
		getPockets().map(ServerPocket::save).forEach(savedPockets::add);
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
		knowledge.forEach((playerId, playerKnowledge) -> savedKnowledge.add(saveKnowledge(playerId, playerKnowledge.getKnowledge())));
		tag.put("knowledge", savedKnowledge);
		return tag;
	}

	private static CompoundTag saveKnowledge(UUID playerId, Knowledge knowledge) {
		CompoundTag saved = new CompoundTag();
		saved.putUUID("player", playerId);
		ListTag savedElements = new ListTag();
		for (Integer elementId : knowledge.asSet()) {
			savedElements.add(IntTag.valueOf(elementId));
		}
		saved.put("elements", savedElements);
		return saved;
	}





	@Nullable
	@Override
	public ServerPocket createPocket(UUID pocketId, UUID owner, PocketInfo info) {
		if (pockets.containsKey(pocketId)) {
			return null;
		}
		ServerPocket pocket = new ServerPocket(pocketId, owner, info, conversions);
		pockets.put(pocketId, pocket);
		DeepPocketPacketHandler.cbCreatePocket(PacketDistributor.ALL.noArg(), pocketId, owner, info);
		return pocket;
	}

	@Override
	public boolean destroyPocket(UUID pocketId) {
		boolean changed = super.destroyPocket(pocketId);
		if (changed) {
			DeepPocketPacketHandler.cbDestroyPocket(PacketDistributor.ALL.noArg(), pocketId);
			viewedPockets.values().forEach(viewed -> viewed.remove(pocketId));
		}
		return changed;
	}

	@Override
	public void clearPockets() {
		super.clearPockets();
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
	public Knowledge getKnowledge(UUID playerId) {
		return knowledge.computeIfAbsent(playerId, id->helper.createKnowledge(conversions).createSnapshot()).getKnowledge();
	}

	private void open(ServerPlayer player, UUID pocketId, Function<ServerPocket, MenuConstructor> menu) {
		ServerPocket pocket = getPocket(pocketId);
		if (pocket == null)
			return;
		NetworkHooks.openScreen(player, new SimpleMenuProvider(menu.apply(pocket), Component.empty()));
		DeepPocketPacketHandler.cbSetViewedPocket(PacketDistributor.PLAYER.with(()->player), pocket.getPocketId());
	}

	@Override
	public void openPocket(ServerPlayer player, UUID pocketId) {
		open(player, pocketId, pocket -> (int id, Inventory inv, Player player0)-> new PocketMenu(id, inv, pocketId));
	}

	@Override
	public void openProcesses(ServerPlayer player, UUID pocketId) {
		open(player, pocketId, pocket -> (int id, Inventory inv, Player player0)-> new ProcessMenu(id, player0, pocket));
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
		ServerPocket pocket = getPocket(pocketId);
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
		if (info.securityMode == PocketSecurityMode.PUBLIC && !DeepPocketConfig.Common.ALLOW_PUBLIC_POCKETS.get())
			info.securityMode = PocketSecurityMode.TEAM;
		ServerPocket newPocket;
		do {
			newPocket = createPocket(UUID.randomUUID(), player.getUUID(), info);
		} while (newPocket == null);
		player.setItemInHand(hand, PocketItem.createStack(newPocket.getPocketId()));
	}

	@Override
	public void changePocketSettingsFrom(ServerPlayer player, UUID pocketId, PocketInfo info) {
		if (info.securityMode == PocketSecurityMode.PUBLIC && !DeepPocketConfig.Common.ALLOW_PUBLIC_POCKETS.get())
			info.securityMode = PocketSecurityMode.TEAM;
		ServerPocket pocket = getPocket(pocketId);
		if (pocket == null || !player.getUUID().equals(pocket.getOwner()))
			return;
		pocket.setInfo(info);
	}

	@Override
	public void destroyPocketFor(ServerPlayer player, UUID pocketId) {
		ServerPocket pocket = getPocket(pocketId);
		if (pocket == null || !player.getUUID().equals(pocket.getOwner()))
			return;
		destroyPocket(pocketId);
	}

	@Override
	public void requestProcessFor(ServerPlayer player, UUID pocketId, RecipeRequest[] requests) {
		//FIXME
//		if (requests.length == 0)
//			return;
//		for (RecipeRequest request : requests)
//			if (request.getPatternsCount() == 0)
//				return;
//		Pocket pocket = getPocket(pocketId);
//		if (pocket == null || !pocket.canAccess(player))
//			return;
//
//		Map<ItemType,Long> unitRequirements = new HashMap<>();
//		List<ItemType[]> recipeRequirements = new ArrayList<>();
//		for (RecipeRequest request : requests) {
//			UUID patternId = request.getPattern(0);
//			CraftingPatternOld pattern = pocket.getPattern(patternId);
//			if (pattern == null)
//				return;
//			var inputCountMap = pattern.getInputCountMap();
//			for (var entry : inputCountMap.entrySet()) {
//				long add = advancedMul(entry.getValue(), request.getAmount());
//				unitRequirements.compute(entry.getKey(), (t, oldAmount) -> oldAmount == null ? add : advancedSum(oldAmount, add));
//			}
//			recipeRequirements.add(inputCountMap.keySet().toArray(ItemType[]::new));
//		}
//		ItemType[] types = unitRequirements.keySet().toArray(ItemType[]::new);
//		PocketProcessUnit unit = pocket.getProcesses().addUnit(types);
//		for (int i = 0; i < types.length; i++)
//			unit.setLeftToProvide(i, unitRequirements.get(types[i]));
//		for (int i = 0; i < requests.length; i++) {
//			RecipeRequest request = requests[i];
//			PocketProcessRecipe recipe = unit.addRecipe(request.getResult(), recipeRequirements.get(i));
//			recipe.setLeftToCraft(request.getAmount());
//			List.of(request.getPatterns()).forEach(recipe::addCrafter);
//		}
//
//		for (ItemType type : types)
//			pocket.getItemsMap().computeIfPresent(type, (t,count) -> unit.supplyItem(type, count));
	}

	void tickUpdate() {
		//======
		// Init
		//======
		List<ServerPocket> pockets = getPockets().toList();
		List<ServerPlayer> onlinePLayers = server.getPlayerList().getPlayers();

		//==============
		// Pockets tick
		//==============
		for (ServerPocket pocket : pockets) {
			pocket.tick();
		}
		
		//================
		// Players Update
		//================
		
		// Remove people that left
		for (ServerPlayer player : viewedPockets.keySet()) {
			if (player.hasDisconnected()) {
				viewedPockets.remove(player);
			}
		}
		
		// Add people that joined
		List<Connection> newPlayersConnections = new ArrayList<>();
		for (ServerPlayer onlinePLayer : onlinePLayers) {
			if (viewedPockets.containsKey(onlinePLayer)) {
				continue;
			}
			viewedPockets.put(onlinePLayer, new HashSet<>());
			newPlayersConnections.add(onlinePLayer.connection.connection);
		}
		if (newPlayersConnections.size() > 0) {
			PacketDistributor.PacketTarget newPlayersTarget = PacketDistributor.NMLIST.with(() -> newPlayersConnections);
			DeepPocketPacketHandler.cbClearPockets(newPlayersTarget);
			DeepPocketPacketHandler.cbPermitPublicPocket(newPlayersTarget, DeepPocketConfig.Common.ALLOW_PUBLIC_POCKETS.get());
			DeepPocketPacketHandler.cbConversions(newPlayersTarget, conversions);
			DeepPocketPacketHandler.cbSetPlayersName(newPlayersTarget, getPlayerNameCache());
			DeepPocketPacketHandler.cbKnowledgeClear(newPlayersTarget);
		}

		//==================
		// Adding knowledge
		//==================

		for (ServerPlayer player : onlinePLayers) {
			Knowledge.Snapshot snapshot = knowledge.get(player.getUUID());
			Knowledge knowledge = snapshot.getKnowledge();
			for (ItemStack item : player.getInventory().items)
				knowledge.add(elementIndices.getIndex(ElementType.item(item)));
			knowledge.add(elementIndices.getIndex(ElementType.item(player.containerMenu.getCarried())));
			
			int[] added = snapshot.getAdded();
			int[] removed = snapshot.getRemoved();
			if (added.length > 0) {
				DeepPocketPacketHandler.cbKnowledgeAdd(PacketDistributor.PLAYER.with(()->player), added);
			}
			if (removed.length > 0) {
				DeepPocketPacketHandler.cbKnowledgeRem(PacketDistributor.PLAYER.with(()->player), removed);
			}
		}
		
		
		//=====================
		// Send pocket updates
		//=====================
		
		for (ServerPocket pocket : pockets) {
			UUID pocketId = pocket.getPocketId();
			List<Connection> allConnections = new ArrayList<>();
			List<Connection> toSendClear = new ArrayList<>();
			List<Connection> toSendSetup = new ArrayList<>();
			List<Connection> toSendUpdate = new ArrayList<>();
			for (ServerPlayer player : onlinePLayers) {
				Set<UUID> playerViewedPockets = viewedPockets.get(player);
				boolean seesPocket = playerViewedPockets.contains(pocketId);
				boolean canAccess = pocket.canAccess(player);
				Connection connection = player.connection.connection;
				allConnections.add(connection);
				if (canAccess) {
					(seesPocket ? toSendUpdate : toSendSetup).add(connection);
					playerViewedPockets.add(pocketId);
				} else {
					if (seesPocket) {
						toSendClear.add(connection);
						playerViewedPockets.remove(pocketId);
					}
				}
			}
			
			if (allConnections.size() > 0 && pocket.didChangeInfo()) {
				DeepPocketPacketHandler.cbPocketInfo(PacketDistributor.NMLIST.with(()->allConnections), pocketId, pocket.getInfo());
			}
			
			if (toSendClear.size() > 0) {
				DeepPocketPacketHandler.cbPocketClearData(PacketDistributor.NMLIST.with(()->toSendClear), pocketId);
			}
			if (toSendSetup.size() > 0) {
				DeepPocketPacketHandler.cbPocketUpdate(PacketDistributor.NMLIST.with(()->toSendSetup), pocketId, pocket.createSetup());
			}
			if (toSendUpdate.size() > 0) {
				DeepPocketPacketHandler.cbPocketUpdate(PacketDistributor.NMLIST.with(()->toSendUpdate), pocketId, pocket.createUpdate());
			}
		}

		//======
		// Post
		//======

		for (ServerPlayer player : onlinePLayers) {
			cachePlayerName(player.getUUID(), player.getGameProfile().getName());
		}

		this.pockets.values().forEach(ServerPocket::clearUpdates);
		
		//TODO remove
		for (ServerPlayer players : onlinePLayers)
			if (players.containerMenu instanceof ProcessMenu menu)
				menu.sendUpdate();
	}
}
