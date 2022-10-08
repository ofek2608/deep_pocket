package com.ofek2608.deep_pocket.api.events;

import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import net.minecraftforge.eventbus.api.Event;

public class DeepPocketServerStartedEvent extends Event {
	private final DeepPocketServerApi api;

	public DeepPocketServerStartedEvent(DeepPocketServerApi api) {
		this.api = api;
	}

	public DeepPocketServerApi getApi() {
		return api;
	}
}
