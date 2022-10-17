package com.ofek2608.deep_pocket.registry.items.crafting_pattern;

import com.ofek2608.deep_pocket.api.struct.ItemAmount;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class CraftingPatternTooltip implements TooltipComponent {
	public final ItemAmount[] input;
	public final ItemTypeAmount[] output;

	public CraftingPatternTooltip(ItemAmount[] input, ItemTypeAmount[] output) {
		this.input = input;
		this.output = output;
	}
}
