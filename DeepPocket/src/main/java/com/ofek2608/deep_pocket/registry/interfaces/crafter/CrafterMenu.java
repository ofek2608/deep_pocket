package com.ofek2608.deep_pocket.registry.interfaces.crafter;

import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrafterMenu extends AbstractContainerMenu {
	public static class FilteredItemStackHandler extends ItemStackHandler {
		public FilteredItemStackHandler() {
		}

		public FilteredItemStackHandler(int size) {
			super(size);
		}

		public FilteredItemStackHandler(NonNullList<ItemStack> stacks) {
			super(stacks);
		}

		@Override
		public boolean isItemValid(int slot, @NotNull ItemStack stack) {
			return stack.is(DeepPocketRegistry.CRAFTING_PATTERN_ITEM.get());
		}
	}

	protected final ContainerLevelAccess access;
	private final int rowCount;

	public CrafterMenu(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, new FilteredItemStackHandler(9), BlockPos.ZERO);
	}

	public CrafterMenu(int containerId, Inventory playerInventory, IItemHandler slots, BlockPos pos) {
		this(DeepPocketRegistry.CRAFTER_MENU.get(), containerId, playerInventory, slots, pos, 1);
	}

	public CrafterMenu(@Nullable MenuType<?> menuType, int containerId, Inventory playerInventory, IItemHandler slots, BlockPos pos, int containerRows) {
		super(menuType, containerId);
		this.access = ContainerLevelAccess.create(playerInventory.player.level, pos);
		this.rowCount = containerRows;

		int i = (containerRows - 4) * 18;
		//Slots
		for(int j = 0; j < containerRows; ++j)
			for(int k = 0; k < 9; ++k)
				this.addSlot(new SlotItemHandler(slots, k + j * 9, 8 + k * 18, 18 + j * 18));
		//Inventory
		for(int l = 0; l < 3; ++l)
			for(int j1 = 0; j1 < 9; ++j1)
				this.addSlot(new Slot(playerInventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + i));
		//Hotbar
		for(int i1 = 0; i1 < 9; ++i1)
			this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 161 + i));
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(slotIndex);
		if (slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (slotIndex < 9) {
				if (!this.moveItemStackTo(itemstack1, 9, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemstack1, 0, 9, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return itemstack;
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(access, player, DeepPocketRegistry.CRAFTER_BLOCK.get());
	}

	public int getRowCount() {
		return rowCount;
	}
}
