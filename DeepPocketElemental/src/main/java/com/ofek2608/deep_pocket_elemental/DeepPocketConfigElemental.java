package com.ofek2608.deep_pocket_elemental;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.stream.Stream;

public final class DeepPocketConfigElemental {
	private DeepPocketConfigElemental() {}
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

			builder.comment("Where to put the overlay", "Possible values are: " + Stream.of(OverlayLocation.values()).map(OverlayLocation::name).toList());
			OVERLAY_LOCATION = builder.defineEnum("OverlayPosition", OverlayLocation.TOP_LEFT);

			builder.comment("If set to true, icons will be on the left, otherwise on the right.");
			OVERLAY_DIRECTION = builder.define("OverlayDirection", true);

			SPEC = builder.build();
		}
	}

	public static final class Common {
		private Common() {}

		private static final ForgeConfigSpec SPEC;

		static {
			var builder = new ForgeConfigSpec.Builder();

			SPEC = builder.build();
		}
	}
}
