package com.ofek2608.deep_pocket_conversions.client;

import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.ItemConversions;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket_conversions.DPCMod;
import com.ofek2608.deep_pocket_conversions.registry.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.OptionalLong;

@Mod.EventBusSubscriber(modid = DPCMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
final class MVTooltip {
	private MVTooltip() {}

	@SubscribeEvent
	public static void event(ItemTooltipEvent event) {
		OptionalLong optionalValue = getValue(new ItemType(event.getItemStack()));
		if (optionalValue.isEmpty())
			return;
		long value = optionalValue.getAsLong();
		List<Component> list = event.getToolTip();
		list.add(list.size() == 0 ? 0 : 1, Component.translatable("tooltip." + DPCMod.ID + ".matter_value", Screen.hasShiftDown() ? (value < 0 ? "Inf" : "" + value) : DeepPocketUtils.advancedToString(value)).withStyle(ChatFormatting.YELLOW));
	}

	private static OptionalLong getValue(ItemType type) {
		ItemConversions conversions = DeepPocketClientApi.get().getItemConversions();
		int matterIndex = getMatterIndex(conversions);
		if (matterIndex < 0) return OptionalLong.empty();
		long[] value = conversions.getValue(type);
		return value == null ? OptionalLong.empty() : OptionalLong.of(value[matterIndex]);
	}

	private static int getMatterIndex(ItemConversions conversions) {
		ItemType[] baseItems = conversions.getBaseItems();
		ItemType search = new ItemType(ModRegistry.getMinMatter());
		for (int i = 0; i < baseItems.length; i++)
			if (baseItems[i].equals(search))
				return i;
		return -1;
	}
}
