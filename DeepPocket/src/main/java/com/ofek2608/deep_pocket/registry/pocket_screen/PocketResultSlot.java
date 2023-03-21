package com.ofek2608.deep_pocket.registry.pocket_screen;

import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.server.ServerPocket;
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
		ServerPocket pocket = menu.getServerPocket();
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
			ElementType oldType = ElementType.item(oldItem);

			if (!(oldType instanceof ElementType.TItem oldItemType)) {
				// if the old item is empty, put the result slot
				craftSlots.setItem(i, newItem);
				continue;
			}

			if (oldItem.getCount() > 1) {
				// if there are more than 1 items in the original slot, consume
				this.craftSlots.removeItem(i, 1);
				// the slot is full so there is no place for the new item
				pocket.insertElement(ElementType.item(newItem), newItem.getCount());
				continue;
			}
			if (pocket.extractItem(api.getKnowledge(player.getUUID()), oldItemType, 1) == 1) {
				// if there is 1 item in the original slot, and we could extract from the pocket.
				// we need to give the player the remaining item, and the slot is already caught
				// also we can put the remaining item there
				pocket.insertElement(ElementType.item(newItem), newItem.getCount());
				continue;
			}
			// if there is 1 item in the original slot, and couldn't extract from the pocket, this item will be consumed
			// also we can put the remaining item there
			this.craftSlots.setItem(i, newItem);
		}
		menu.reloadCrafting();
	}

	@Override
	public void checkTakeAchievements(ItemStack pStack) {
		super.checkTakeAchievements(pStack);
	}
}
