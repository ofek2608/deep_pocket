package com.ofek2608.deep_pocket.api.events;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.ElementConversions;
import net.minecraftforge.eventbus.api.Event;

public class DeepPocketConversionsUpdatedEvent extends Event {
	private final DeepPocketClientApi api;
	private final ElementConversions conversions;

	public DeepPocketConversionsUpdatedEvent(DeepPocketClientApi api, ElementConversions conversions) {
		this.api = api;
		this.conversions = conversions;
	}

	public DeepPocketClientApi getApi() {
		return api;
	}

	public ElementConversions getConversions() {
		return conversions;
	}
}
