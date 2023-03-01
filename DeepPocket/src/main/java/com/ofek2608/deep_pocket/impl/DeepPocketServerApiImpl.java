package com.ofek2608.deep_pocket.impl;

import com.mojang.logging.LogUtils;
import com.ofek2608.deep_pocket.api.DeepPocketHelper;
import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.Knowledge;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.api.struct.ElementConversionsOld;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.PocketInfo;
import com.ofek2608.deep_pocket.api.struct.RecipeRequest;
import com.ofek2608.deep_pocket.api.struct.server.ServerPocket;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import com.ofek2608.deep_pocket.registry.items.PocketItem;
import com.ofek2608.deep_pocket.registry.pocket_screen.PocketMenu;
import com.ofek2608.deep_pocket.registry.process_screen.ProcessMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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

final class DeepPocketServerApiImpl extends DeepPocketApiImpl<DeepPocketHelper, ServerPocket> implements DeepPocketServerApi {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final MinecraftServer server;
	private final Map<ServerPlayer, Set<UUID>> viewedPockets = new HashMap<>();
	private final Map<UUID, Knowledge.Snapshot> knowledge = new HashMap<>();

	DeepPocketServerApiImpl(DeepPocketHelper helper, MinecraftServer server, ElementConversionsOld conversions) {
		super(helper);
		this.server = server;
		this.conversions = conversions;
	}

	DeepPocketServerApiImpl(DeepPocketHelper helper, MinecraftServer server, ElementConversionsOld conversions, CompoundTag tag) {
		this(helper, server, conversions);
		boolean errors = false;
		// Loading: Pockets
		boolean allowPublicPockets = DeepPocketConfig.Common.ALLOW_PUBLIC_POCKETS.get();
		for (Tag savedPocket : tag.getList("pockets", 10)) {
			try {
				ServerPocket readPocket = ServerPocket.load((CompoundTag)savedPocket, allowPublicPockets, conversions);
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
				Knowledge readKnowledge = loadKnowledge((CompoundTag)savedKnowledge);
				knowledge.put(playerId, readKnowledge.createSnapshot());
			} catch (Exception e) {
				errors = true;
			}
		}
		if (errors)
			LOGGER.warn("There was error while trying to load Deep Pocket's saved data");
	}

//	private ServerPocket loadPocket(boolean allowPublicPocket, CompoundTag saved) {
//		ServerPocket pocket = new ServerPocket(saved.getUUID("pocketId"), saved.getUUID("owner"), PocketInfo.load(saved.getCompound("info")), conversions);
//
//		Map<ItemType,Long> items = pocket.getItemsMap();
//		Map<UUID, CraftingPatternOld> patterns = pocket.getPatternsMap();
//		Map<ItemType,Optional<UUID>> defaultPatterns = pocket.getDefaultPatternsMap();
//
//		for (Tag itemCount : saved.getList("itemCounts", 10)) {
//			ItemType type = ItemType.load(((CompoundTag) itemCount).getCompound("item"));
//			long count = ((CompoundTag)itemCount).getLong("count");
//			if (count == 0)
//				continue;
//			items.put(type, count < 0 ? -1 : count);
//		}
//		conversions.convertMap(items);
//		for (Tag savedPattern : saved.getList("patterns", 10)) {
//			CraftingPatternOld pattern = loadPattern((CompoundTag)savedPattern);
//			if (pattern != null)
//				patterns.put(pattern.getPatternId(), pattern);
//		}
//		for (Tag savedDefaultPattern : saved.getList("defaultPatterns", 10)) {
//			ItemType type = ItemType.load(((CompoundTag) savedDefaultPattern).getCompound("item"));
//			Optional<UUID> patternId;
//			try {
//				patternId = Optional.of(((CompoundTag) savedDefaultPattern).getUUID("pattern"));
//			} catch (Exception e) {
//				patternId = Optional.empty();
//			}
//			defaultPatterns.put(type, patternId);
//		}
//
//		loadPManager(pocket.getProcesses(), saved.getList("processes", 10));
//
//		if (!allowPublicPocket) {
//			PocketInfo info = pocket.getInfo();
//			if (info.securityMode == PocketSecurityMode.PUBLIC) {
//				info.securityMode = PocketSecurityMode.TEAM;
//				pocket.setInfo(info);
//			}
//		}
//
//		return pocket;
//	}

//	private @Nullable CraftingPatternOld loadPattern(CompoundTag saved) {
//		try {
//			WorldCraftingPatternOld pattern = new WorldCraftingPatternOld(saved, server);
//			if (pattern.getLevel().getBlockEntity(pattern.getPos()) instanceof PatternSupportedBlockEntity entity && entity.containsPattern(pattern.getPatternId()))
//				return pattern;
//		} catch (Exception ignored) {}
//		return null;
//	}

