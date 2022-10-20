package com.ofek2608.deep_pocket.registry.pocket_screen;

import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.Pocket;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;

public class PocketResultSlot extends ResultSlot {
	private final CraftingContainer craftSlots;
	private final PocketMenu menu;

	public PocketResultSlot(Player player, CraftingContainer craftSlots, Container container, int slot, int xPos, int yPos, PocketMenu menu) {
		super(player, craftSlots, container, slot, xPos, yPos);
		this.craftSlots = craftSlots;
		this.menu = menu;
	}

	@Override
	public void onTake(Player player, ItemStack stack) {
		if (player.level.isClientSide) {
			for (int i = 0; i < craftSlots.getContainerSize(); i++)
				craftSlots.removeItem(i, 1);
			return;
		}

		DeepPocketServerApi api = DeepPocketServerApi.get();
		Pocket pocket = menu.getPocket();
		if (api == null || pocket == null) {
			super.onTake(player, stack);
			return;
		}
		this.checkTakeAchievements(stack);
		ForgeHooks.setCraftingPlayer(player);
		NonNullList<ItemStack> remainingItems = player.level.getRecipeManager().getRemainingItemsFor(RecipeType.CRAFTING, this.craftSlots, player.level);
		ForgeHooks.setCraftingPlayer(null);
		for(int i = 0; i < remainingItems.size(); ++i) {
			ItemStack oldItem = this.craftSlots.getItem(i);
			ItemStack newItem = remainingItems.get(i);

			if (oldItem.isEmpty()) {
				pocket.insertItem(new ItemType(newItem), newItem.getCount());
				continue;
			}

			if (oldItem.getCount() > 1 || pocket.extractItem(api.getKnowledge(player.getUUID()), new ItemType(oldItem), 1) != 1)
				this.craftSlots.removeItem(i, 1);
			oldItem = this.craftSlots.getItem(i);

			if (newItem.isEmpty())
				continue;

			if (oldItem.isEmpty()) {
				this.craftSlots.setItem(i, newItem);
				continue;
			}
			pocket.insertItem(new ItemType(newItem), newItem.getCount());
		}
		menu.reloadCrafting();
	}

	@Override
	public void checkTakeAchievements(ItemStack pStack) {
		super.checkTakeAchievements(pStack);
	}
}
