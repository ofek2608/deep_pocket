package com.ofek2608.deep_pocket.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DPClientAPI;
import com.ofek2608.deep_pocket.api.ServerConfig;
import com.ofek2608.deep_pocket.api.enums.PocketAccess;
import com.ofek2608.deep_pocket.api.events.DPClientAPIEvent;
import com.ofek2608.deep_pocket.api.implementable.ClientEntryCategory;
import com.ofek2608.deep_pocket.api.implementable.PocketTabDefinition;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.pocket.PocketProperties;
import com.ofek2608.deep_pocket.api.types.EntryStack;
import com.ofek2608.deep_pocket.api.types.EntryType;
import com.ofek2608.deep_pocket.api.utils.LNUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ClientAPIImpl implements DPClientAPI {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static ClientAPIImpl instance;
	public final Map<UUID, PocketPropertiesImpl> properties = new HashMap<>();
	public final Map<UUID, PocketImpl> pockets = new HashMap<>();
	public final Map<ResourceLocation, ClientEntryCategory> entryTypeRenderer = new HashMap<>();
	public final List<RegisteredPocketTab> registeredPocketTabsList = new ArrayList<>();
	public final Map<ResourceLocation,RegisteredPocketTab> registeredPocketTabsMap = new HashMap<>();
	public boolean valid = true;
	public ServerConfigImpl serverConfig = ServerConfigImpl.DEFAULT;
	
	public ClientAPIImpl() {
		setEntryCategory(EntryType.CATEGORY_EMPTY, EMPTY_CATEGORY);
		setEntryCategory(EntryType.CATEGORY_ENERGY, ENERGY_CATEGORY);
		setEntryCategory(EntryType.CATEGORY_ITEM, ITEM_CATEGORY);
		setEntryCategory(EntryType.CATEGORY_FLUID, FLUID_CATEGORY);
		setEntryCategory(EntryType.CATEGORY_GENERIC, GENERIC_CATEGORY);
		registerPocketTab(DeepPocketMod.loc("items"), ITEM_TAB, "a");
		registerPocketTab(DeepPocketMod.loc("settings"), SETTINGS_TAB, "z");
	}
	
	@Override
	public boolean isValid() {
		return valid;
	}
	
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
	
	@Override
	public void setEntryCategory(ResourceLocation id, ClientEntryCategory category) {
		entryTypeRenderer.put(id, category);
	}
	
	@Override
	public ClientEntryCategory getEntryCategory(ResourceLocation id) {
		return entryTypeRenderer.getOrDefault(id, UNKNOWN_CATEGORY);
	}
	
	@Override
	public void requestCreatePocket() {
		PacketHandler.sbCreatePocket();
	}
	
	@Override
	public void registerPocketTab(ResourceLocation id, PocketTabDefinition<?> tab, String order) {
		if (registeredPocketTabsMap.containsKey(id)) {
			throw new IllegalArgumentException("Duplicate id detected");
		}
		RegisteredPocketTab toAdd = new RegisteredPocketTab(id, tab, order);
		registeredPocketTabsMap.put(id, toAdd);
		
		for (int i = 0; i < registeredPocketTabsList.size(); i++) {
			if (registeredPocketTabsList.get(i).order.compareTo(order) >= 0) {
				registeredPocketTabsList.add(i, toAdd);
				return;
			}
		}
		registeredPocketTabsList.add(toAdd);
	}
	
	@Override
	public Optional<PocketTabDefinition<?>> getPocketTab(ResourceLocation id) {
		return Optional.of(registeredPocketTabsMap.get(id)).map(RegisteredPocketTab::tab);
	}
	
	@Override
	public List<ResourceLocation> getVisiblePocketTabs(LocalPlayer player, Pocket pocket) {
		return registeredPocketTabsList.stream()
				.filter(r -> r.tab.isVisible(this, player, pocket))
				.map(RegisteredPocketTab::id)
				.collect(Collectors.toList());
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
					instance.valid = false;
				}
				instance = new ClientAPIImpl();
				MinecraftForge.EVENT_BUS.post(new DPClientAPIEvent(instance));
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
				} else {
					instance.valid = false;
				}
				instance = null;
				MinecraftForge.EVENT_BUS.post(new DPClientAPIEvent(null));
			}
		}
	}
	
	private record RegisteredPocketTab(ResourceLocation id, PocketTabDefinition<?> tab, String order) {}
	
	
	
	
	private static final ClientEntryCategory UNKNOWN_CATEGORY = new ClientEntryCategory() {
		@Override
		public void render(EntryStack entryStack, int x, int y) {
			//TODO
		}
		
		@Override
		public void render(EntryStack entryStack, PoseStack poseStack) {
			//TODO
		}
	};
	private static final ClientEntryCategory EMPTY_CATEGORY = new ClientEntryCategory() {
		@Override
		public void render(EntryStack entryStack, int x, int y) {}
		
		@Override
		public void render(EntryStack entryStack, PoseStack poseStack) {}
	};
	private static final ClientEntryCategory ENERGY_CATEGORY = new ClientEntryCategory() {
		@Override
		public void render(EntryStack entryStack, int x, int y) {
			//TODO
		}
		
		@Override
		public void render(EntryStack entryStack, PoseStack poseStack) {
			//TODO
		}
	};
	private static final ClientEntryCategory ITEM_CATEGORY = new ClientEntryCategory() {
		@Override
		public void render(EntryStack entryStack, int x, int y) {
			Item item = ForgeRegistries.ITEMS.getValue(entryStack.type().id());
			if (item == null) {
				UNKNOWN_CATEGORY.render(entryStack, x, y);
				return;
			}
			ItemStack itemStack = new ItemStack(item, LNUtils.closestInt(entryStack.count()));
			Minecraft.getInstance().getItemRenderer().renderGuiItem(itemStack, x, y);
		}
		
		@Override
		public void render(EntryStack entryStack, PoseStack poseStack) {
			//TODO
		}
	};
	private static final ClientEntryCategory FLUID_CATEGORY = new ClientEntryCategory() {
		@Override
		public void render(EntryStack entryStack, int x, int y) {
			//TODO
		}
		
		@Override
		public void render(EntryStack entryStack, PoseStack poseStack) {
			//TODO
		}
	};
	private static final ClientEntryCategory GENERIC_CATEGORY = new ClientEntryCategory() {
		@Override
		public void render(EntryStack entryStack, int x, int y) {
			//TODO
		}
		
		@Override
		public void render(EntryStack entryStack, PoseStack poseStack) {
			//TODO
		}
	};
	private static final PocketTabDefinition<?> ITEM_TAB = new PocketTabDefinition<Void>() {
		@Override
		public boolean isVisible(DPClientAPI api, LocalPlayer player, Pocket pocket) {
			return true;
		}
		
		@Override
		public Void onOpen(DPClientAPI api, LocalPlayer player, Pocket pocket) {
			return null;
		}
		
		@Override
		public int getLeftWidth(Void unused) {
			return 1;
		}
		
		@Override
		public int getRightWidth(Void unused) {
			return 2;
		}
	};
	private static final PocketTabDefinition<?> SETTINGS_TAB = new PocketTabDefinition<Void>() {
		@Override
		public boolean isVisible(DPClientAPI api, LocalPlayer player, Pocket pocket) {
			return Objects.equals(player.getUUID(), pocket.getProperties().getOwner());
		}
		
		@Override
		public Void onOpen(DPClientAPI api, LocalPlayer player, Pocket pocket) {
			return null;
		}
	};
}
