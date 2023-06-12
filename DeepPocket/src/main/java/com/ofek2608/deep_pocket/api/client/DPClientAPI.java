package com.ofek2608.deep_pocket.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.api.ServerConfig;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.pocket.PocketProperties;
import com.ofek2608.deep_pocket.api.types.EntryStack;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface DPClientAPI {
	/**
	 * @return weather the client had already left the server.
	 */
	boolean isValid();
	
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
	
	/**
	 * The config defined on the server, may not be updated when the client just joins the world
	 * @return The server config
	 */
	ServerConfig getServerConfig();
	
	/**
	 * @param id the category id
	 * @param category the implementation of the category
	 * @see DPClientAPI#getEntryCategory(ResourceLocation)
	 */
	void setEntryCategory(ResourceLocation id, ClientEntryCategory category);
	
	/**
	 * @param id the category id
	 * @see DPClientAPI#setEntryCategory(ResourceLocation, ClientEntryCategory)
	 */
	ClientEntryCategory getEntryCategory(ResourceLocation id);
}
