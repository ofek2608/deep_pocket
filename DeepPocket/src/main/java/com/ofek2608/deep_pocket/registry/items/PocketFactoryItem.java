package com.ofek2608.deep_pocket.registry.items;

import com.ofek2608.deep_pocket.client.client_screens.ClientScreens;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PocketFactoryItem extends Item {
	public PocketFactoryItem(Properties prop) {
		super(prop);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide)
			ClientScreens.settingsNew();
		return InteractionResultHolder.success(stack);
	}
}
