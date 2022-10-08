package com.ofek2608.deep_pocket.client_screens;

import com.ofek2608.deep_pocket.api.struct.Pocket;
import com.ofek2608.deep_pocket.api.struct.PocketInfo;
import com.ofek2608.deep_pocket.api.struct.SignalSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public final class ClientScreens {
	private ClientScreens() {}

	public  static void openScreenSettings(@Nullable UUID pocketId, PocketInfo info) {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.setScreen(new PocketSettingsScreen(minecraft.screen, pocketId, info));
	}

	public static void openScreenSettingsNew() {
		openScreenSettings(null, new PocketInfo());
	}

	public static void openScreenSettingsEdit(Pocket pocket) {
		openScreenSettings(pocket.getPocketId(), pocket.getInfo());
	}

	public static void openScreenSelectPocket() {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player != null)
			minecraft.setScreen(new PocketSelectionScreen(minecraft.screen, player));
	}

	public static void openScreenSelectItem(Component title, int color, Consumer<ItemStack> onSelect, Runnable onCancel) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player != null)
			minecraft.setScreen(new ItemSelectionScreen(title, color, player.getInventory(), onSelect, onCancel));
	}

	public static void openScreenConfigureSignalBlock(int color, BlockPos pos, SignalSettings settings) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player != null)
			minecraft.setScreen(new SignalSettingsScreen(minecraft.screen, player, color, pos, settings));
	}
}
