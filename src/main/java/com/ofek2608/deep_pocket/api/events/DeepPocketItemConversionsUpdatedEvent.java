package com.ofek2608.deep_pocket.api.events;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.ItemConversions;
import net.minecraftforge.eventbus.api.Event;

public class DeepPocketItemConversionsUpdatedEvent extends Event {
	private final DeepPocketClientApi api;
	private final ItemConversions conversions;

	public DeepPocketItemConversionsUpdatedEvent(DeepPocketClientApi api, ItemConversions conversions) {
		this.api = api;
		this.conversions = conversions;
	}

	public DeepPocketClientApi getApi() {
		return api;
	}

	public ItemConversions getConversions() {
		return conversions;
	}
}
