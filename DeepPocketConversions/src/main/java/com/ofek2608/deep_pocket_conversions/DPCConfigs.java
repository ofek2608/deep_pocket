package com.ofek2608.deep_pocket_conversions;

import com.ofek2608.deep_pocket_conversions.client.OverlayLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class DPCConfigs {
	private DPCConfigs() {}
	@SuppressWarnings("EmptyMethod") public static void loadClass() {}
	static {
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Client.SPEC);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Common.SPEC);
	}

	public static class Client {
		private Client() {}

		private static final ForgeConfigSpec SPEC;
		public static final ForgeConfigSpec.EnumValue<OverlayLocation> OVERLAY_LOCATION;
		public static final ForgeConfigSpec.BooleanValue OVERLAY_DIRECTION;

		static {
			var builder = new ForgeConfigSpec.Builder();

			builder.comment("Where to put the overlay");
			OVERLAY_LOCATION = builder.defineEnum("OverlayPosition", OverlayLocation.TOP_LEFT);

			builder.comment("If set to true, icons will be on the left, otherwise on the right.");
			OVERLAY_DIRECTION = builder.define("OverlayDirection", true);

			SPEC = builder.build();
		}
	}

	public static class Common {
		private Common() {}

		private static final ForgeConfigSpec SPEC;
		public static final ForgeConfigSpec.BooleanValue RECIPE_LOADERS_ALL;
		public static final ForgeConfigSpec.BooleanValue RECIPE_LOADERS_CRAFTING;
		public static final ForgeConfigSpec.BooleanValue RECIPE_LOADERS_SMELTING;
		public static final ForgeConfigSpec.BooleanValue RECIPE_LOADERS_BLASTING;
		public static final ForgeConfigSpec.BooleanValue RECIPE_LOADERS_SMOKING;
		public static final ForgeConfigSpec.BooleanValue RECIPE_LOADERS_CAMPFIRE;
		public static final ForgeConfigSpec.BooleanValue RECIPE_LOADERS_STONECUTTING;
		public static final ForgeConfigSpec.BooleanValue RECIPE_LOADERS_SMITHING;

		static {
			var builder = new ForgeConfigSpec.Builder();

			{
				builder.comment("Determines if it should use recipes and what type of recipes");
				builder.push("RecipeLoaders");

				builder.comment("Enable/Disable all recipes.");
				RECIPE_LOADERS_ALL = builder.define("all", true);
				builder.comment("Enable/Disable crafting table recipes.");
				RECIPE_LOADERS_CRAFTING = builder.define("crafting", true);
				builder.comment("Enable/Disable furnace recipes.");
				RECIPE_LOADERS_SMELTING = builder.define("smelting", true);
				builder.comment("Enable/Disable blast furnace recipes.");
				RECIPE_LOADERS_BLASTING = builder.define("blasting", true);
				builder.comment("Enable/Disable smoker recipes.");
				RECIPE_LOADERS_SMOKING = builder.define("blasting", true);
				builder.comment("Enable/Disable campfire recipes.");
				RECIPE_LOADERS_CAMPFIRE = builder.define("campfire_cooking", true);
				builder.comment("Enable/Disable stone cutter recipes.");
				RECIPE_LOADERS_STONECUTTING = builder.define("stonecutting", true);
				builder.comment("Enable/Disable smithing table recipes.");
				RECIPE_LOADERS_SMITHING = builder.define("smithing", true);

				builder.pop();
			}

			SPEC = builder.build();
		}
	}
}
