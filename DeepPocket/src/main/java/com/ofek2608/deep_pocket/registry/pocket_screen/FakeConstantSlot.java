package com.ofek2608.deep_pocket.registry.pocket_screen;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FakeConstantSlot extends Slot {
	public FakeConstantSlot(ItemStack stack, int pX, int pY) {
		super(new SimpleContainer(stack), 0, pX, pY);
	}

	@Override
	public boolean mayPickup(Player pPlayer) {
		return false;
	}

	@Override
	public boolean mayPlace(ItemStack pStack) {
		return false;
	}
}
