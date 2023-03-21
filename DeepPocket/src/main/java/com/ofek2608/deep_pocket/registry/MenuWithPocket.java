package com.ofek2608.deep_pocket.registry;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.struct.client.ClientPocket;
import com.ofek2608.deep_pocket.api.struct.server.ServerPocket;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface MenuWithPocket {
	Player getPocketAccessor();
	UUID getPocketId();
	void setPocketId(UUID pocketId);
	
	default @Nullable ServerPocket getServerPocket() {
		DeepPocketServerApi api = DeepPocketServerApi.get();
		if (api == null) {
			return null;
		}
		ServerPocket pocket = api.getPocket(getPocketId());
		if (pocket == null || !pocket.canAccess(getPocketAccessor())) {
			return null;
		}
		return pocket;
	}
	
	default @Nullable ClientPocket getClientPocket() {
		return DeepPocketClientApi.get().getPocket(getPocketId());
	}
}
