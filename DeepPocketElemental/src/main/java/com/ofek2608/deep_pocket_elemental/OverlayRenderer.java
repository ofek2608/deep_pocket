package com.ofek2608.deep_pocket_elemental;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.integration.DeepPocketCurios;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = DPEMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
final class OverlayRenderer {
	private OverlayRenderer() {}

	@SubscribeEvent
	public static void event(RenderGuiOverlayEvent.Post event) {
		if (!event.getOverlay().id().equals(VanillaGuiOverlay.TITLE_TEXT.id()))
			return;
		if (Minecraft.getInstance().options.hideGui)
			return;
		Window window = event.getWindow();
		int w = window.getGuiScaledWidth();
		int h = window.getGuiScaledHeight();
		if (w < 72 || h < 72)
			return;
		OverlayLocation location = Configs.Client.OVERLAY_LOCATION.get();
		boolean ltr = Configs.Client.OVERLAY_DIRECTION.get();
		int x = (w - 72) * location.x / 2 + 4;
		int y = (h - 72) * location.y / 2 + 4;
		render(event.getPoseStack(), x, y, ltr);

	}

	private static void render(PoseStack stack, int x, int y, boolean direction) {
		Minecraft minecraft = Minecraft.getInstance();
		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		Font font = minecraft.font;
		Player player = minecraft.player;
		if (player == null) return;
		UUID pocketId = DeepPocketCurios.getPocket(player);
		if (pocketId == null) return;
		Pocket pocket = DeepPocketClientApi.get().getPocket(pocketId);
		if (pocket == null || !pocket.canAccess(player)) return;

		String earth = DeepPocketUtils.advancedToString(pocket.getItemCount(new ItemType(ModRegistry.EARTH.get())));
		String water = DeepPocketUtils.advancedToString(pocket.getItemCount(new ItemType(ModRegistry.WATER.get())));
		String air = DeepPocketUtils.advancedToString(pocket.getItemCount(new ItemType(ModRegistry.AIR.get())));
		String fire = DeepPocketUtils.advancedToString(pocket.getItemCount(new ItemType(ModRegistry.FIRE.get())));

		//Background
		Screen.fill(stack, x, y, x + 64, y + 64, 0xCC111111);
		//Icons
		int iconX = direction ? x : x + 48;
		itemRenderer.renderGuiItem(new ItemStack(ModRegistry.EARTH.get()), iconX, y);
		itemRenderer.renderGuiItem(new ItemStack(ModRegistry.WATER.get()), iconX, y + 16);
		itemRenderer.renderGuiItem(new ItemStack(ModRegistry.AIR.get()), iconX, y + 32);
		itemRenderer.renderGuiItem(new ItemStack(ModRegistry.FIRE.get()), iconX, y + 48);
		//Numbers
		font.draw(stack, earth, direction ? x + 18 : x + 46 - font.width(earth), y + 4, 0xFFFFFF);
		font.draw(stack, water, direction ? x + 18 : x + 46 - font.width(water), y + 20, 0xFFFFFF);
		font.draw(stack, air, direction ? x + 18 : x + 46 - font.width(air), y + 36, 0xFFFFFF);
		font.draw(stack, fire, direction ? x + 18 : x + 46 - font.width(fire), y + 52, 0xFFFFFF);
	}
}
