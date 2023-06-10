package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.pocket.ModifiablePocket;

import java.util.Optional;
import java.util.UUID;

public interface DPServerAPI {
	Optional<ModifiablePocket> getPocket(UUID pocketId);
	ModifiablePocket createPocket(UUID owner);
	void deletePocket(UUID pocketId);
}
