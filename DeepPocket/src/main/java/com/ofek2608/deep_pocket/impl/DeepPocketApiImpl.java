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

abstract class DeepPocketApiImpl<Helper extends DeepPocketHelper, Pocket extends PocketBase> implements DeepPocketApi<Pocket> {
	protected final Helper helper;
	protected @Nonnull ElementConversions conversions = ElementConversions.EMPTY;
	protected final Map<UUID, Pocket> pockets = new HashMap<>();
	protected final Map<UUID, String> playerNameCache = new HashMap<>();

	protected DeepPocketApiImpl(Helper helper) {
		this.helper = helper;
	}

	@Override
	public Helper getHelper() {
		return helper;
	}
	
	@Override
	public ElementConversions getConversions() {
		return conversions;
	}
	
	@Override
	public Stream<Pocket> getPockets() {
		return pockets.values().stream();
	}

	public @Nullable Pocket getPocket(UUID pocketId) {
		return pockets.get(pocketId);
	}
	
	public abstract @Nullable Pocket createPocket(UUID pocketId, UUID owner, PocketInfo info);

	@Override
	public boolean destroyPocket(UUID pocketId) {
		return pockets.remove(pocketId) != null;
	}

	@Override
	public void clearPockets() {
		if (pockets.isEmpty())
			return;
		pockets.clear();
	}

	@Override
	public boolean cachePlayerName(UUID id, String name) {
		if (name.length() > 16)
			throw new IllegalArgumentException("name");
		String oldName = playerNameCache.put(id, name);
		return !name.equals(oldName);
	}

	public boolean hasCachedPlayerName(UUID id) {
		return playerNameCache.containsKey(id);
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
