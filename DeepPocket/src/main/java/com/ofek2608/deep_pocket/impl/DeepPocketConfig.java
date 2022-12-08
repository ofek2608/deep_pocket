package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.enums.PocketDisplayFilter;
import com.ofek2608.deep_pocket.api.enums.PocketDisplayMode;
import com.ofek2608.deep_pocket.api.enums.SearchMode;
import com.ofek2608.deep_pocket.api.enums.SortingOrder;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class DeepPocketConfig {
	private DeepPocketConfig() {}
	@SuppressWarnings("EmptyMethod") public static void loadClass() {}
	static {
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Client.SPEC);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Common.SPEC);
	}

	public static class Client {
		private Client() {}

		private static final ForgeConfigSpec SPEC;
		public static final ForgeConfigSpec.EnumValue<SearchMode> SEARCH_MODE;
		public static final ForgeConfigSpec.EnumValue<SortingOrder> SORTING_ORDER;
		public static final ForgeConfigSpec.BooleanValue SORT_ASCENDING;
		public static final ForgeConfigSpec.EnumValue<PocketDisplayFilter> POCKET_DISPLAY_FILTER;
		public static final ForgeConfigSpec.EnumValue<PocketDisplayMode> POCKET_DISPLAY_MODE;

		static {
			var builder = new ForgeConfigSpec.Builder();

			{
				builder.push("GameSettings");
				SEARCH_MODE = builder.defineEnum("SearchMode", SearchMode.NORMAL);
				SORTING_ORDER = builder.defineEnum("SortingOrder", SortingOrder.COUNT);
				SORT_ASCENDING = builder.define("SortAscending", false);
				POCKET_DISPLAY_FILTER = builder.defineEnum("PocketDisplayFilter", PocketDisplayFilter.ITEMS_AND_FLUIDS);
				POCKET_DISPLAY_MODE = builder.defineEnum("PocketDisplayMode", PocketDisplayMode.NORMAL);
				builder.pop();
			}

			SPEC = builder.build();
		}
	}

	public static final class Common {
		private Common() {}

		private static final ForgeConfigSpec SPEC;
		public static final ForgeConfigSpec.BooleanValue ALLOW_PUBLIC_POCKETS;

		static {
			var builder = new ForgeConfigSpec.Builder();

			{
				builder.push("General");
				builder.comment("Whether or not to let players set the pocket security to \"public\".");
				ALLOW_PUBLIC_POCKETS = builder.define("AllowPublicPockets", true);
				builder.pop();
			}

			SPEC = builder.build();
		}
	}
}
