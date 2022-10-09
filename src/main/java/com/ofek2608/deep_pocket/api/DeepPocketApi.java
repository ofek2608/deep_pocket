package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.struct.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.UnmodifiableView;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public interface DeepPocketApi {
	public static @Nullable DeepPocketApi get(boolean isClientSide) { return isClientSide ? DeepPocketClientApi.get() : DeepPocketServerApi.get(); }
	public static @Nullable DeepPocketApi get(Level level) { return get(level.isClientSide); }

	ItemConversions getItemConversions();

	Stream<Pocket> getPockets();
	@Nullable
	Pocket getPocket(UUID pocketId);
	@Nullable
	Pocket createPocket(UUID pocketId, UUID owner, PocketInfo info);
	boolean destroyPocket(UUID pocketId);
	void clearPockets();

	boolean cachePlayerName(UUID id, String name);
	String getCachedPlayerName(UUID id);
	@UnmodifiableView Map<UUID, String> getPlayerNameCache();
}
