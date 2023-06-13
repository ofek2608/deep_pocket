package com.ofek2608.deep_pocket.api.events;

import com.ofek2608.deep_pocket.api.DPClientAPI;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class DPClientAPIEvent extends Event {
	@Nullable
	private final DPClientAPI api;
	
	@ApiStatus.Internal
	public DPClientAPIEvent(@Nullable DPClientAPI api) {
		this.api = api;
	}
	
	public Optional<DPClientAPI> getApi() {
		return Optional.ofNullable(api);
	}
}
