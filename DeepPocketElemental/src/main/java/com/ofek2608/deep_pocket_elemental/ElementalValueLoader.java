package com.ofek2608.deep_pocket_elemental;

import com.mojang.logging.LogUtils;
import com.ofek2608.deep_pocket.api.events.DeepPocketBuildConversionsEvent;
import com.ofek2608.deep_pocket.api.struct.ItemConversions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = DPEMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
final class ElementalValueLoader {
	private ElementalValueLoader() {}
	private static final Logger LOGGER = LogUtils.getLogger();
	private static AllItemsValues values = new AllItemsValues();

	@SubscribeEvent
	public static void event(AddReloadListenerEvent event) {
		event.addListener(new SimplePreparableReloadListener<Void>() {
			@Override
			protected Void prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
				return null;
			}

			@Override
			protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
				values = new AllItemsValues(resourceManager);
			}
		});

	}

	@SubscribeEvent
	public static void event(DeepPocketBuildConversionsEvent event) {
		values.applyBuilder(event.getBuilder());
	}

	private static final class AllItemsValues {
		private final Map<ResourceLocation,ElementalValue> map = new HashMap<>();

		private AllItemsValues() {}

		private AllItemsValues(ResourceManager resourceManager) {
			var resourceStack = resourceManager.getResourceStack(DPEMod.loc("elemental_values.txt"));
			for (Resource resource : resourceStack) {
				try (var reader = resource.openAsReader()) {
					reader.lines().forEach(this::readLine);
				} catch (Exception e) {
					LOGGER.error("Error while reading elemental_values for " + resource.sourcePackId(), e);
				}
			}
		}

		private void readLine(String line) {
			if (line.equals("clear")) {
				map.clear();
				return;
			}
			String[] split = line.split(",");
			try {
				if (split.length == 5) {
					map.put(new ResourceLocation(split[0]), new ElementalValue(
									Long.parseLong(split[1]),
									Long.parseLong(split[2]),
									Long.parseLong(split[3]),
									Long.parseLong(split[4])
					));
					return;
				}
				map.remove(new ResourceLocation(split[0]));
			} catch (Exception ignored) {}
		}

		private void applyBuilder(ItemConversions.Builder builder) {
			map.forEach((key,value)->{
				Item item = ForgeRegistries.ITEMS.getValue(key);
				if (item == null)
					return;
				value.applyBuilder(builder.item(item));
			});
		}
	}

	private static final class ElementalValue {
		private final long earth;
		private final long water;
		private final long air;
		private final long fire;

		private ElementalValue(long earth, long water, long air, long fire) {
			this.earth = earth;
			this.water = water;
			this.air = air;
			this.fire = fire;
		}

		private void applyBuilder(ItemConversions.ItemValueBuilder builder) {
			builder.add(ModRegistry.EARTH.get(), earth);
			builder.add(ModRegistry.WATER.get(), water);
			builder.add(ModRegistry.AIR.get(), air);
			builder.add(ModRegistry.FIRE.get(), fire);
		}
	}
}
