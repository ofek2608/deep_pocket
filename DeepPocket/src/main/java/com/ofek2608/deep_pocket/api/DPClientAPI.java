package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.pocket.PocketProperties;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface DPClientAPI {
	/**
	 * @param pocketId the pocketId
	 * @return   the pocket properties corresponding to pocketId
	 * @see DPClientAPI#getKnownPockets()
	 */
	PocketProperties getProperties(UUID pocketId);
	
	/**
	 * @param pocketId the pocketId
	 * @return   the pocket corresponding to pocketId
	 * @see DPClientAPI#getVisiblePockets()
	 */
	Optional<Pocket> getPocket(UUID pocketId);
	
	/**
	 * @return a stream which contains all the valid keys for getProperties
	 * @see DPClientAPI#getProperties(UUID)
	 */
	Stream<UUID> getKnownPockets();
	
	/**
	 * @return a stream which contains all the valid keys for getPocket
	 * @see DPClientAPI#getPocket(UUID)
	 */
	Stream<UUID> getVisiblePockets();
}
