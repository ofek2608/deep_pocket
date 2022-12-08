package com.ofek2608.deep_pocket.client.client_screens;

import com.ofek2608.deep_pocket.api.struct.*;
import com.ofek2608.deep_pocket.api.Pocket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

public final class ClientScreens {
	private ClientScreens() {}

	public  static void settings(@Nullable UUID pocketId, PocketInfo info) {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.setScreen(new PocketSettingsScreen(minecraft.screen, pocketId, info));
	}

	public static void settingsNew() {
		settings(null, new PocketInfo());
	}

	public static void settingsEdit(Pocket pocket) {
		settings(pocket.getPocketId(), pocket.getInfo());
	}

	public static void selectPocket() {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player != null)
			minecraft.setScreen(new PocketSelectionScreen(minecraft.screen, player));
	}

	public static void selectItem(Component title, int color, Consumer<ItemStack> onSelect, Runnable onCancel) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player != null)
			minecraft.setScreen(new ItemSelectionScreen(title, color, player.getInventory(), onSelect, onCancel));
	}

	public static void selectNumber(Component title, int color, long initialValue, LongConsumer onSelect) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player != null)
			minecraft.setScreen(new NumberSelectionScreen(title, color, initialValue, onSelect));
	}

	public static void selectRecipe(Pocket pocket, ItemType requiredOutput, Consumer<CraftingPattern> onSelect) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player != null)
			minecraft.setScreen(new RecipeSelectionScreen(player, pocket, requiredOutput, onSelect));
	}

	public static void configureSignalBlock(int color, BlockPos pos, SignalSettings settings) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player != null)
			minecraft.setScreen(new SignalSettingsScreen(minecraft.screen, player, color, pos, settings));
	}

	public static void bulkCrafting(Pocket pocket, ItemType[] recipe) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player != null)
			minecraft.setScreen(new BulkCraftingScreen(minecraft.screen, pocket, recipe));
	}

	public static void processRequest(Pocket pocket, ElementType requestedType, long requestedAmount) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player != null)
			minecraft.setScreen(new RequestProcessScreen(minecraft.screen, pocket, requestedType, requestedAmount));
	}
}
