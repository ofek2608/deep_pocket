package com.ofek2608.deep_pocket.integration;

import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.SignalSettings;
import com.ofek2608.deep_pocket.api.struct.client.ClientPocket;
import com.ofek2608.deep_pocket.registry.interfaces.BlockEntityWithPocket;
import com.ofek2608.deep_pocket.registry.interfaces.BlockEntityWithPocketFilter;
import com.ofek2608.deep_pocket.registry.interfaces.SignalBlock;
import com.ofek2608.deep_pocket.registry.items.PocketItem;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;

import java.util.function.Function;

public final class DeepPocketTheOneProbe {
	private DeepPocketTheOneProbe() {}
	@SuppressWarnings("EmptyMethod")
	public static void loadClass() {}
	@SuppressWarnings("SpellCheckingInspection")
	public static final String MODID = "theoneprobe";

	public static boolean hasMod() {
		return ModList.get().isLoaded(MODID);
	}

	static {
		if (hasMod())
			Integrator.init();
	}

	private static final class Integrator {
		private Integrator() {}

		private static void init() {
			InterModComms.sendTo(MODID, "getTheOneProbe", ()->(Function<ITheOneProbe,Void>)Integrator::onProbeAvailable);
		}

		@SuppressWarnings("SameReturnValue")
		private static Void onProbeAvailable(ITheOneProbe probe) {
			probe.registerProvider(new DeepPocketInfoProvider());
			return null;
		}
	}

	private static final class DeepPocketInfoProvider implements IProbeInfoProvider {
		private static final ResourceLocation ID = DeepPocketMod.loc("main");
		private DeepPocketInfoProvider() {}

		@Override
		public ResourceLocation getID() {
			return ID;
		}

		@Override
		public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, Player player, Level level, BlockState blockState, IProbeHitData iProbeHitData) {
			BlockEntity blockEntity = level.getBlockEntity(iProbeHitData.getPos());
			if (blockEntity instanceof BlockEntityWithPocket cast)
				addPocketInfo(iProbeInfo.horizontal(), cast);
			if (blockEntity instanceof BlockEntityWithPocketFilter cast)
				addFilterInfo(iProbeInfo.horizontal(), cast);
			if (blockEntity instanceof SignalBlock.Ent cast) {
				addSignalSettingsInfo(iProbeInfo.horizontal(), cast);
				addSignalOutputInfo(iProbeInfo.horizontal(), cast);
			}
		}

		private void addPocketInfo(IProbeInfo line, BlockEntityWithPocket entity) {
			addVerticalAlignedText(line, "Pocket: ");
			ClientPocket pocket = entity.getClientPocket();
			if (pocket == null)
				addVerticalAlignedText(line, "None");
			else
				addElementText(line, ElementType.item(PocketItem.createStack(entity.getPocketId())));
		}

		private void addFilterInfo(IProbeInfo line, BlockEntityWithPocketFilter entity) {
			addVerticalAlignedText(line, "Filter: ");
			addElementText(line, entity.getFilter());
		}

		private void addSignalSettingsInfo(IProbeInfo line, SignalBlock.Ent entity) {
			SignalSettings settings = entity.getSettings();
			addVerticalAlignedText(line, "Filter: ");
			addElementText(line, settings.first);
			addVerticalAlignedText(line, settings.bigger ? ">" : "<");
			if (settings.secondItem == null)
				addVerticalAlignedText(line, "" + settings.secondCount);
			else
				addElementText(line, settings.secondItem);
		}

		private void addSignalOutputInfo(IProbeInfo line, SignalBlock.Ent entity) {
			line.text("Emitting: ");
			line.text(entity.getOutput() ? Component.literal("true").withStyle(ChatFormatting.GREEN) : Component.literal("false").withStyle(ChatFormatting.RED));
		}

		private void addElementText(IProbeInfo line, ElementType element) {
			if (element instanceof ElementType.TItem item) {
				line.item(item.create());
			} else if (element instanceof ElementType.TFluid fluid) {
				line.tankSimple(1, fluid.create());
			} else if (element instanceof ElementType.TConvertible convertible) {
				ResourceLocation texture = new ResourceLocation("minecraft:block/stone.png");
				//TODO fix texture to use convertible
				line.icon(texture, 0, 0, 256, 256);
				addVerticalAlignedText(line, element.getDisplayName());
			} else if (element instanceof ElementType.TEnergy) {
				line.progress(1, 1, line.defaultProgressStyle().suffix("FE").filledColor(Config.rfbarFilledColor).alternateFilledColor(Config.rfbarAlternateFilledColor).borderColor(Config.rfbarBorderColor).numberFormat(Config.rfFormat.get()));
			} else {
				addVerticalAlignedText(line, "None");
			}
			
			addVerticalAlignedText(line, element.getDisplayName());
		}

		private void addVerticalAlignedText(IProbeInfo line, String text) {
			line.vertical(line.defaultLayoutStyle().topPadding(6).bottomPadding(7)).text(text);
		}

		private void addVerticalAlignedText(IProbeInfo line, Component text) {
			line.vertical(line.defaultLayoutStyle().topPadding(6).bottomPadding(7)).text(text);
		}
	}
}
