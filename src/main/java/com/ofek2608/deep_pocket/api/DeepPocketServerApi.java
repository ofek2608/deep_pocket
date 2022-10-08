package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.impl.DeepPocketManager;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.UUID;

public interface DeepPocketServerApi extends DeepPocketApi {
	static @Nullable DeepPocketServerApi get() { return DeepPocketManager.getServerApi(); }

	PlayerKnowledge getKnowledge(UUID playerId);

	void openPocket(ServerPlayer player, UUID pocketId);
	void selectPocketFor(ServerPlayer player, UUID pocketId);
	void createPocketFor(ServerPlayer player, String name, ItemType icon, int color, PocketSecurityMode team);
	void changePocketSettingsFrom(ServerPlayer player, UUID pocketId, String name, ItemType icon, int color, PocketSecurityMode securityMode);
	void destroyPocketFor(ServerPlayer player, UUID pocketId);
}
