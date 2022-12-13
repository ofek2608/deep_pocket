package com.ofek2608.deep_pocket.client.client_screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.PocketInfo;
import com.ofek2608.deep_pocket.integration.DeepPocketFTBTeams;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class PocketSettingsScreen extends Screen {
	private static final DeepPocketClientHelper HELPER = DeepPocketClientHelper.get();
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/settings.png");
	private static final int VIEW_WIDTH = 154;
	private static final int VIEW_HEIGHT = 62;
	private final @Nullable Screen backScreen;
	private final @Nullable UUID pocketId;
	private final PocketInfo pocketInfo;

	//Update Fields
	private int leftPos;
	private int topPos;
	private boolean hoverIcon;
	private boolean hoverName;
	private int hoverSlider;
	private boolean hoverSecurityMode;
	private boolean hoverDelete;
	private boolean hoverCancel;
	private boolean hoverConfirm;
	private boolean holdingShift;
	//Focus
	private boolean focusSearch;
	private int focusSlider = -1;


	PocketSettingsScreen(@Nullable Screen backScreen, @Nullable UUID pocketId, PocketInfo pocketInfo) {
		super(Component.empty());
		this.backScreen = backScreen;
		this.pocketId = pocketId;
		this.pocketInfo = pocketInfo;
	}

	private void updateFields(int mx, int my) {
		leftPos = (width - VIEW_WIDTH) >> 1;
		topPos = (height - VIEW_HEIGHT) >> 1;
		mx -= leftPos;
		my -= topPos;

		hoverIcon = 5 <= mx && mx <= 20 && 5 <= my && my <= 20;
		hoverName = 25 <= mx && mx <= 148 && 8 <= my && my <= 17;
		hoverSlider = focusSlider >= 0 ? focusSlider : 5 <= mx && mx <= 28 && 25 <= my && my <= 56 ? (mx - 5) / 8 : -1;
		hoverSecurityMode = 41 <= mx && mx <= 81 && 25 <= my && my <= 34;
		hoverDelete = 93 <= mx && mx <= 108 && 41 <= my && my <= 56;
		hoverCancel = 113 <= mx && mx <= 128 && 41 <= my && my <= 56;
		hoverConfirm = 133 <= mx && mx <= 148 && 41 <= my && my <= 56;
		holdingShift = Screen.hasShiftDown();
	}

	@Override
	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		updateFields(mx, my);

		renderBackground(poseStack);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);

		DeepPocketUtils.setRenderShaderColor(pocketInfo.color);
		Sprites.OUTLINE.blit(poseStack, leftPos, topPos);

		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		Sprites.BASE.blit(poseStack, leftPos, topPos);
		(hoverIcon ? Sprites.ICON_H : Sprites.ICON_N).blit(poseStack, leftPos + 5, topPos + 5);
		(hoverSecurityMode ? Sprites.SECURITY_H : Sprites.SECURITY_N).blit(poseStack, leftPos + 41, topPos + 25);
		(pocketId == null ? Sprites.HIDDEN_BUTTON : hoverDelete ? Sprites.DELETE_H : Sprites.DELETE_N).blit(poseStack, leftPos + 93, topPos + 41);
		(hoverCancel ? Sprites.CANCEL_H : Sprites.CANCEL_N).blit(poseStack, leftPos + 113, topPos + 41);
		(hoverConfirm ? Sprites.CONFIRM_H : Sprites.CONFIRM_N).blit(poseStack, leftPos + 133, topPos + 41);
		for (int channelIndex = 0; channelIndex < 3; channelIndex++) {
			int sliderY = (0xFF - ((pocketInfo.color >> (8 * (2 - channelIndex))) & 0xFF)) * 30 / 0xFF;
			(hoverSlider == channelIndex ? Sprites.SLIDER_H : Sprites.SLIDER_N).blit(poseStack, leftPos + 5 + 8 * channelIndex, topPos + 25 + sliderY);
		}
		
		HELPER.renderElementType(poseStack, leftPos + 5, topPos + 5, pocketInfo.icon, itemRenderer, font);
		font.draw(poseStack, pocketInfo.name + (focusSearch ? DeepPocketUtils.getTimedTextEditSuffix() : ""), leftPos + 26, topPos + 9, 0xDDDDDD);
		int securityOffsetX = (39 - font.width(pocketInfo.securityMode.displayName)) / 2;
		font.draw(poseStack, pocketInfo.securityMode.displayName, leftPos + 42 + securityOffsetX, topPos + 26, pocketInfo.securityMode.displayColor);

		if (hoverIcon)
			renderTooltip(poseStack, Component.literal("Change Icon"), mx, my);
		if (hoverSecurityMode)
			renderTooltip(poseStack, Component.literal("Change Security Mode"), mx, my);
		if (hoverDelete && pocketId != null) {
			renderTooltip(poseStack, List.of(
							Component.literal("Delete").withStyle(holdingShift ? ChatFormatting.RED : ChatFormatting.WHITE),
							Component.literal("Warning:").withStyle(ChatFormatting.GRAY),
							Component.literal("It will delete all the items inside the pocket.").withStyle(ChatFormatting.GRAY),
							Component.empty(),
							Component.literal("[Press shift to confirm]").withStyle(ChatFormatting.GRAY)
			), Optional.empty(), mx, my);
		}
		if (hoverCancel)
			renderTooltip(poseStack, Component.literal("Cancel"), mx, my);
		if (hoverConfirm)
			renderTooltip(poseStack, Component.literal("Confirm"), mx, my);
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		updateFields((int)mx, (int)my);
		focusSearch = false;
		Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
		if (hoverIcon) {
			DeepPocketUtils.playClickSound();
			ClientScreens.selectItem(Component.literal("Select Icon"), pocketInfo.color, this::onSelectIcon, this::onSelectIconCancel);
			return true;
		}
		if (hoverName) {
			focusSearch = true;
			Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
			return true;
		}
		if (hoverSlider >= 0) {
			DeepPocketUtils.playClickSound();
			focusSlider = hoverSlider;
			mouseMoved(mx, my);
			return true;
		}
		if (hoverSecurityMode) {
			DeepPocketUtils.playClickSound();
			pocketInfo.securityMode = switch (pocketInfo.securityMode) {
				case PRIVATE -> DeepPocketFTBTeams.hasMod() ? PocketSecurityMode.TEAM : DeepPocketClientApi.get().isPermitPublicPocket() ? PocketSecurityMode.PUBLIC : PocketSecurityMode.PRIVATE;
				case TEAM -> DeepPocketClientApi.get().isPermitPublicPocket() ? PocketSecurityMode.PUBLIC : PocketSecurityMode.PRIVATE;
				case PUBLIC -> PocketSecurityMode.PRIVATE;
			};
			return true;
		}
		if (hoverDelete && pocketId != null) {
			DeepPocketUtils.playClickSound();
			if (holdingShift) {
				DeepPocketPacketHandler.sbDestroyPocket(pocketId);
				if (minecraft != null)
					minecraft.setScreen(null);
			}
			return true;
		}
		if (hoverCancel) {
			DeepPocketUtils.playClickSound();
			onClose();
			return true;
		}
		if (hoverConfirm) {
			DeepPocketUtils.playClickSound();
			sendEditToServer();
			return true;
		}
		return false;
	}

	private void onSelectIcon(ItemStack newIcon) {
		ElementType newIconElement = ElementType.item(newIcon);
		if (!newIconElement.isEmpty())
			pocketInfo.icon = newIconElement;
		Minecraft.getInstance().setScreen(this);
	}

	private void onSelectIconCancel() {
		Minecraft.getInstance().setScreen(this);
	}

	private void sendEditToServer() {
		if (pocketId == null)
			DeepPocketPacketHandler.sbCreatePocket(pocketInfo);
		else
			DeepPocketPacketHandler.sbChangePocketSettings(pocketId, pocketInfo);
		onClose();
	}



	@Override
	public boolean keyPressed(int keyCode, int scanCode, int Modifiers) {
		switch (keyCode) {
			case InputConstants.KEY_ESCAPE -> onClose();
			case InputConstants.KEY_RETURN -> sendEditToServer();
			case InputConstants.KEY_BACKSPACE -> erase();
			default -> { return false; }
		}
		return true;
	}

	@Override
	public boolean mouseReleased(double mx, double my, int button) {
		if (button != InputConstants.MOUSE_BUTTON_LEFT)
			return false;
		focusSlider = -1;
		return true;
	}

	@Override
	public void mouseMoved(double mx, double my) {
		updateFields((int)mx, (int)my);
		if (focusSlider >= 0) {
			int selectedValue = 0xFF - (int)((my - topPos - 25) / 32 * 255);
			if (selectedValue < 0x00) selectedValue = 0x00;
			if (selectedValue > 0xFF) selectedValue = 0xFF;
			int shift = 8 * (2 - focusSlider);
			pocketInfo.color = pocketInfo.color & ~(0xFF << shift) | (selectedValue << shift);
		}
	}

	private void erase() {
		if (focusSearch)
			if (pocketInfo.name.length() > 0)
				pocketInfo.name = pocketInfo.name.substring(0, pocketInfo.name.length() - 1);
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (focusSearch) {
			if (pocketInfo.name.length() < PocketInfo.MAX_NAME_LENGTH)
				pocketInfo.name += codePoint;
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
		BASE(0, 0, 154, 62),
		OUTLINE(0, 62, 154, 62),
		ICON_N(240, 0, 16, 16),
		ICON_H(240, 16, 16, 16),
		HIDDEN_BUTTON(240, 32, 16, 16),
		SECURITY_N(154, 0, 41, 10),
		SECURITY_H(154, 10, 41, 10),
		CANCEL_N(154, 20, 16, 16),
		CANCEL_H(154, 36, 16, 16),
		CONFIRM_N(170, 20, 16, 16),
		CONFIRM_H(170, 36, 16, 16),
		DELETE_N(186, 20, 16, 16),
		DELETE_H(186, 36, 16, 16),
		SLIDER_N(154, 52, 8, 2),
		SLIDER_H(154, 54, 8, 2),
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
