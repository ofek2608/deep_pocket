package com.ofek2608.deep_pocket.impl;

import com.mojang.logging.LogUtils;
import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.ItemValue;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import com.ofek2608.deep_pocket.registry.items.PocketItem;
import com.ofek2608.deep_pocket.registry.pocket_screen.PocketMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class DeepPocketServerApiImpl extends DeepPocketApiImpl implements DeepPocketServerApi {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Map<UUID,PlayerKnowledgeImpl> knowledge = new HashMap<>();

	DeepPocketServerApiImpl() {}

	DeepPocketServerApiImpl(CompoundTag tag) {
		boolean errors = false;
		// Loading: Pockets
		boolean fixPublicPockets = !DeepPocketConfig.Common.ALLOW_PUBLIC_POCKETS.get();
		for (Tag savedPocket : tag.getList("pockets", 10)) {
			try {
				PocketImpl readPocket = new PocketImpl((CompoundTag)savedPocket);
				if (fixPublicPockets && readPocket.getSecurityMode() == PocketSecurityMode.PUBLIC)
					readPocket.setSecurityMode(PocketSecurityMode.TEAM);
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
				PlayerKnowledgeImpl readKnowledge = new PlayerKnowledgeImpl(this, (CompoundTag)savedKnowledge);
				knowledge.put(readKnowledge.player, readKnowledge);
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
		for (PocketImpl pocket : pockets.values())
			savedPockets.add(pocket.save());
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
		for (PlayerKnowledgeImpl knowledge : knowledge.values())
			savedKnowledge.add(knowledge.save());
		tag.put("knowledge", savedKnowledge);
		return tag;
	}

	@Override
	public void setItemValue(ItemType type, @Nullable ItemValue value) {
		super.setItemValue(type, value);
		if (value == null)
			DeepPocketPacketHandler.cbRemoveItemValue(PacketDistributor.ALL.noArg(), type);
		else
			DeepPocketPacketHandler.cbSetItemValue(PacketDistributor.ALL.noArg(), Map.of(type, value));
	}

	@Override
	public void clearItemValues() {
		super.clearItemValues();
		DeepPocketPacketHandler.cbClearItemValues(PacketDistributor.ALL.noArg());
	}

	@Override
	public void destroyPocket(UUID pocketId) {
		super.destroyPocket(pocketId);
		DeepPocketPacketHandler.cbDestroyPocket(PacketDistributor.ALL.noArg(), pocketId);
	}

	@Override
	public void clearPockets() {
		super.clearPockets();
		DeepPocketPacketHandler.cbClearPockets(PacketDistributor.ALL.noArg());
	}

	@Override
	PocketImpl generatePocket(UUID pocketId, UUID owner) {
		PocketImpl pocket = new PocketImpl(pocketId, owner, true);
		DeepPocketPacketHandler.cbCreatePocket(PacketDistributor.ALL.noArg(), pocketId, owner, pocket.getName(), pocket.getIcon(), pocket.getColor(), pocket.getSecurityMode());
		return pocket;
	}

	@Override
	public boolean cachePlayerName(UUID id, String name) {
		boolean changed = super.cachePlayerName(id, name);
		if (changed)
			DeepPocketPacketHandler.cbSetPlayersName(PacketDistributor.ALL.noArg(), Map.of(id, name));
		return changed;
	}

	@Override
	public PlayerKnowledgeImpl getKnowledge(UUID playerId) {
		return knowledge.computeIfAbsent(playerId, id->new PlayerKnowledgeImpl(this, id));
	}

	@Override
	public void openPocket(ServerPlayer player, UUID pocketId) {
		if (player.containerMenu != player.inventoryMenu)
			return;
		Pocket pocket = getPocket(pocketId);
		if (pocket == null)
			return;
		NetworkHooks.openScreen(player, PocketMenu.MENU_PROVIDER);
		if (player.containerMenu instanceof PocketMenu menu)
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
		PocketImpl pocket = getPocket(pocketId);
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
	public void createPocketFor(ServerPlayer player, String name, ItemType icon, int color, PocketSecurityMode securityMode) {
		InteractionHand hand = getHandForItem(player, DeepPocketRegistry.POCKET_FACTORY_ITEM.get());
		if (hand == null)
			return;
		if (securityMode == PocketSecurityMode.PUBLIC && DeepPocketConfig.Common.ALLOW_PUBLIC_POCKETS.get())
			securityMode = PocketSecurityMode.TEAM;
		PocketImpl newPocket;
		do {
			newPocket = createPocket(UUID.randomUUID(), player.getUUID());
		} while (newPocket == null);
		newPocket.setName(name);
		newPocket.setIcon(icon);
		newPocket.setColor(color);
		newPocket.setSecurityMode(securityMode);
		player.setItemInHand(hand, PocketItem.createStack(newPocket.getPocketId()));
	}

	@Override
	public void changePocketSettingsFrom(ServerPlayer player, UUID pocketId, String name, ItemType icon, int color, PocketSecurityMode securityMode) {
		if (securityMode == PocketSecurityMode.PUBLIC && DeepPocketConfig.Common.ALLOW_PUBLIC_POCKETS.get())
			securityMode = PocketSecurityMode.TEAM;
		Pocket pocket = getPocket(pocketId);
		if (pocket == null || !player.getUUID().equals(pocket.getOwner()))
			return;
		pocket.setName(name);
		pocket.setIcon(icon);
		pocket.setColor(color);
		pocket.setSecurityMode(securityMode);
	}

	@Override
	public void destroyPocketFor(ServerPlayer player, UUID pocketId) {
		Pocket pocket = getPocket(pocketId);
		if (pocket == null || !player.getUUID().equals(pocket.getOwner()))
			return;
		destroyPocket(pocketId);
	}

	void tickUpdate() {
		pockets.values().forEach(PocketImpl::sendUpdate);
	}
}
