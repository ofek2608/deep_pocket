package com.ofek2608.deep_pocket.registry.items.crafting_pattern;

import com.ofek2608.deep_pocket.api.struct.ElementTypeStack;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class CraftingPatternTooltip implements TooltipComponent {
	public final ElementTypeStack[] input;
	public final ElementTypeStack[] output;

	public CraftingPatternTooltip(ElementTypeStack[] input, ElementTypeStack[] output) {
		this.input = input;
		this.output = output;
	}
}