	private Knowledge loadKnowledge(CompoundTag saved) {
		Knowledge knowledge = helper.createKnowledge(conversions);
		knowledge.add(loadElementArray(saved.getList("items", 10)));
		return knowledge;
	}

//	private void loadPManager(PocketProcessManager manager, ListTag saved) {
//		for (Tag tag : saved)
//			if (tag instanceof CompoundTag savedUnit)
//				loadPUnit(manager, savedUnit);
//	}
//
//	private void loadPUnit(PocketProcessManager manager, CompoundTag saved) {
//		ItemType[] types = loadTypeArray(saved.getList("types", 10));
//		PocketProcessUnit unit = manager.addUnit(types);
//		unit.getResources().load(saved.getList("resources", 10));
//		long[] leftToProvide = saved.getLongArray("leftToProvide");
//		int leftToProvideLen = Math.min(leftToProvide.length, types.length);
//		for (int i = 0; i < leftToProvideLen; i++)
//			unit.setLeftToProvide(i, leftToProvide[i]);
//		for (Tag tag : saved.getList("recipes", 10))
//			if (tag instanceof CompoundTag savedRecipes)
//				loadPRecipe(unit, savedRecipes);
//	}
//
//	private void loadPRecipe(PocketProcessUnit unit, CompoundTag saved) {
//		ItemType result = ItemType.load(saved.getCompound("result"));
//		ItemType[] types = loadTypeArray(saved.getList("types", 10));
//		PocketProcessRecipe recipe = unit.addRecipe(result, types);
//		recipe.getResources().load(saved.getList("resources", 10));
//		recipe.setLeftToCraft(saved.getLong("leftToCraft"));
//		for (Tag tag : saved.getList("crafters", 10))
//			if (tag instanceof CompoundTag savedCrafter)
//				loadPCrafter(recipe, savedCrafter);
//	}
//
//	private void loadPCrafter(PocketProcessRecipe recipe, CompoundTag saved) {
//		try {
//			PocketProcessCrafter crafter = recipe.addCrafter(saved.getUUID("patternId"));
//			crafter.getResources().load(saved.getList("resources", 10));
//		} catch (Exception ignored) {}
//	}
//
//	private ItemType[] loadTypeArray(ListTag saved) {
//		return saved.stream()
//						.map(tag->tag instanceof CompoundTag compound ? compound : null)
//						.filter(Objects::nonNull)
//						.map(ItemType::load)
//						.filter(type->!type.isEmpty())
//						.toArray(ItemType[]::new);
//	}

