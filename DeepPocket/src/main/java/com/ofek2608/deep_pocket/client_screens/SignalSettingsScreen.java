package com.ofek2608.deep_pocket.client_screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.SignalSettings;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class SignalSettingsScreen extends Screen {
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/signal.png");
	private static final int VIEW_WIDTH = 186;
	private static final int VIEW_HEIGHT = 26;
	private final @Nullable Screen backScreen;
	private final @Nonnull Entity entity;
	private final int color;
	private final @Nonnull BlockPos pos;
	private final @Nonnull SignalSettings settings;

	//Update Fields
	private int leftPos;
	private int topPos;
	private boolean hoverFirst;
	private boolean hoverOperator;
	private boolean hoverSecond;
	private boolean hoverMode;
	//Focus
	private boolean twoItemsMode;
	private boolean focusNumber;


	SignalSettingsScreen(@Nullable Screen backScreen, @Nonnull Entity entity, int color, @Nonnull BlockPos pos, @Nonnull SignalSettings settings) {
		super(Component.empty());
		this.backScreen = backScreen;
		this.entity = entity;
		this.color = color;
		this.pos = pos;
		this.settings = settings;
		this.twoItemsMode = settings.secondItem != null;
	}

	private void updateFields(int mx, int my) {
		leftPos = (width - VIEW_WIDTH) >> 1;
		topPos = (height - VIEW_HEIGHT) >> 1;
		mx -= leftPos;
		my -= topPos;

		boolean inRow = 5 <= my && my <= 20;
		if (twoItemsMode) {
			hoverFirst = 65 <= mx && mx <= 80 && inRow;
			hoverOperator = 85 <= mx && mx <= 100 && inRow;
			hoverSecond = 105 <= mx && mx <= 120 && inRow;
		} else {
			hoverFirst = 5 <= mx && mx <= 20 && inRow;
			hoverOperator = 25 <= mx && mx <= 40 && inRow;
			hoverSecond = 45 <= mx && mx <= 160 && 8 <= my && my <= 17;
		}
		hoverMode = 165 <= mx && mx <= 180 && inRow;
	}

	@Override
	public void render(PoseStack stack, int mx, int my, float partialTick) {
		updateFields(mx, my);

		renderBackground(stack);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);

		DeepPocketUtils.setRenderShaderColor(color);
		(twoItemsMode ? Sprites.OUTLINE_1 : Sprites.OUTLINE_0).blit(stack, leftPos, topPos);

		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		(twoItemsMode ? Sprites.BASE_1 : Sprites.BASE_0).blit(stack, leftPos, topPos);
		int offsetX = twoItemsMode ? 65 : 5;
		(hoverFirst ? Sprites.ITEM_H : Sprites.ITEM_N).blit(stack, leftPos + offsetX, topPos + 5);
		(settings.bigger ?
						hoverOperator ? Sprites.BIG_H : Sprites.BIG_N :
						hoverOperator ? Sprites.SMALL_H : Sprites.SMALL_N
		).blit(stack, leftPos + offsetX + 20, topPos + 5);
		if (twoItemsMode)
			(hoverSecond ? Sprites.ITEM_H : Sprites.ITEM_N).blit(stack, leftPos + offsetX + 40, topPos + 5);
		(hoverMode ? Sprites.MODE_H : Sprites.MODE_N).blit(stack, leftPos + 165, topPos + 5);

		itemRenderer.renderGuiItem(settings.first.create(), leftPos + offsetX, topPos + 5);
		if (twoItemsMode && settings.secondItem != null)
			itemRenderer.renderGuiItem(settings.secondItem.create(), leftPos + offsetX + 40, topPos + 5);
		if (!twoItemsMode) {
			boolean displayMark = focusNumber && ("" + settings.secondCount).length() < 19;
			font.draw(stack, (settings.secondCount == 0 ? "" : "" + settings.secondCount) + (displayMark ? DeepPocketUtils.getTimedTextEditSuffix() : ""), leftPos + 46, topPos + 9, 0xDDDDDD);
		}

		if (hoverFirst || twoItemsMode && hoverSecond)
			renderTooltip(stack, Component.literal("Set Item"), mx, my);
		if (hoverOperator)
			renderTooltip(stack, Component.literal("Emmit Redstone Signal When " + (settings.bigger ? "Above" : "Below") + "..."), mx, my);
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		updateFields((int)mx, (int)my);
		focusNumber = false;
		Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
		if (hoverFirst) {
			DeepPocketUtils.playClickSound();
			ClientScreens.selectItem(Component.literal("Select First Item"), color, this::onSelectFirst, this::onSelectCancel);
			return true;
		}
		if (hoverOperator) {
			DeepPocketUtils.playClickSound();
			settings.bigger = !settings.bigger;
			updateOnServer();
			return true;
		}
		if (hoverSecond) {
			if (twoItemsMode) {
				DeepPocketUtils.playClickSound();
				ClientScreens.selectItem(Component.literal("Select Second Item"), color, this::onSelectSecond, this::onSelectCancel);
			} else {
				focusNumber = true;
				Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
			}
			return true;
		}
		if (hoverMode) {
			DeepPocketUtils.playClickSound();
			twoItemsMode = !twoItemsMode;
			updateOnServer();
			return true;
		}
		return false;
	}

	private void onSelectFirst(ItemStack newIcon) {
		settings.first = newIcon.isEmpty() ? ItemType.EMPTY : new ItemType(newIcon);
		Minecraft.getInstance().setScreen(this);
		updateOnServer();
	}

	private void onSelectSecond(ItemStack newIcon) {
		settings.secondItem = newIcon.isEmpty() ? ItemType.EMPTY : new ItemType(newIcon);
		Minecraft.getInstance().setScreen(this);
		updateOnServer();
	}

	private void onSelectCancel() {
		Minecraft.getInstance().setScreen(this);
	}

	private void updateOnServer() {
		DeepPocketPacketHandler.sbPocketSignalSettings(pos, twoItemsMode ?
						new SignalSettings(settings.first, settings.bigger, settings.secondItem == null ? ItemType.EMPTY : settings.secondItem) :
						new SignalSettings(settings.first, settings.bigger, settings.secondCount)
		);
	}

	@Override
	public void tick() {
		if (entity.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64.0)
			onClose();
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
			settings.secondCount /= 10;
			updateOnServer();
		}
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (focusNumber) {
			if (codePoint < '0' || '9' < codePoint)
				return true;
			int digit = codePoint - '0';
			//Long.MAX_VALUE == 9223372036854775807
			settings.secondCount = settings.secondCount <= (digit <= 7 ? 922337203685477580L : 922337203685477579L) ? settings.secondCount * 10 + digit : Long.MAX_VALUE;
			updateOnServer();
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
		BASE_0(0, 0, 186, 26),
		OUTLINE_0(0, 26, 186, 26),
		BASE_1(0, 52, 186, 26),
		OUTLINE_1(0, 78, 186, 26),
		MODE_N(192, 0, 16, 16), MODE_H(192, 16, 16, 16),
		SMALL_N(208, 0, 16, 16), SMALL_H(208, 16, 16, 16),
		BIG_N(224, 0, 16, 16), BIG_H(224, 16, 16, 16),
		ITEM_N(240, 0, 16, 16), ITEM_H(240, 16, 16, 16),
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
