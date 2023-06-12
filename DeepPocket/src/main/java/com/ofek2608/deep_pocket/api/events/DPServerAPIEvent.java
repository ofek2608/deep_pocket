package com.ofek2608.deep_pocket.api.events;

import com.ofek2608.deep_pocket.api.DPServerAPI;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class DPServerAPIEvent extends Event {
	@Nullable
	private final DPServerAPI api;
	
	@ApiStatus.Internal
	public DPServerAPIEvent(@Nullable DPServerAPI api) {
		this.api = api;
	}
	
	public Optional<DPServerAPI> getApi() {
		return Optional.ofNullable(api);
	}
}
