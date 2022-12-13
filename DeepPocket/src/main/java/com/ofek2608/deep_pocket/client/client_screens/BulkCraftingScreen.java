package com.ofek2608.deep_pocket.client.client_screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.Knowledge0;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class BulkCraftingScreen extends Screen {
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/bulk_crafting.png");
	private static final int VIEW_WIDTH = 154;
	private static final int VIEW_HEIGHT = 50;
	private final @Nullable Screen backScreen;
	private final @Nonnull Pocket pocket;
	private final @Nonnull ElementType.TItem[] recipe;

	//Update Fields
	private int leftPos;
	private int topPos;
	private boolean hoverNumber;
	private boolean hoverBtn0, hoverBtn1, hoverBtn2, hoverBtn3, hoverBtn4;
	//Focus
	private boolean focusNumber;
	private long count;


	BulkCraftingScreen(@Nullable Screen backScreen, @Nonnull Pocket pocket, @Nonnull ElementType.TItem[] recipe) {
		super(Component.empty());
		this.backScreen = backScreen;
		this.pocket = pocket;
		this.recipe = recipe;
	}

	private void updateFields(int mx, int my) {
		leftPos = (width - VIEW_WIDTH) >> 1;
		topPos = (height - VIEW_HEIGHT) >> 1;
		mx -= leftPos;
		my -= topPos;
		hoverNumber = 33 <= mx && mx <= 148 && 15 <= my && my <= 24;
		boolean inRow = 29 <= my && my <= 44;
		hoverBtn0 = 33 <= mx && mx <= 48 && inRow;
		hoverBtn1 = 53 <= mx && mx <= 68 && inRow;
		hoverBtn2 = 73 <= mx && mx <= 88 && inRow;
		hoverBtn3 = 113 <= mx && mx <= 128 && inRow;
		hoverBtn4 = 133 <= mx && mx <= 148 && inRow;
	}

	@Override
	public void render(PoseStack stack, int mx, int my, float partialTick) {
		updateFields(mx, my);

		renderBackground(stack);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);

		DeepPocketUtils.setRenderShaderColor(pocket.getColor());
		Sprites.OUTLINE.blit(stack, leftPos, topPos);

		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		Sprites.BASE.blit(stack, leftPos, topPos);
		(hoverBtn0 ? Sprites.BTN0_H : Sprites.BTN0_N).blit(stack, leftPos + 33, topPos + 29);
		(hoverBtn1 ? Sprites.BTN1_H : Sprites.BTN1_N).blit(stack, leftPos + 53, topPos + 29);
		(hoverBtn2 ? Sprites.BTN2_H : Sprites.BTN2_N).blit(stack, leftPos + 73, topPos + 29);
		(hoverBtn3 ? Sprites.BTN3_H : Sprites.BTN3_N).blit(stack, leftPos + 113, topPos + 29);
		(hoverBtn4 ? Sprites.BTN4_H : Sprites.BTN4_N).blit(stack, leftPos + 133, topPos + 29);

		font.draw(stack, "Bulk Crafting", leftPos + 5, topPos + 5, 0xFFFFFF);
		font.draw(stack, "Craft", leftPos + 5, topPos + 16, 0xFFFFFF);
		boolean displayMark = focusNumber && ("" + count).length() < 19;
		font.draw(stack, (count == 0 ? "" : count < 0 ? "Inf" : "" + count) + (displayMark ? DeepPocketUtils.getTimedTextEditSuffix() : ""), leftPos + 34, topPos + 16, 0xDDDDDD);

		if (hoverBtn0) renderTooltip(stack, Component.literal("Set to 25%"), mx, my);
		if (hoverBtn1) renderTooltip(stack, Component.literal("Set to 50%"), mx, my);
		if (hoverBtn2) renderTooltip(stack, Component.literal("Set to 100%"), mx, my);
		if (hoverBtn3) renderTooltip(stack, Component.literal("Cancel"), mx, my);
		if (hoverBtn4) renderTooltip(stack, Component.literal("Craft"), mx, my);
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		updateFields((int)mx, (int)my);
		focusNumber = false;
		Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
		if (hoverNumber) {
			focusNumber = true;
			Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
			return true;
		}
		Knowledge0 knowledge = DeepPocketClientApi.get().getKnowledge();
		if (hoverBtn0) {
			DeepPocketUtils.playClickSound();
			count = pocket.getMaxExtract(knowledge, recipe) >> 2;
			return true;
		}
		if (hoverBtn1) {
			DeepPocketUtils.playClickSound();
			count = pocket.getMaxExtract(knowledge, recipe) >> 1;
			return true;
		}
		if (hoverBtn2) {
			DeepPocketUtils.playClickSound();
			count = pocket.getMaxExtract(knowledge, recipe);
			return true;
		}
		if (hoverBtn3) {
			DeepPocketUtils.playClickSound();
			onClose();
			return true;
		}
		if (hoverBtn4) {
			DeepPocketUtils.playClickSound();
			DeepPocketPacketHandler.sbBulkCrafting(count);
			onClose();
			return true;
		}
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int Modifiers) {
		switch (keyCode) {
			case InputConstants.KEY_ESCAPE -> onClose();
			case InputConstants.KEY_BACKSPACE -> erase();
			default -> { return false; }
		}
		return true;
	}

	private void erase() {
		if (focusNumber) {
			count /= 10;
		}
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (focusNumber) {
			if (codePoint == 'i' || codePoint == 'I' || codePoint == '-') {
				count = -1;
				return true;
			}
			if (codePoint < '0' || '9' < codePoint)
				return true;
			if (count < 0)
				count = 0;
			int digit = codePoint - '0';
			//Long.MAX_VALUE == 9223372036854775807
			count = count <= (digit <= 7 ? 922337203685477580L : 922337203685477579L) ? count * 10 + digit : -1;
			return true;
		}
		return false;
	}

	@Override
	public void onClose() {
		if (minecraft != null)
			minecraft.setScreen(backScreen);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private static enum Sprites {
		BASE(0, 0, 154, 50),
		OUTLINE(0, 50, 154, 50),
		BTN0_N(176, 0, 16, 16), BTN0_H(176, 16, 16, 16),
		BTN1_N(192, 0, 16, 16), BTN1_H(192, 16, 16, 16),
		BTN2_N(208, 0, 16, 16), BTN2_H(208, 16, 16, 16),
		BTN3_N(224, 0, 16, 16), BTN3_H(224, 16, 16, 16),
		BTN4_N(240, 0, 16, 16), BTN4_H(240, 16, 16, 16),
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
