package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.*;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.ItemValue;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.UnmodifiableView;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

abstract class DeepPocketApiImpl implements DeepPocketApi {
	private final Map<ItemType, ItemValue> itemValues = new HashMap<>();
	protected final Map<UUID, PocketImpl> pockets = new HashMap<>();
	protected final Map<UUID, String> playerNameCache = new HashMap<>();

	@Override
	public @Nullable ItemValue getItemValue(ItemType type) {
		return itemValues.get(type);
	}

	@Override
	public @UnmodifiableView Map<ItemType, ItemValue> getItemValues() {
		return Collections.unmodifiableMap(itemValues);
	}

	@Override
	public void setItemValue(ItemType type, @Nullable ItemValue value) {
		if (value == null)
			itemValues.remove(type);
		else
			itemValues.put(type, value);
	}

	@Override
	public void clearItemValues() {
		itemValues.clear();
	}

	@Override
	public Map<UUID, Pocket> getPockets() {
		return Collections.unmodifiableMap(pockets);
	}

	public @Nullable PocketImpl getPocket(UUID pocketId) {
		return pockets.get(pocketId);
	}

	public @Nullable PocketImpl createPocket(UUID pocketId, UUID owner) {
		if (pockets.containsKey(pocketId))
			return null;
		PocketImpl newPocket = generatePocket(pocketId, owner);
		pockets.put(pocketId, newPocket);
		return newPocket;
	}

	public PocketImpl getOrCreatePocket(UUID pocketId, UUID owner) {
		return pockets.computeIfAbsent(pocketId, id->generatePocket(id, owner));
	}

	@Override
	public void destroyPocket(UUID pocketId) {
		pockets.remove(pocketId);
	}

	@Override
	public void clearPockets() {
		if (pockets.isEmpty())
			return;
		pockets.clear();
	}

	abstract PocketImpl generatePocket(UUID pocketId, UUID owner);

	@Override
	public boolean cachePlayerName(UUID id, String name) {
		if (name.length() > 16)
			throw new IllegalArgumentException("name");
		String oldName = playerNameCache.put(id, name);
		return !name.equals(oldName);
	}

	@Override
	public String getCachedPlayerName(UUID id) {
		String result = playerNameCache.get(id);
		return result == null ? "" + id : result;
	}

	@Override
	public @UnmodifiableView Map<UUID, String> getPlayerNameCache() {
		return Collections.unmodifiableMap(playerNameCache);
	}

	@Override
	public void insertItem(Pocket pocket, ItemStack stack) {
		if (stack.isEmpty())
			return;
		PlayerKnowledge knowledge = this instanceof DeepPocketServerApi server ? server.getKnowledge(pocket.getOwner()) : null;
		ItemType type = new ItemType(stack);
		ItemValue value = getItemValue(type);
		if (knowledge != null) knowledge.add(type);
		if (value == null) {
			pocket.addCount(type, stack.getCount());
			return;
		}
		value.items.forEach((valueItem,valueCount) -> pocket.addCount(valueItem, valueCount * stack.getCount()));
		if (knowledge != null) knowledge.add(value.items.keySet().toArray(new ItemType[0]));
	}

	@Override
	public ItemStack extractItem(Pocket pocket, ItemStack stack) {
		ItemType type = new ItemType(stack);
		double count = pocket.getCount(type);
		double newCount = count - stack.getCount();
		if (newCount >= 0) {
			pocket.setCount(type, newCount);
			return stack;
		}

		ItemValue value = getItemValue(type);
		if (value == null) {
			int extractCount = (int)count;
			pocket.setCount(type, Math.max(count - extractCount, 0));//max just in case
			return type.create(extractCount);
		}
		double maxCraft = getMaxCraft(pocket, value, 1-newCount);
		int extractCount = maxCraft >= -newCount ? stack.getCount() : (int)(count + maxCraft);
		double craftCount = extractCount - count;
		pocket.setCount(type, 0);
		for (var entry : value.items.entrySet())
			pocket.setCount(entry.getKey(), Math.max(pocket.getCount(entry.getKey()) - entry.getValue() * craftCount, 0));//max just in case
		return type.create(extractCount);
	}

	@Override
	public double getMaxExtract(Pocket pocket, ItemType type) {
		double maxExtract = pocket.getCount(type);
		ItemValue value = getItemValue(type);
		if (value != null)
			maxExtract += getMaxCraft(pocket, value, Double.POSITIVE_INFINITY);
		return maxExtract;
	}

	protected static double getMaxCraft(Pocket pocket, ItemValue value, double limit) {
		for (var entry : value.items.entrySet())
			limit = Math.min(limit, pocket.getCount(entry.getKey()) / entry.getValue());
		return limit;
	}
}
