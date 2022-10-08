package com.ofek2608.deep_pocket.integration;

import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.registry.interfaces.BlockEntityWithPocket;
import com.ofek2608.deep_pocket.registry.interfaces.BlockEntityWithPocketFilter;
import com.ofek2608.deep_pocket.registry.items.PocketItem;
import mcjty.theoneprobe.api.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
		}

		private void addPocketInfo(IProbeInfo line, BlockEntityWithPocket entity) {
			addVerticalAlignedText(line, "Pocket: ");
			Pocket pocket = entity.getClientPocket();
			if (pocket == null)
				addVerticalAlignedText(line, "None");
			else
				addItemText(line, PocketItem.createStack(entity.getPocketId()));
		}

		private void addFilterInfo(IProbeInfo line, BlockEntityWithPocketFilter entity) {
			addVerticalAlignedText(line, "Filter: ");
			ItemType filter = entity.getFilter();
			if (filter.isEmpty())
				addVerticalAlignedText(line, "None");
			else
				addItemText(line, filter.create());
		}

		private void addItemText(IProbeInfo line, ItemStack item) {
			line.item(item);
			addVerticalAlignedText(line, item.getHoverName());
		}

		private void addVerticalAlignedText(IProbeInfo line, String text) {
			line.vertical(line.defaultLayoutStyle().topPadding(6).bottomPadding(7)).text(text);
		}

		private void addVerticalAlignedText(IProbeInfo line, Component text) {
			line.vertical(line.defaultLayoutStyle().topPadding(6).bottomPadding(7)).text(text);
		}
	}
}
