package com.ofek2608.deep_pocket.impl;

import com.mojang.logging.LogUtils;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DPClientAPI;
import com.ofek2608.deep_pocket.api.ServerConfig;
import com.ofek2608.deep_pocket.api.enums.PocketAccess;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.pocket.PocketProperties;
import com.ofek2608.deep_pocket.api.types.EntryType;
import net.minecraft.Util;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public final class ClientAPIImpl implements DPClientAPI {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static ClientAPIImpl instance;
	public final Map<UUID, PocketPropertiesImpl> properties = new HashMap<>();
	public final Map<UUID, PocketImpl> pockets = new HashMap<>();
	public ServerConfigImpl serverConfig = ServerConfigImpl.DEFAULT;
	
	@Override
	public PocketProperties getProperties(UUID pocketId) {
		PocketProperties pocketProperties = properties.get(pocketId);
		if (pocketProperties != null) {
			return pocketProperties;
		}
		return new PocketPropertiesImpl(
				pocketId,
				Util.NIL_UUID,
				"Undefined Pocket " + pocketId,
				PocketAccess.PRIVATE,
				EntryType.EMPTY,
				0xFFFFFF
		);
	}
	
	@Override
	public Optional<Pocket> getPocket(UUID pocketId) {
		return Optional.ofNullable(pockets.get(pocketId));
	}
	
	@Override
	public Stream<UUID> getKnownPockets() {
		return properties.keySet().stream();
	}
	
	@Override
	public Stream<UUID> getVisiblePockets() {
		return pockets.keySet().stream();
	}
	
	@Override
	public ServerConfig getServerConfig() {
		return serverConfig;
	}
	
	public void putProperties(PocketPropertiesImpl properties) {
		PocketPropertiesImpl oldProperties = this.properties.get(properties.getPocketId());
		if (oldProperties == null) {
			this.properties.put(properties.getPocketId(), properties);
		} else {
			oldProperties.setFrom(properties);
		}
	}
	
	public Optional<PocketImpl> getOrCreatePocket(UUID id) {
		if (this.pockets.containsKey(id)) {
			return Optional.of(this.pockets.get(id));
		}
		PocketPropertiesImpl properties = this.properties.get(id);
		if (properties == null) {
			return Optional.empty();
		}
		PocketImpl pocket = new PocketImpl(properties);
		this.pockets.put(id, pocket);
		return Optional.of(pocket);
	}
	
	public void clearPocket(UUID id) {
		this.pockets.remove(id);
	}
	
	public void deletePocket(UUID id) {
		this.pockets.remove(id);
		this.properties.remove(id);
	}
	
	
	@Mod.EventBusSubscriber(modid = DeepPocketMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
	private static final class Events {
		private static int loadedLevels = 0;
		@SubscribeEvent
		public static void event(LevelEvent.Load event) {
			if (!(event.getLevel() instanceof Level level) || !(level.isClientSide())) {
				return;
			}
			loadedLevels++;
			if (loadedLevels == 1) {
				if (instance != null) {
					LOGGER.error("LevelEvent.Load was called when instance is nonnull");
				}
				instance = new ClientAPIImpl();
			}
		}
		
		@SubscribeEvent
		public static void event(LevelEvent.Unload event) {
			if (!(event.getLevel() instanceof Level level) || !(level.isClientSide())) {
				return;
			}
			loadedLevels--;
			if (loadedLevels < 0) {
				LOGGER.error("LevelEvent.Unload was called more times than LevelEvent.Load");
			}
			if (loadedLevels == 0) {
				if (instance == null) {
					LOGGER.error("LevelEvent.Unload was called when instance is null");
				}
				instance = null;
			}
		}
	}
}
