package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.*;
import com.ofek2608.deep_pocket.api.struct.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.UnmodifiableView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

abstract class DeepPocketApiImpl implements DeepPocketApi {
	protected @Nonnull ItemConversions conversions = ItemConversions.EMPTY;
	protected final Map<UUID, Pocket.Snapshot> pocketSnapshots = new HashMap<>();
	protected final Map<UUID, String> playerNameCache = new HashMap<>();

	@Override
	public ItemConversions getItemConversions() {
		return conversions;
	}

	@Override
	public Stream<Pocket> getPockets() {
		return pocketSnapshots.values().stream().map(Pocket.Snapshot::getPocket);
	}

	public @Nullable Pocket getPocket(UUID pocketId) {
		Pocket.Snapshot snapshot = pocketSnapshots.get(pocketId);
		return snapshot == null ? null : snapshot.getPocket();
	}

	public @Nullable Pocket createPocket(UUID pocketId, UUID owner, PocketInfo info) {
		if (pocketSnapshots.containsKey(pocketId))
			return null;
		Pocket newPocket = new Pocket(conversions, pocketId, owner, info);
		pocketSnapshots.put(pocketId, newPocket.createSnapshot());
		return newPocket;
	}

	@Override
	public boolean destroyPocket(UUID pocketId) {
		return pocketSnapshots.remove(pocketId) != null;
	}

	@Override
	public void clearPockets() {
		if (pocketSnapshots.isEmpty())
			return;
		pocketSnapshots.clear();
	}

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
		insertItem(pocket, new ItemType(stack), stack.getCount());
	}

	@Override
	public void insertItem(Pocket pocket, ItemType type, long count) {
		if (type.isEmpty() || count == 0)
			return;
		pocket.insertItem(type, count);
//		if (!(this instanceof DeepPocketServerApi server))
//			return;
//		PlayerKnowledge knowledge = server.getKnowledge(pocket.getOwner());
//		knowledge.add(type);
//		long[] value = conversions.getValue(type);
//		if (value == null)
//			return;
//		ItemType[] baseItems = IntStream.range(0, value.length).filter(i->value[i] != 0).mapToObj(conversions::getBaseItem).toArray(ItemType[]::new);
//		knowledge.add(baseItems);
	}

	@Override
	public ItemStack extractItem(Pocket pocket, ItemStack stack) {
		ItemType type = new ItemType(stack);
		return type.create((int)pocket.extractItem(type, stack.getCount()));
	}

	@Override
	public long getMaxExtract(Pocket pocket, ItemType type) {
		return pocket.getMaxExtract(type);
	}
}
