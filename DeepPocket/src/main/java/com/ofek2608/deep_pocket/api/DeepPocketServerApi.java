package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.struct.PlayerKnowledge;
import com.ofek2608.deep_pocket.api.struct.PocketInfo;
import com.ofek2608.deep_pocket.impl.DeepPocketManager;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.UnmodifiableView;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

public interface DeepPocketServerApi extends DeepPocketApi {
	static @Nullable DeepPocketServerApi get() { return DeepPocketManager.getServerApi(); }

	@UnmodifiableView Set<UUID> getViewedPockets(ServerPlayer player);
	PlayerKnowledge getKnowledge(UUID playerId);

	void openPocket(ServerPlayer player, UUID pocketId);
	void selectPocketFor(ServerPlayer player, UUID pocketId);
	void createPocketFor(ServerPlayer player, PocketInfo info);
	void changePocketSettingsFrom(ServerPlayer player, UUID pocketId, PocketInfo info);
	void destroyPocketFor(ServerPlayer player, UUID pocketId);
}
