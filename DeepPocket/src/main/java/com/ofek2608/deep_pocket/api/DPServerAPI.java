package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.pocket.ModifiablePocket;

import java.util.Optional;
import java.util.UUID;

public interface DPServerAPI {
	/**
	 * @return weather the server had already closed.
	 */
	boolean isValid();
	
	Optional<ModifiablePocket> getPocket(UUID pocketId);
	ModifiablePocket createPocket(UUID owner);
	void deletePocket(UUID pocketId);
	ServerConfig getServerConfig();
}
