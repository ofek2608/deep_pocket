package com.ofek2608.deep_pocket.client.client_screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.struct.PocketInfo;
import com.ofek2608.deep_pocket.integration.DeepPocketFTBTeams;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

class PocketSelectionScreen extends Screen {
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/select_pocket.png");
	private static final int VIEW_WIDTH = 154;
	private static final int VIEW_HEIGHT = 220;
	private final @Nullable Screen backScreen;
	private final Player player;
	private final UUID playerId;

	//Update Fields
	private int leftPos;
	private int topPos;
	private int pageCount;
	private int pageIndex;
	private final Pocket[] visiblePockets = new Pocket[8];
	private boolean hoverSearch;
	private int hoveredPocket;
	private boolean hoverPrevPage;
	private boolean hoverNextPage;
	//Focus
	private final StringBuilder search = new StringBuilder();
	private boolean focusSearch;


	PocketSelectionScreen(@Nullable Screen backScreen, Player player) {
		super(Component.empty());
		this.backScreen = backScreen;
		this.player = player;
		this.playerId = player.getUUID();
	}

	private void updateFields(int mx, int my) {
		leftPos = (width - VIEW_WIDTH) >> 1;
		topPos = (height - VIEW_HEIGHT) >> 1;
		mx -= leftPos;
		my -= topPos;

		List<Pocket> pockets = DeepPocketClientApi.get().getPockets().filter(this::filterAccess).filter(this::filterSearch).sorted(this::comparePockets).toList();
		pageCount = (pockets.size() + 7) / 8;
		if (pageCount == 0) pageCount = 1;
		pageIndex = Math.max(Math.min(pageIndex, pageCount - 1), 0);
		for (int i = 0; i < 8; i++) {
			int pocketIndex = pageIndex * 8 + i;
			visiblePockets[i] = pocketIndex < pockets.size() ? pockets.get(pocketIndex) : null;
		}
		//Hover checks
		hoverSearch = false;
		hoveredPocket = -1;
		hoverPrevPage = false;
		hoverNextPage = false;
		if (5 <= mx && mx <= 148) {
			if (5 <= my && my <= 14)
				hoverSearch = true;
			if (19 <= my && my <= 194)
				hoveredPocket = (my - 19) / 22;
		}
		if (199 <= my && my <= 214) {
			if (40 <= mx && mx <= 55)
				hoverPrevPage = true;
			if (98 <= mx && mx <= 113)
				hoverNextPage = true;
		}
	}

	private boolean filterAccess(Pocket pocket) {
		return pocket.canAccess(player);
	}

	private boolean filterSearch(Pocket pocket) {
		return pocket.getName().toLowerCase().contains(search.toString().toLowerCase());
	}

	private int comparePockets(Pocket p0, Pocket p1) {
		UUID id0 = p0.getOwner();
		UUID id1 = p1.getOwner();
		boolean me0 = id0.equals(playerId);
		boolean me1 = id1.equals(playerId);
		boolean friend0 = DeepPocketFTBTeams.areInTheSameTeam(true, playerId, id0);
		boolean friend1 = DeepPocketFTBTeams.areInTheSameTeam(true, playerId, id1);
		if (me0 && !me1) return -1;
		if (me1 && !me0) return 1;
		if (friend0 && !friend1) return -1;
		if (friend1 && !friend0) return 1;
		return id0.compareTo(id1);
	}