	private ElementType[] loadElementArray(ListTag saved) {
		return saved.stream()
						.map(tag->tag instanceof CompoundTag compound ? compound : null)
						.filter(Objects::nonNull)
						.map(ElementType::load)
						.filter(type->!type.isEmpty())
						.toArray(ElementType[]::new);
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

//	private static CompoundTag savePocket(ServerPocket pocket) {
//		CompoundTag saved = new CompoundTag();
//		saved.putUUID("pocketId", pocket.getPocketId());
//		saved.putUUID("owner", pocket.getOwner());
//		saved.put("info", PocketInfo.save(pocket.getInfo()));
//		ListTag itemCounts = new ListTag();
//		for (var entry : pocket.getItemsMap().entrySet()) {
//			CompoundTag itemCount = new CompoundTag();
//			itemCount.put("item", entry.getKey().save());
//			itemCount.putDouble("count", entry.getValue());
//			itemCounts.add(itemCount);
//		}
//		saved.put("itemCounts", itemCounts);
//		ListTag savedPatterns = new ListTag();
//		for (CraftingPatternOld pattern : pocket.getPatternsMap().values())
//			savedPatterns.add(pattern.save());
//		saved.put("patterns", savedPatterns);
//		saved.put("processes", savePManager(pocket.getProcesses()));
//		ListTag savedDefaultPatterns = new ListTag();
//		for (var entry : pocket.getDefaultPatternsMap().entrySet()) {
//			CompoundTag savedDefaultPattern = new CompoundTag();
//			savedDefaultPattern.put("item", entry.getKey().save());
//			if (entry.getValue().isPresent())
//				savedDefaultPattern.putUUID("pattern", entry.getValue().get());
//			savedDefaultPatterns.add(savedDefaultPattern);
//		}
//		saved.put("defaultPatterns", savedDefaultPatterns);
//		saved.put("crafters", savePManager(pocket.getProcesses()));
//		return saved;
//	}

	private static CompoundTag saveKnowledge(UUID playerId, Knowledge knowledge) {
		CompoundTag saved = new CompoundTag();
		saved.putUUID("player", playerId);
		saved.put("items", saveElementArray(knowledge.asSet().toArray(ElementType[]::new)));
		return saved;
	}

//	private static ListTag savePManager(PocketProcessManager manager) {
//		ListTag saved = new ListTag();
//		for (PocketProcessUnit unit : manager.getUnits())
//			saved.add(savePUnit(unit));
//		return saved;
//	}
//
//	private static CompoundTag savePUnit(PocketProcessUnit unit) {
//		CompoundTag saved = new CompoundTag();
//		saved.put("types", saveTypeArray(unit.getTypes()));
//		saved.put("resources", unit.getResources().save());
//		saved.putLongArray("leftToProvide", IntStream.range(0, unit.getTypeCount()).mapToLong(unit::getLeftToProvide).toArray());
//		ListTag savedRecipes = new ListTag();
//		for (PocketProcessRecipe recipe : unit.getRecipes())
//			savedRecipes.add(savePRecipe(recipe));
//		saved.put("recipes", savedRecipes);
//		return saved;
//	}
//
//	private static CompoundTag savePRecipe(PocketProcessRecipe recipe) {
//		CompoundTag saved = new CompoundTag();
//		saved.put("result", recipe.getResult().save());
//		saved.put("types", saveTypeArray(recipe.getResources().getTypes()));
//		saved.put("resources", recipe.getResources().save());
//		saved.putLong("leftToCraft", recipe.getLeftToCraft());
//		ListTag savedCrafters = new ListTag();
//		for (PocketProcessCrafter crafter : recipe.getCrafters())
//			savedCrafters.add(savePCrafter(crafter));
//		saved.put("crafters", savedCrafters);
//		return saved;
//	}
//
//	private static CompoundTag savePCrafter(PocketProcessCrafter crafter) {
//		CompoundTag saved = new CompoundTag();
//		saved.putUUID("patternId", crafter.getPatternId());
//		saved.put("resources", crafter.getResources().save());
//		return saved;
//	}
//
//	private static ListTag saveTypeArray(ItemType[] types) {
//		ListTag saved = new ListTag();
//		for (ItemType item : types)
//			saved.add(item.save());
//		return saved;
//	}

	private static ListTag saveElementArray(ElementType[] types) {
		ListTag saved = new ListTag();
		for (ElementType type : types)
			saved.add(ElementType.save(type));
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
		if (changed)
			DeepPocketPacketHandler.cbDestroyPocket(PacketDistributor.ALL.noArg(), pocketId);
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
		open(player, pocketId, pocket -> (int id, Inventory inv, Player player0)-> new PocketMenu(id, inv, pocket));
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

		//==================
		// Adding knowledge
		//==================

		for (ServerPlayer player : onlinePLayers) {
			Knowledge knowledge = getKnowledge(player.getUUID());
			for (ItemStack item : player.getInventory().items)
				knowledge.add(ElementType.item(item));
			knowledge.add(ElementType.item(player.containerMenu.getCarried()));
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
