package com.ofek2608.deep_pocket.client.client_screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.LongConsumer;

class NumberSelectionScreen extends Screen {
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/select_number.png");
	private static final int VIEW_WIDTH = 126;
	private static final int VIEW_HEIGHT = 50;
	private final Component title;
	private final int color;
	private final LongConsumer onSelect;
	private final long initialValue;

	//Update Fields
	private int leftPos;
	private int topPos;
	private boolean hoverNumber;
	private boolean hoverBtnCancel, hoverBtnConfirm;
	//Focus
	private boolean focusNumber;
	private long count;


	NumberSelectionScreen(Component title, int color, long initialValue, LongConsumer onSelect) {
		super(Component.empty());
		this.title = title;
		this.color = color;
		this.initialValue = initialValue;
		this.onSelect = onSelect;

		this.count = initialValue;
	}

	private void updateFields(int mx, int my) {
		leftPos = (width - VIEW_WIDTH) >> 1;
		topPos = (height - VIEW_HEIGHT) >> 1;
		mx -= leftPos;
		my -= topPos;
		hoverNumber = 5 <= mx && mx <= 120 && 15 <= my && my <= 24;
		boolean inRow = 29 <= my && my <= 44;
		hoverBtnCancel = 85 <= mx && mx <= 100 && inRow;
		hoverBtnConfirm = 105 <= mx && mx <= 120 && inRow;
	}

	@Override
	public void render(PoseStack stack, int mx, int my, float partialTick) {
		updateFields(mx, my);

		renderBackground(stack);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);

		DeepPocketUtils.setRenderShaderColor(color);
		Sprites.OUTLINE.blit(stack, leftPos, topPos);

		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		Sprites.BASE.blit(stack, leftPos, topPos);
		(hoverBtnCancel ? Sprites.BTN_CANCEL_H : Sprites.BTN_CANCEL_N).blit(stack, leftPos + 85, topPos + 29);
		(hoverBtnConfirm ? Sprites.BTN_CONFIRM_H : Sprites.BTN_CONFIRM_N).blit(stack, leftPos + 105, topPos + 29);

		font.draw(stack, title, leftPos + 5, topPos + 5, 0xFFFFFF);
		boolean displayMark = focusNumber && ("" + count).length() < 19;
		font.draw(stack, (count == 0 ? "" : count < 0 ? "Inf" : "" + count) + (displayMark ? DeepPocketUtils.getTimedTextEditSuffix() : ""), leftPos + 6, topPos + 16, 0xDDDDDD);

		if (hoverBtnCancel) renderTooltip(stack, Component.literal("Cancel"), mx, my);
		if (hoverBtnConfirm) renderTooltip(stack, Component.literal("Confirm"), mx, my);
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
		if (hoverBtnCancel) {
			DeepPocketUtils.playClickSound();
			pressCancel();
			return true;
		}
		if (hoverBtnConfirm) {
			DeepPocketUtils.playClickSound();
			pressConfirm();
			return true;
		}
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int Modifiers) {
		switch (keyCode) {
			case InputConstants.KEY_ESCAPE -> pressCancel();
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

	public void pressCancel() {
		count = initialValue;
		onClose();
	}

	public void pressConfirm() {
		onClose();
	}

	@Override
	public void onClose() {
		onSelect.accept(count);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private static enum Sprites {
		BASE(0, 0, 126, 50),
		OUTLINE(0, 50, 126, 50),
		BTN_CANCEL_N(224, 0, 16, 16), BTN_CANCEL_H(224, 16, 16, 16),
		BTN_CONFIRM_N(240, 0, 16, 16), BTN_CONFIRM_H(240, 16, 16, 16),
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
