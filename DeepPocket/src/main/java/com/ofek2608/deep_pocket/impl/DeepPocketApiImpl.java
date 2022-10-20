package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.*;
import com.ofek2608.deep_pocket.api.struct.*;
import org.jetbrains.annotations.UnmodifiableView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

abstract class DeepPocketApiImpl<Helper extends DeepPocketHelper> implements DeepPocketApi {
	protected final Helper helper;
	protected @Nonnull ItemConversions conversions = ItemConversions.EMPTY;
	protected final Map<UUID, Pocket.Snapshot> pocketSnapshots = new HashMap<>();
	protected final Map<UUID, String> playerNameCache = new HashMap<>();

	protected DeepPocketApiImpl(Helper helper) {
		this.helper = helper;
	}

	@Override
	public Helper getHelper() {
		return helper;
	}

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
		Pocket newPocket = helper.createPocket(conversions, pocketId, owner, info);
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
}
