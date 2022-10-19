package com.ofek2608.deep_pocket.registry.interfaces.crafter;

import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CrafterSlot extends Slot {
	public CrafterSlot(Container container, int slot, int x, int y) {
		super(container, slot, x, y);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return stack.is(DeepPocketRegistry.CRAFTING_PATTERN_ITEM.get());
	}
}
