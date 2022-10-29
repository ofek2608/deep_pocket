package com.ofek2608.deep_pocket.registry;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public final class ProtoMenu extends AbstractContainerMenu {
	public static final ProtoMenu INSTANCE = new ProtoMenu();
	private ProtoMenu() {
		super(null, 0);
	}

	@Override
	public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return true;
	}
}
