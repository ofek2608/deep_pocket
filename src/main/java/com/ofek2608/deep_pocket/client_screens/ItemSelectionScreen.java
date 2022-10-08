package com.ofek2608.deep_pocket.client_screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.DeepPocketUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

class ItemSelectionScreen extends Screen {
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/select_item.png");
	private static final int VIEW_WIDTH = 154;
	private static final int VIEW_HEIGHT = 98;
	private final Component title;
	private final int color;
	private final Inventory inventory;
	private final Consumer<ItemStack> onSelect;
	private final Runnable onCancel;


	//Update Fields
	private int leftPos;
	private int topPos;
	private boolean hoverCancel;
	private int hoveredSlot;


	ItemSelectionScreen(Component title, int color, Inventory inventory, Consumer<ItemStack> onSelect, Runnable onCancel) {
		super(Component.empty());
		this.title = title;
		this.color = color;
		this.inventory = inventory;
		this.onSelect = onSelect;
		this.onCancel = onCancel;
	}

	private void updateFields(int mx, int my) {
		leftPos = (width - VIEW_WIDTH) >> 1;
		topPos = (height - VIEW_HEIGHT) >> 1;
		mx -= leftPos;
		my -= topPos;

		hoverCancel = 133 <= mx && mx <= 148 && 5 <= my && my <= 20;
		hoveredSlot = -1;
		if (5 <= mx && mx <= 148) {
			int hoverSlotX = (mx - 5) / 16;
			if (25 <= my && my <= 72)
				hoveredSlot = (my - 25) / 16 * 9 + 9 + hoverSlotX;
			if (77 <= my && my <= 92)
				hoveredSlot = hoverSlotX;
		}
	}

	@Override
	public void render(PoseStack stack, int mx, int my, float partialTick) {
		updateFields(mx, my);

		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);

		DeepPocketUtils.setRenderShaderColor(color);
		Sprites.OUTLINE.blit(stack, leftPos, topPos);
		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		Sprites.BASE.blit(stack, leftPos, topPos);
		(hoverCancel ? Sprites.CANCEL_H : Sprites.CANCEL_N).blit(stack, leftPos + 133, topPos + 5);

		for (int i = 0; i < 36; i++) {
			int slotX = leftPos + 5 + (i % 9) * 16;
			int slotY = topPos + (i < 9 ? 77 : 25 + (i - 9) / 9 * 16);

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, TEXTURE);
			DeepPocketUtils.setRenderShaderColor(0xFFFFFF);

			(hoveredSlot == i ? Sprites.SLOT_H : Sprites.SLOT_N).blit(stack, slotX, slotY);
			itemRenderer.renderGuiItem(inventory.getItem(i), slotX, slotY);
		}

		font.draw(stack, title, leftPos + 5, topPos + 5, 0xFFFFFF);

		if (hoverCancel)
			renderTooltip(stack, Component.literal("Cancel"), mx, my);
		else if (hoveredSlot >= 0) {
			ItemStack itemStack = inventory.getItem(hoveredSlot);
			if (!itemStack.isEmpty())
				renderTooltip(stack, itemStack, mx, my);
		}
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		updateFields((int)mx, (int)my);
		if (hoverCancel) {
			DeepPocketUtils.playClickSound();
			onCancel.run();
			return true;
		}
		if (hoveredSlot >= 0) {
			DeepPocketUtils.playClickSound();
			onSelect.accept(inventory.getItem(hoveredSlot));
			return true;
		}
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int Modifiers) {
		if (keyCode == InputConstants.KEY_ESCAPE) {
			onCancel.run();
			return true;
		}
		return false;
	}

	@Override
	public void onClose() {}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private enum Sprites {
		BASE(0, 0, 154, 98),
		OUTLINE(0, 98, 154, 98),
		CANCEL_N(240, 0, 16, 16), CANCEL_H(240, 16, 16, 16),
		SLOT_N(224, 0, 16, 16), SLOT_H(224, 16, 16, 16),
		;

		private final int u, v, w, h;

		Sprites(int u, int v, int w, int h) {
			this.u = u;
			this.v = v;
			this.w = w;
			this.h = h;
		}

		private void blit(PoseStack stack, int x, int y) {
			Screen.blit(stack, x, y, u, v, w, h, 256, 256);
		}
	}
}