	@Override
	public void render(PoseStack stack, int mx, int my, float partialTick) {
		updateFields(mx, my);

		renderBackground(stack);

		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);

		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		Sprites.TOP.blit(stack, leftPos, topPos);
		for (int i = 0; i < 8; i++) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, TEXTURE);

			int y = topPos + 19 + 22 * i;
			Pocket pocket = visiblePockets[i];
			DeepPocketUtils.setRenderShaderColor(pocket == null ? 0x000000 : pocket.getColor());
			Sprites.POCKET_OUTLINE.blit(stack, leftPos, y);
			DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
			Sprites.BORDERS.blit(stack, leftPos, y);
			(hoveredPocket == i ? Sprites.POCKET_H : Sprites.POCKET_N).blit(stack, leftPos, y);
			if (pocket == null)
				continue;
			String pocketName = pocket.getName();
			String ownerName = DeepPocketClientApi.get().getCachedPlayerName(pocket.getOwner());
			PocketSecurityMode securityMode = pocket.getSecurityMode();

			itemRenderer.renderGuiItem(pocket.getIcon().create(), leftPos + 8, y + 3);
			font.draw(stack, pocketName, leftPos + 26, y + 2, 0xFFFFFF);
			font.draw(stack, ownerName, leftPos + 26, y + 12, 0xFFFFFF);
			font.draw(stack, securityMode.displayName, leftPos + 26 + font.width(ownerName) + 4, y + 12, securityMode.displayColor);
		}

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);

		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);

		Sprites.BOTTOM.blit(stack, leftPos, topPos + 195);
		(hoverPrevPage ? Sprites.PREV_PAGE_H : Sprites.PREV_PAGE_N).blit(stack, leftPos + 40, topPos + 199);
		(hoverNextPage ? Sprites.NEXT_PAGE_H : Sprites.NEXT_PAGE_N).blit(stack, leftPos + 98, topPos + 199);


		font.draw(stack, search + (focusSearch ? DeepPocketUtils.getTimedTextEditSuffix() : ""), leftPos + 6, topPos + 6, 0xDDDDDD);
		drawPageText(stack, "/", leftPos + 77, topPos + 203);
		drawPageText(stack, "" + (pageIndex + 1), leftPos + 67, topPos + 203);
		drawPageText(stack, "" + pageCount, leftPos + 87, topPos + 203);


		if (hoverNextPage)
			renderTooltip(stack, Component.literal("Next Page"), mx, my);
		if (hoverPrevPage)
			renderTooltip(stack, Component.literal("Previous Page"), mx, my);
		if (0 <= hoveredPocket && hoveredPocket < visiblePockets.length && visiblePockets[hoveredPocket] != null)
			renderTooltip(stack, Component.literal("Select"), mx, my);
	}

	private void drawPageText(PoseStack stack, String text, int x, int y) {
		x -= font.width(text) / 2;
		font.draw(stack, text, x, y, 0xFFFFFF);
	}

	private void prevPage() {
		pageIndex = pageIndex == 0 ? pageCount - 1 : pageIndex - 1;
	}

	private void nextPage() {
		pageIndex = pageIndex + 1 == pageCount ? 0 : pageIndex + 1;
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		updateFields((int)mx, (int)my);
		focusSearch = false;
		Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
		if (hoverPrevPage) {
			DeepPocketUtils.playClickSound();
			prevPage();
			return true;
		}
		if (hoverNextPage) {
			DeepPocketUtils.playClickSound();
			nextPage();
			return true;
		}
		if (hoverSearch) {
			focusSearch = true;
			Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
			return true;
		}
		if (0 <= hoveredPocket && hoveredPocket < 8) {
			Pocket pocket = visiblePockets[hoveredPocket];
			if (pocket != null) {
				DeepPocketUtils.playClickSound();
				selectPocket(pocket.getPocketId());
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int Modifiers) {
		switch (keyCode) {
			case InputConstants.KEY_ESCAPE -> onClose();
			case InputConstants.KEY_BACKSPACE -> erase();
			case InputConstants.KEY_LEFT -> prevPage();
			case InputConstants.KEY_RIGHT -> nextPage();
			default -> { return false; }
		}
		return true;
	}

	private void erase() {
		if (focusSearch)
			if (search.length() > 0)
				search.setLength(search.length() - 1);
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (focusSearch) {
			if (search.length() < PocketInfo.MAX_NAME_LENGTH)
				search.append(codePoint);
			return true;
		}
		return false;
	}

	@Override
	public void onClose() {
		if (minecraft != null)
			minecraft.setScreen(backScreen);
	}

	private void selectPocket(UUID pocketId) {
		DeepPocketPacketHandler.sbSelectPocket(pocketId);
		onClose();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private enum Sprites {
		TOP(0, 0, 154, 19),
		BORDERS(0, 19, 154, 22),
		BOTTOM(0, 41, 154, 25),
		POCKET_OUTLINE(0, 66, 154, 22),
		POCKET_N(0, 88, 154, 22),
		POCKET_H(0, 110, 154, 22),
		PREV_PAGE_N(154, 0, 16, 16), PREV_PAGE_H(154, 16, 16, 16),
		NEXT_PAGE_N(170, 0, 16, 16), NEXT_PAGE_H(170, 16, 16, 16),
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
