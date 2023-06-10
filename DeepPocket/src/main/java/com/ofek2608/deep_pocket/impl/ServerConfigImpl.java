package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.ServerConfig;
import net.minecraft.network.FriendlyByteBuf;

public record ServerConfigImpl(boolean allowPublicPockets, boolean requirePocketFactory) implements ServerConfig {
	public static final ServerConfigImpl DEFAULT = new ServerConfigImpl(true, true);
	
	public static void encode(ServerConfigImpl config, FriendlyByteBuf buf) {
		buf.writeBoolean(config.allowPublicPockets);
		buf.writeBoolean(config.requirePocketFactory);
	}
	
	public static ServerConfigImpl decode(FriendlyByteBuf buf) {
		return new ServerConfigImpl(
				buf.readBoolean(),
				buf.readBoolean()
		);
	}
}
