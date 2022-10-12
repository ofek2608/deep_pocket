package com.ofek2608.deep_pocket_conversions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.logging.LogUtils;
import com.ofek2608.deep_pocket.api.events.DeepPocketBuildConversionsEvent;
import com.ofek2608.deep_pocket.api.struct.ItemConversions;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket_conversions.api.DPCRecipeLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = DPCMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
final class MVLoader {
	private MVLoader() {}
	private static final Logger LOGGER = LogUtils.getLogger();
	private static MVs matterValues = new MVs();

	@SubscribeEvent
	public static void event(AddReloadListenerEvent event) {
		event.addListener(new SimplePreparableReloadListener<Void>() {
			@Override
			protected Void prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
				return null;
			}

			@Override
			protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
				matterValues = new MVs(resourceManager);
			}
		});

	}

	@SubscribeEvent
	public static void event(DeepPocketBuildConversionsEvent event) {
		matterValues.applyBuilder2(event.getServer(), event.getBuilder());
	}

	private static final class MVs {
		private final List<Pattern> doNotCalculateRegex = new ArrayList<>();
		private final Map<String,Long> constants = new HashMap<>();
		private final Map<String,String> map = new HashMap<>();

		private MVs() {}

//		private MVs(ResourceManager resourceManager) {
//			var resourceStack = resourceManager.getResourceStack(DPCMod.loc("matter_values.text"));
//			for (Resource resource : resourceStack) {
//				try (var reader = resource.openAsReader()) {
//					reader.lines().forEach(this::readLine);
//				} catch (Exception e) {
//					LOGGER.error("Error while reading elemental_values for " + resource.sourcePackId(), e);
//				}
//			}
//		}
//
//		private void readLine(String line) {
//			if (line.equals("clear")) {
//				map.clear();
//				return;
//			}
//			String[] split = line.split(",");
//			try {
//				if (split.length == 2) {
//					map.put(new ResourceLocation(split[0]), Long.parseLong(split[1]));
//					return;
//				}
//				map.remove(new ResourceLocation(split[0]));
//			} catch (Exception ignored) {}
//		}

		private MVs(ResourceManager resourceManager) {
			var resourceStack = resourceManager.getResourceStack(DPCMod.loc("matter_values.json"));
			for (Resource resource : resourceStack) {
				try (var reader = resource.openAsReader()) {
					JsonObject obj = GsonHelper.parse(reader);
					if (obj.get("clear") instanceof JsonPrimitive clear && clear.getAsBoolean()) {
						doNotCalculateRegex.clear();
						constants.clear();
						map.clear();
					}
					if (obj.get("blacklist") instanceof JsonArray blacklist) {
						for (JsonElement blackListElement : blacklist) {
							try {
								doNotCalculateRegex.add(Pattern.compile(blackListElement.getAsString()));
							} catch (Exception ignored) {}
						}
					}
					if (obj.get("constants") instanceof JsonObject jsonConstants) {
						for (var entry : jsonConstants.entrySet()) {
							try {
								String value = entry.getValue().getAsString();
								constants.put(entry.getKey(), value.equals("@") ? -1 : Long.parseLong(value));
							} catch (Exception ignored) {}
						}
					}
					if (obj.get("values") instanceof JsonObject values) {
						for (var entry : values.entrySet()) {
							if (entry.getValue() instanceof JsonPrimitive value && !value.isBoolean())
								map.put(entry.getKey(), value.getAsString());
							else
								map.remove(entry.getKey());
						}
					}

				} catch (Exception e) {
					LOGGER.error("Error while reading elemental_values for " + resource.sourcePackId(), e);
				}
			}
		}

		private boolean doNotCalculateMatch(ResourceLocation loc) {
			for (Pattern pattern : doNotCalculateRegex)
				if (pattern.matcher(loc.toString()).matches())
					return true;
			return false;
		}

		private void applyBuilder2(MinecraftServer server, ItemConversions.Builder builder) {
			ItemType matter = new ItemType(ModRegistry.getMinMatter());
			//Creating initial values
			Map<ItemType,String> recipesOverrides = new HashMap<>();
			map.forEach((key,value)->{
				ItemType type = DPCUtils.parseItemType(key);
				if (!type.isEmpty())
					recipesOverrides.put(type, value);
			});
			for (int num = MatterItem.MIN_MATTER_NUM; num <= MatterItem.MAX_MATTER_NUM; num++) {
				MatterItem item = ModRegistry.getMatter(num);
				recipesOverrides.put(new ItemType(item), item.value >= 0 ? "" + item.value : "@");
			}
			//Filtering items to skip
			Set<Item> doNotCalculate = ForgeRegistries.ITEMS.getEntries()
							.stream()
							.filter(entry -> doNotCalculateMatch(entry.getKey().location()))
							.map(Map.Entry::getValue)
							.collect(Collectors.toSet());
			//Loading recipes
			var recipes = DPCRecipeLoader.loadRecipes(server);
			//Calculating
			Map<ItemType,Long> values = MVCalculator.calculateMV(constants, recipes, recipesOverrides, doNotCalculate);
			values.remove(matter);
			//applying builder
			values.forEach((type,value) -> builder.item(type).set(matter, value));
		}
	}
}
