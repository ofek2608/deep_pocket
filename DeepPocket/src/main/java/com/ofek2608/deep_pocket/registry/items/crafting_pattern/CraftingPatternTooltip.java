package com.ofek2608.deep_pocket.registry.items.crafting_pattern;

import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class CraftingPatternTooltip implements TooltipComponent {
	public final ItemTypeAmount[] input;
	public final ItemTypeAmount[] output;

	public CraftingPatternTooltip(ItemTypeAmount[] input, ItemTypeAmount[] output) {
		this.input = input;
		this.output = output;
	}
}
