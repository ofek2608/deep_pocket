package com.ofek2608.deep_pocket.impl;

import com.mojang.logging.LogUtils;
import com.ofek2608.deep_pocket.api.*;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessCrafter;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessManager;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessRecipe;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessUnit;
import com.ofek2608.deep_pocket.api.struct.*;
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
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.ofek2608.deep_pocket.utils.AdvancedLongMath.*;

final class DeepPocketServerApiImpl extends DeepPocketApiImpl<DeepPocketHelper> implements DeepPocketServerApi {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final MinecraftServer server;
	private final Map<ServerPlayer, Set<UUID>> viewedPockets = new HashMap<>();
	private final Map<UUID, Knowledge.Snapshot> knowledge = new HashMap<>();
	private final Map<UUID, Knowledge0.Snapshot> knowledge0 = new HashMap<>();

	DeepPocketServerApiImpl(DeepPocketHelper helper, MinecraftServer server, ItemConversions conversions) {
		super(helper);
		this.server = server;
		this.conversions = conversions;
	}

	DeepPocketServerApiImpl(DeepPocketHelper helper, MinecraftServer server, ItemConversions conversions, CompoundTag tag) {
		this(helper, server, conversions);
		boolean errors = false;
		// Loading: Pockets
		boolean allowPublicPockets = DeepPocketConfig.Common.ALLOW_PUBLIC_POCKETS.get();
		for (Tag savedPocket : tag.getList("pockets", 10)) {
			try {
				Pocket readPocket = loadPocket(allowPublicPockets, (CompoundTag)savedPocket);
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

	private Pocket loadPocket(boolean allowPublicPocket, CompoundTag saved) {
		Pocket pocket = helper.createPocket(conversions, conversions0, saved.getUUID("pocketId"), saved.getUUID("owner"), new PocketInfo(saved.getCompound("info")));

		Map<ItemType,Long> items = pocket.getItemsMap();
		Map<UUID,CraftingPattern> patterns = pocket.getPatternsMap();
		Map<ItemType,Optional<UUID>> defaultPatterns = pocket.getDefaultPatternsMap();

		for (Tag itemCount : saved.getList("itemCounts", 10)) {
			ItemType type = ItemType.load(((CompoundTag) itemCount).getCompound("item"));
			long count = ((CompoundTag)itemCount).getLong("count");
			if (count == 0)
				continue;
			items.put(type, count < 0 ? -1 : count);
		}
		conversions.convertMap(items);
		for (Tag savedPattern : saved.getList("patterns", 10)) {
			CraftingPattern pattern = loadPattern((CompoundTag)savedPattern);
			if (pattern != null)
				patterns.put(pattern.getPatternId(), pattern);
		}
		for (Tag savedDefaultPattern : saved.getList("defaultPatterns", 10)) {
			ItemType type = ItemType.load(((CompoundTag) savedDefaultPattern).getCompound("item"));
			Optional<UUID> patternId;
			try {
				patternId = Optional.of(((CompoundTag) savedDefaultPattern).getUUID("pattern"));
			} catch (Exception e) {
				patternId = Optional.empty();
			}
			defaultPatterns.put(type, patternId);
		}

		loadPManager(pocket.getProcesses(), saved.getList("processes", 10));

		if (!allowPublicPocket) {
			PocketInfo info = pocket.getInfo();
			if (info.securityMode == PocketSecurityMode.PUBLIC) {
				info.securityMode = PocketSecurityMode.TEAM;
				pocket.setInfo(info);
			}
		}

		return pocket;
	}

	private @Nullable CraftingPattern loadPattern(CompoundTag saved) {
		try {
			WorldCraftingPattern pattern = new WorldCraftingPattern(saved, server);
			if (pattern.getLevel().getBlockEntity(pattern.getPos()) instanceof PatternSupportedBlockEntity entity && entity.containsPattern(pattern.getPatternId()))
				return pattern;
		} catch (Exception ignored) {}
		return null;
	}

	private Knowledge loadKnowledge(CompoundTag saved) {
		Knowledge knowledge = helper.createKnowledge(conversions);
		knowledge.add(loadTypeArray(saved.getList("items", 10)));
		return knowledge;
	}

	private void loadPManager(PocketProcessManager manager, ListTag saved) {
		for (Tag tag : saved)
			if (tag instanceof CompoundTag savedUnit)
				loadPUnit(manager, savedUnit);
	}

	private void loadPUnit(PocketProcessManager manager, CompoundTag saved) {
		ItemType[] types = loadTypeArray(saved.getList("types", 10));
		PocketProcessUnit unit = manager.addUnit(types);
		unit.getResources().load(saved.getList("resources", 10));
		long[] leftToProvide = saved.getLongArray("leftToProvide");
		int leftToProvideLen = Math.min(leftToProvide.length, types.length);
		for (int i = 0; i < leftToProvideLen; i++)
			unit.setLeftToProvide(i, leftToProvide[i]);
		for (Tag tag : saved.getList("recipes", 10))
			if (tag instanceof CompoundTag savedRecipes)
				loadPRecipe(unit, savedRecipes);
	}

	private void loadPRecipe(PocketProcessUnit unit, CompoundTag saved) {
		ItemType result = ItemType.load(saved.getCompound("result"));
		ItemType[] types = loadTypeArray(saved.getList("types", 10));
		PocketProcessRecipe recipe = unit.addRecipe(result, types);
		recipe.getResources().load(saved.getList("resources", 10));
		recipe.setLeftToCraft(saved.getLong("leftToCraft"));
		for (Tag tag : saved.getList("crafters", 10))
			if (tag instanceof CompoundTag savedCrafter)
				loadPCrafter(recipe, savedCrafter);
	}

	private void loadPCrafter(PocketProcessRecipe recipe, CompoundTag saved) {
		try {
			PocketProcessCrafter crafter = recipe.addCrafter(saved.getUUID("patternId"));
			crafter.getResources().load(saved.getList("resources", 10));
		} catch (Exception ignored) {}
	}

	private ItemType[] loadTypeArray(ListTag saved) {
		return saved.stream()
						.map(tag->tag instanceof CompoundTag compound ? compound : null)
						.filter(Objects::nonNull)
						.map(ItemType::load)
						.filter(type->!type.isEmpty())
						.toArray(ItemType[]::new);
	}







	public CompoundTag save(CompoundTag tag) {
		// Saving: Pockets
		ListTag savedPockets = new ListTag();
		getPockets().map(DeepPocketServerApiImpl::savePocket).forEach(savedPockets::add);
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
		knowledge.forEach((playerId,playerKnowledge) -> savedKnowledge.add(saveKnowledge(playerId, playerKnowledge.getKnowledge())));
		tag.put("knowledge", savedKnowledge);
		return tag;
	}

	private static CompoundTag savePocket(Pocket pocket) {
		CompoundTag saved = new CompoundTag();
		saved.putUUID("pocketId", pocket.getPocketId());
		saved.putUUID("owner", pocket.getOwner());
		saved.put("info", pocket.getInfo().save());
		ListTag itemCounts = new ListTag();
		for (var entry : pocket.getItemsMap().entrySet()) {
			CompoundTag itemCount = new CompoundTag();
			itemCount.put("item", entry.getKey().save());
			itemCount.putDouble("count", entry.getValue());
			itemCounts.add(itemCount);
		}
		saved.put("itemCounts", itemCounts);
		ListTag savedPatterns = new ListTag();
		for (CraftingPattern pattern : pocket.getPatternsMap().values())
			savedPatterns.add(pattern.save());
		saved.put("patterns", savedPatterns);
		saved.put("processes", savePManager(pocket.getProcesses()));
		ListTag savedDefaultPatterns = new ListTag();
		for (var entry : pocket.getDefaultPatternsMap().entrySet()) {
			CompoundTag savedDefaultPattern = new CompoundTag();
			savedDefaultPattern.put("item", entry.getKey().save());
			if (entry.getValue().isPresent())
				savedDefaultPattern.putUUID("pattern", entry.getValue().get());
			savedDefaultPatterns.add(savedDefaultPattern);
		}
		saved.put("defaultPatterns", savedDefaultPatterns);
		saved.put("crafters", savePManager(pocket.getProcesses()));
		return saved;
	}

	private static CompoundTag saveKnowledge(UUID playerId, Knowledge knowledge) {
		CompoundTag saved = new CompoundTag();
		saved.putUUID("player", playerId);
		saved.put("items", saveTypeArray(knowledge.asSet().toArray(ItemType[]::new)));
		return saved;
	}

	private static ListTag savePManager(PocketProcessManager manager) {
		ListTag saved = new ListTag();
		for (PocketProcessUnit unit : manager.getUnits())
			saved.add(savePUnit(unit));
		return saved;
	}

	private static CompoundTag savePUnit(PocketProcessUnit unit) {
		CompoundTag saved = new CompoundTag();
		saved.put("types", saveTypeArray(unit.getTypes()));
		saved.put("resources", unit.getResources().save());
		saved.putLongArray("leftToProvide", IntStream.range(0, unit.getTypeCount()).mapToLong(unit::getLeftToProvide).toArray());
		ListTag savedRecipes = new ListTag();
		for (PocketProcessRecipe recipe : unit.getRecipes())
			savedRecipes.add(savePRecipe(recipe));
		saved.put("recipes", savedRecipes);
		return saved;
	}

	private static CompoundTag savePRecipe(PocketProcessRecipe recipe) {
		CompoundTag saved = new CompoundTag();
		saved.put("result", recipe.getResult().save());
		saved.put("types", saveTypeArray(recipe.getResources().getTypes()));
		saved.put("resources", recipe.getResources().save());
		saved.putLong("leftToCraft", recipe.getLeftToCraft());
		ListTag savedCrafters = new ListTag();
		for (PocketProcessCrafter crafter : recipe.getCrafters())
			savedCrafters.add(savePCrafter(crafter));
		saved.put("crafters", savedCrafters);
		return saved;
	}

	private static CompoundTag savePCrafter(PocketProcessCrafter crafter) {
		CompoundTag saved = new CompoundTag();
		saved.putUUID("patternId", crafter.getPatternId());
		saved.put("resources", crafter.getResources().save());
		return saved;
	}

	private static ListTag saveTypeArray(ItemType[] types) {
		ListTag saved = new ListTag();
		for (ItemType item : types)
			saved.add(item.save());
		return saved;
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
	public Knowledge getKnowledge(UUID playerId) {
		return knowledge.computeIfAbsent(playerId, id->helper.createKnowledge(conversions).createSnapshot()).getKnowledge();
	}

	@Override
	public Knowledge0 getKnowledge0(UUID playerId) {
		return knowledge0.computeIfAbsent(playerId, id->helper.createKnowledge(conversions0).createSnapshot()).getKnowledge();
	}

	private void open(ServerPlayer player, UUID pocketId, Function<Pocket, MenuConstructor> menu) {
		Pocket pocket = getPocket(pocketId);
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
		if (info.securityMode == PocketSecurityMode.PUBLIC && !DeepPocketConfig.Common.ALLOW_PUBLIC_POCKETS.get())
			info.securityMode = PocketSecurityMode.TEAM;
		Pocket newPocket;
		do {
			newPocket = createPocket(UUID.randomUUID(), player.getUUID(), info);
		} while (newPocket == null);
		player.setItemInHand(hand, PocketItem.createStack(newPocket.getPocketId()));
	}

	@Override
	public void changePocketSettingsFrom(ServerPlayer player, UUID pocketId, PocketInfo info) {
		if (info.securityMode == PocketSecurityMode.PUBLIC && !DeepPocketConfig.Common.ALLOW_PUBLIC_POCKETS.get())
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

	@Override
	public void requestProcessFor(ServerPlayer player, UUID pocketId, RecipeRequest[] requests) {
		if (requests.length == 0)
			return;
		for (RecipeRequest request : requests)
			if (request.getPatternsCount() == 0)
				return;
		Pocket pocket = getPocket(pocketId);
		if (pocket == null || !pocket.canAccess(player))
			return;

		Map<ItemType,Long> unitRequirements = new HashMap<>();
		List<ItemType[]> recipeRequirements = new ArrayList<>();
		for (RecipeRequest request : requests) {
			UUID patternId = request.getPattern(0);
			CraftingPattern pattern = pocket.getPattern(patternId);
			if (pattern == null)
				return;
			var inputCountMap = pattern.getInputCountMap();
			for (var entry : inputCountMap.entrySet()) {
				long add = advancedMul(entry.getValue(), request.getAmount());
				unitRequirements.compute(entry.getKey(), (t, oldAmount) -> oldAmount == null ? add : advancedSum(oldAmount, add));
			}
			recipeRequirements.add(inputCountMap.keySet().toArray(ItemType[]::new));
		}
		ItemType[] types = unitRequirements.keySet().toArray(ItemType[]::new);
		PocketProcessUnit unit = pocket.getProcesses().addUnit(types);
		for (int i = 0; i < types.length; i++)
			unit.setLeftToProvide(i, unitRequirements.get(types[i]));
		for (int i = 0; i < requests.length; i++) {
			RecipeRequest request = requests[i];
			PocketProcessRecipe recipe = unit.addRecipe(request.getResult(), recipeRequirements.get(i));
			recipe.setLeftToCraft(request.getAmount());
			List.of(request.getPatterns()).forEach(recipe::addCrafter);
		}

		for (ItemType type : types)
			pocket.getItemsMap().computeIfPresent(type, (t,count) -> unit.supplyItem(type, count));
	}


	private Knowledge.Snapshot getKnowledgeSnapshot(UUID playerId) {
		Knowledge.Snapshot snapshot = knowledge.get(playerId);
		if (snapshot == null) {
			snapshot = helper.createKnowledge(conversions).createSnapshot();
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

		//==============
		// Recipes tick
		//==============
		for (Pocket pocket : pockets)
			pocket.getProcesses().executeCrafters(pocket);

		//==================
		// Adding knowledge
		//==================

		for (ServerPlayer player : onlinePLayers) {
			Knowledge knowledge = getKnowledge(player.getUUID());
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
				Knowledge.Snapshot snapshot = getKnowledgeSnapshot(player.getUUID());
				ItemType[] removed = snapshot.getRemoved();
				ItemType[] added = snapshot.getAdded();
				if (removed.length > 0)
					DeepPocketPacketHandler.cbKnowledgeRem(playerTarget, removed);
				if (added.length > 0)
					DeepPocketPacketHandler.cbKnowledgeAdd(playerTarget, false, added);
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
				DeepPocketPacketHandler.cbKnowledgeAdd(PacketDistributor.PLAYER.with(() -> player), true, getKnowledgeSnapshot(player.getUUID()).getKnowledge().asSet().toArray(new ItemType[0]));
			//pockets
			DeepPocketPacketHandler.cbClearPockets(packetTarget);
			for (Pocket pocket : pockets)
				DeepPocketPacketHandler.cbCreatePocket(packetTarget, pocket.getPocketId(), pocket.getOwner(), pocket.getInfo());
		}
		//Pocket Update
		for (var entry : toSendPocketUpdate.entrySet()) {
			Pocket.Snapshot snapshot = entry.getKey();
			Pocket pocket = snapshot.getPocket();
			UUID pocketId = pocket.getPocketId();
			PacketDistributor.PacketTarget packetTarget = PacketDistributor.NMLIST.with(entry::getValue);
			
			//Changed elements
			PocketContent.Snapshot pocketSnapshot = snapshot.getContentSnapshot();
			PocketContent content = pocket.getContent();
			int contentSize = content.getSize();
			
			Set<Integer> changedTypeSet = pocketSnapshot.getChangedTypes();
			Set<Integer> changedCountSet = pocketSnapshot.getChangedCount();
			
			int[] changedTypeIndexes = changedTypeSet.stream().mapToInt(Integer::intValue).filter(i -> i < contentSize).sorted().toArray();
			ElementType[] changedType = IntStream.of(changedTypeIndexes).mapToObj(content::getType).toArray(ElementType[]::new);
			long[] changedTypeCount = IntStream.of(changedTypeIndexes).mapToLong(content::getCount).toArray();
			
			int[] changedCountIndexes = changedCountSet.stream().mapToInt(Integer::intValue).filter(i -> i < contentSize).filter(i -> !changedTypeSet.contains(i)).sorted().toArray();
			long[] changedCount = IntStream.of(changedCountIndexes).mapToLong(content::getCount).toArray();
			
			DeepPocketPacketHandler.cbPocketContentUpdate(packetTarget, pocketId, contentSize, changedTypeIndexes, changedType, changedTypeCount, changedCountIndexes, changedCount);
			
			//Update patterns
			CraftingPattern[] addedPatterns = snapshot.getAddedPatterns();
			UUID[] removedPatterns = snapshot.getRemovedPatterns();
			if (addedPatterns.length > 0 || removedPatterns.length > 0)
				DeepPocketPacketHandler.cbUpdatePatterns(packetTarget, pocketId, addedPatterns, removedPatterns);
			
			//Update default patterns
			var addedDefaultPatterns = snapshot.getAddedDefaultPatterns();
			var removedDefaultPatterns = snapshot.getRemovedDefaultPatterns();
			if (addedDefaultPatterns.size() > 0 || removedDefaultPatterns.length > 0)
				DeepPocketPacketHandler.cbUpdateDefaultPatterns(packetTarget, pocketId, addedDefaultPatterns, removedDefaultPatterns);

		}
		//Pocket Clear
		for (var entry : toSendPocketClear.entrySet()) {
			PacketDistributor.PacketTarget packetTarget = PacketDistributor.NMLIST.with(entry::getValue);
			Pocket.Snapshot pocketSnapshot = entry.getKey();
			Pocket pocket = pocketSnapshot.getPocket();
			UUID pocketId = pocket.getPocketId();
			DeepPocketPacketHandler.cbPocketClearElements(packetTarget, pocketId);
			UUID[] patternsToRemove = Stream.concat(
							pocket.getPatternsMap().keySet().stream(),
							Stream.of(pocketSnapshot.getRemovedPatterns())
			).toArray(UUID[]::new);
			if (patternsToRemove.length > 0)
				DeepPocketPacketHandler.cbUpdatePatterns(packetTarget, pocketId, new CraftingPattern[0], patternsToRemove);
			ItemType[] defaultPatternsToRemove = Stream.concat(
							pocket.getDefaultPatternsMap().keySet().stream(),
							Stream.of(pocketSnapshot.getRemovedDefaultPatterns())
			).toArray(ItemType[]::new);
			if (defaultPatternsToRemove.length > 0)
				DeepPocketPacketHandler.cbUpdateDefaultPatterns(packetTarget, pocketId, Collections.emptyMap(), defaultPatternsToRemove);
		}
		//Pocket Fill
		for (var entry : toSendPocketFill.entrySet()) {
			PacketDistributor.PacketTarget packetTarget = PacketDistributor.NMLIST.with(entry::getValue);
			Pocket pocket = entry.getKey();
			UUID pocketId = pocket.getPocketId();
			
			PocketContent content = pocket.getContent();
			int contentSize = content.getSize();
			DeepPocketPacketHandler.cbPocketContentUpdate(
					packetTarget, pocketId,
					contentSize,
					IntStream.range(0, contentSize).toArray(),
					IntStream.range(0, contentSize).mapToObj(content::getType).toArray(ElementType[]::new),
					IntStream.range(0, contentSize).mapToLong(content::getCount).toArray(),
					new int[0],
					new long[0]
			);
			
			CraftingPattern[] patterns = pocket.getPatternsMap().values().toArray(CraftingPattern[]::new);
			if (patterns.length > 0)
				DeepPocketPacketHandler.cbUpdatePatterns(packetTarget, pocketId, patterns, new UUID[0]);
			var defaultPatterns = pocket.getDefaultPatternsMap();
			if (defaultPatterns.size() > 0)
				DeepPocketPacketHandler.cbUpdateDefaultPatterns(packetTarget, pocketId, defaultPatterns, new ItemType[0]);
		}

		//Data for menus
		for (ServerPlayer players : onlinePLayers)
			if (players.containerMenu instanceof ProcessMenu menu)
				menu.sendUpdate();

		//======
		// Post
		//======

		for (ServerPlayer newPlayer : newPlayers)
			cachePlayerName(newPlayer.getUUID(), newPlayer.getGameProfile().getName());

		this.pocketSnapshots.replaceAll((pocketId, pocketSnapshot)->pocketSnapshot.getPocket().createSnapshot());
	}
}
