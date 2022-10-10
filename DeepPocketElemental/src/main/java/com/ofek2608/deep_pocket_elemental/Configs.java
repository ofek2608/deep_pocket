package com.ofek2608.deep_pocket_elemental;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class Configs {
	private Configs() {}
	@SuppressWarnings("EmptyMethod") public static void loadClass() {}
	static {
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Client.SPEC);
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
}
