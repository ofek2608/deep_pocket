package com.ofek2608.deep_pocket.api.events;

import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerLifecycleEvent;

public class DeepPocketServerStartedEvent extends ServerLifecycleEvent {
	private final DeepPocketServerApi api;

	public DeepPocketServerStartedEvent(MinecraftServer server, DeepPocketServerApi api) {
		super(server);
		this.api = api;
	}

	public DeepPocketServerApi getApi() {
		return api;
	}
}
