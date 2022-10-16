package com.ofek2608.deep_pocket.registry.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class CraftingPatternItem extends Item {
	public CraftingPatternItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> text, TooltipFlag isAdvanced) {
		text.add(Component.literal("WIP").withStyle(ChatFormatting.GOLD));
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
		//FIXME add image of the recipe
		// - use RegisterClientTooltipComponentFactoriesEvent
		return Optional.empty();
	}


	//FIXME item should render like the crafted item if {Screen.hasShiftDown()}
}
