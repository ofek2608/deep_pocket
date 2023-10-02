package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.implementable.ClientEntryCategory;
import com.ofek2608.deep_pocket.api.implementable.PocketTabDefinition;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.pocket.PocketProperties;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
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
	
	/**
	 * Sends the server a packet that identifies the client want to create a pocket.
	 */
	void requestCreatePocket();
	
	/**
	 * @param id    the id of the tab
	 * @param tab   the implementation
	 * @param order the order in which to show the tabs
	 * @see DPClientAPI#getPocketTab(ResourceLocation)
	 * @see DPClientAPI#getVisiblePocketTabs(LocalPlayer, Pocket)
	 */
	void registerPocketTab(ResourceLocation id, PocketTabDefinition tab, String order);
	
	/**
	 * @param id the id assigned in registerPocketTab
	 * @return the implementation assigned in registerPocketTab
	 * @see DPClientAPI#registerPocketTab(ResourceLocation, PocketTabDefinition, String)
	 */
	Optional<PocketTabDefinition> getPocketTab(ResourceLocation id);
	
	/**
	 * @param player the player which see the tabs
	 * @param pocket the pocket which the player look for tabs in
	 * @return the tab ids in the order
	 * @see DPClientAPI#registerPocketTab(ResourceLocation, PocketTabDefinition, String)
	 * @see DPClientAPI#getPocketTab(ResourceLocation)
	 */
	List<ResourceLocation> getVisiblePocketTabs(LocalPlayer player, Pocket pocket);
}
