package com.ofek2608.deep_pocket_conversions.matter_value_loading;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.ofek2608.deep_pocket.api.events.DeepPocketBuildConversionsEvent;
import com.ofek2608.deep_pocket.api.struct.ItemConversions;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket_conversions.DPCConfigs;
import com.ofek2608.deep_pocket_conversions.DPCMod;
import com.ofek2608.deep_pocket_conversions.DPCUtils;
import com.ofek2608.deep_pocket_conversions.api.DPCRecipeLoader;
import com.ofek2608.deep_pocket_conversions.registry.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class MVLoader {
	private MVLoader() {}
	private static final Logger LOGGER = LogUtils.getLogger();
	private static MVFile matterValues = new MVFile();

	@Mod.EventBusSubscriber(modid = DPCMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	private static final class ForgeEvents {
		@SubscribeEvent
		public static void event(AddReloadListenerEvent event) {
			event.addListener(new SimplePreparableReloadListener<Void>() {
				@Override
				protected Void prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
					return null;
				}

				@Override
				protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
					matterValues = load(resourceManager);
				}
			});
		}

		@SubscribeEvent
		public static void event(DeepPocketBuildConversionsEvent event) {
			applyBuilder(matterValues, event.getServer(), event.getBuilder());
		}
	}

	private static MVFile load(ResourceManager resourceManager) {
		boolean overrideConfiguredRecipes = DPCConfigs.Common.OVERRIDE_CONFIGURED_RECIPES.get();
		var locations = resourceManager.getNamespaces().stream().map(namespace->new ResourceLocation(namespace, "matter_values.json")).toList();
		Stream<MVFile> files = resourceManager.listPacks()
						.flatMap(pack->locations.stream().map(loc->loadOneFile(pack,loc)))
						.filter(Objects::nonNull);
		if (!overrideConfiguredRecipes) {
			MVFile file = loadConfig();
			if (file != null)
				files = Stream.concat(files, Stream.of(file));
		}
		MVFile result = new MVFile(false, files.toArray(MVFile[]::new));
		if (overrideConfiguredRecipes)
			saveConfig(result);
		return result;
	}

	public static @Nullable MVFile loadConfig() {
		try {
			Path path = getConfigPath();
			String data = Files.readString(path);
			return new MVFile(GsonHelper.parse(data));
		} catch (Exception e) {
			LOGGER.error("Couldn't load matter values from the config folder", e);
			return null;
		}
	}

	public static void saveConfig(MVFile file) {

		try {
			String data = new GsonBuilder().setPrettyPrinting().create().toJson(file.save());
			int firstObject = data.indexOf('{') + 1;
			data = data.substring(0, firstObject) +
							"\n  \"Note\": \"If you want to edit this file, you need to disable 'overrideConfiguredRecipes' in the mod config.\"," +
							data.substring(firstObject);

			Path path = getConfigPath();
			if (!Files.exists(path))
				Files.createFile(path);
			Files.writeString(path, data);
		} catch (Exception e) {
			LOGGER.error("Couldn't save matter values to the config folder", e);
		}
	}

	private static Path getConfigPath() {
		return FMLPaths.CONFIGDIR.get().resolve("deep_pocket_conversions-matter_value.json");
	}

	private static @Nullable MVFile loadOneFile(PackResources pack, ResourceLocation loc) {
		try (var reader = new BufferedReader(new InputStreamReader(pack.getResource(PackType.SERVER_DATA, loc), StandardCharsets.UTF_8))) {
			return new MVFile(GsonHelper.parse(reader));
		} catch (Exception e) {
			return null;
		}
	}

	private static Stream<Item> getItemsForTag(ResourceLocation findTag) {
		return ForgeRegistries.ITEMS.getValues()
						.stream()
						.filter(item->new ItemStack(item).getTags().anyMatch(tag->tag.location().equals(findTag)));
	}

	private static void addItemTypeValue(Map<ItemType,String> itemTypeValues, String key, String value) {
		if (key.startsWith("#")) {
			getItemsForTag(new ResourceLocation(key.substring(1)))
							.forEach(item->itemTypeValues.put(new ItemType(new ItemStack(item)), value));
			return;
		}
		ItemType type = DPCUtils.parseItemType(key);
		if (!type.isEmpty())
			itemTypeValues.put(type, value);
	}

	private static Map<ItemType,String> getItemTypeValues(List<Map<String,String>> valuesRaw) {
		Map<ItemType,String> itemTypeValues = new HashMap<>();
		valuesRaw.stream()
						.map(Map::entrySet)
						.flatMap(Set::stream)
						.forEach(entry->addItemTypeValue(itemTypeValues, entry.getKey(), entry.getValue()));
		return itemTypeValues;
	}

	private static Set<Item> getBlackListedItems(List<String> blackListRaw) {
		List<Predicate<String>> blackListPredicates = new ArrayList<>();
		for (String regex : blackListRaw) {
			try {
				blackListPredicates.add(Pattern.compile(regex).asMatchPredicate());
			} catch (Exception e) {
				LOGGER.error("Couldn't parse black list pattern: '" + regex + "'");
			}
		}
		Predicate<String> unitedPredicate = str -> blackListPredicates.stream().anyMatch(predicate -> predicate.test(str));
		return ForgeRegistries.ITEMS.getEntries().stream()
						.filter(entry -> unitedPredicate.test(entry.getKey().location().toString()))
						.map(Map.Entry::getValue)
						.collect(Collectors.toSet());
	}

	private static void applyBuilder(MVFile file, MinecraftServer server, ItemConversions.Builder builder) {
		ItemType matter = new ItemType(ModRegistry.getMinMatter());

		Map<ItemType,String> itemTypeValues = getItemTypeValues(file.values);
		Set<Item> blackListedItems = getBlackListedItems(file.blacklist);
		var recipes = DPCRecipeLoader.loadRecipes(server);

		Map<ItemType,Long> resultValues = MVCalculator.calculateMV(file.constants, recipes, itemTypeValues, blackListedItems);
		resultValues.remove(matter);

		resultValues.forEach((type,value) -> builder.item(type).set(matter, value));
	}
}
