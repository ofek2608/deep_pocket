package com.ofek2608.deep_pocket.client.client_screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import com.ofek2608.deep_pocket.api.pocket.PocketPatterns;
import com.ofek2608.deep_pocket.api.struct.CraftingPattern;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.ElementTypeStack;
import com.ofek2608.deep_pocket.api.struct.client.ClientPocket;
import com.ofek2608.deep_pocket.registry.items.crafting_pattern.CraftingPatternItem;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

class RecipeSelectionScreen extends Screen {
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/select_recipe.png");
	private final DeepPocketClientHelper dpClientHelper = DeepPocketClientHelper.get();
	private static final int DISPLAY_PATTERN_COUNT = 8;
	private static final int VIEW_WIDTH = 218;
	private static final int VIEW_HEIGHT = 42 + DISPLAY_PATTERN_COUNT * 16;
	private final Player player;
	private final ClientPocket pocket;
	private final ElementType requiredOutput;
	private final Consumer<UUID> onSelect;

	//Update Fields
	private int leftPos;
	private int topPos;
	private boolean hoverPrevPage;
	private boolean hoverNextPage;
	private int hoveredPattern;
	private int pageCount;
	private int pageIndex;
	private final UUID[] visiblePatternIds = new UUID[DISPLAY_PATTERN_COUNT];


	RecipeSelectionScreen(Player player, ClientPocket pocket, ElementType requiredOutput, Consumer<UUID> onSelect) {
		super(Component.empty());
		this.player = player;
		this.pocket = pocket;
		this.requiredOutput = requiredOutput;
		this.onSelect = onSelect;
	}

	private void updateFields(int mx, int my) {
		leftPos = (width - VIEW_WIDTH) >> 1;
		topPos = (height - VIEW_HEIGHT) >> 1;
		mx -= leftPos;
		my -= topPos;

		//hover checks
		hoverPrevPage = VIEW_HEIGHT - 21 <= my && my <= VIEW_HEIGHT - 6 && 99 <= mx && mx <= 114;
		hoverNextPage = VIEW_HEIGHT - 21 <= my && my <= VIEW_HEIGHT - 6 && 119 <= mx && mx <= 134;
		if (5 <= mx && mx <= 212 && 17 <= my && my <= 16 + 16 * DISPLAY_PATTERN_COUNT)
			hoveredPattern = (my - 17) / 16;
		else
			hoveredPattern = -1;

		//access check
		if (!pocket.canAccess(player)) {
			pageCount = 1;
			pageIndex = 0;
			for (int i = 0; i < 8; i++)
				visiblePatternIds[i] = null;
			return;
		}

		//find patterns of the page
		PocketPatterns pocketPatterns = pocket.getPatterns();
		List<UUID> patternsId = pocketPatterns.getAllPatterns()
				.stream()
				.filter(id->{
					CraftingPattern pattern = pocketPatterns.get(id);
					return pattern != null && pattern.hasOutput(requiredOutput);
				})
				.toList();
		
		pageCount = (patternsId.size() + DISPLAY_PATTERN_COUNT - 1) / DISPLAY_PATTERN_COUNT;
		if (pageCount == 0) pageCount = 1;
		pageIndex = Math.max(Math.min(pageIndex, pageCount - 1), 0);
		for (int i = 0; i < DISPLAY_PATTERN_COUNT; i++) {
			int pocketIndex = pageIndex * DISPLAY_PATTERN_COUNT + i;
			visiblePatternIds[i] = pocketIndex < patternsId.size() ? patternsId.get(pocketIndex) : null;
		}
	}

	@Override
	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		updateFields(mx, my);
		PocketPatterns pocketPatterns = pocket.getPatterns();

		renderBackground(poseStack);

		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);

		//outline
		DeepPocketUtils.setRenderShaderColor(pocket.getColor());
		Sprites.OUTLINE_TOP.blit(poseStack, leftPos, topPos);
		Sprites.OUTLINE_MIDDLE.blit(poseStack, leftPos, topPos + 1, VIEW_WIDTH, VIEW_HEIGHT - 2);
		Sprites.OUTLINE_BOTTOM.blit(poseStack, leftPos, topPos + VIEW_HEIGHT - 1);

		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		//frame
		Sprites.FRAME_TOP.blit(poseStack, leftPos, topPos + 1);
		Sprites.FRAME_MIDDLE.blit(poseStack, leftPos, topPos + 1 + Sprites.FRAME_TOP.h, VIEW_WIDTH, VIEW_HEIGHT - 2 - Sprites.FRAME_TOP.h - Sprites.FRAME_BOTTOM.h);
		Sprites.FRAME_BOTTOM.blit(poseStack, leftPos, topPos + VIEW_HEIGHT - 1 - Sprites.FRAME_BOTTOM.h);

		//buttons
		(hoverPrevPage ? Sprites.PREV_PAGE_H : Sprites.PREV_PAGE_N).blit(poseStack, leftPos + 72, topPos + VIEW_HEIGHT - 21);
		(hoverNextPage ? Sprites.NEXT_PAGE_H : Sprites.NEXT_PAGE_N).blit(poseStack, leftPos + 130, topPos + VIEW_HEIGHT - 21);
		for (int i = 0; i < DISPLAY_PATTERN_COUNT; i++)
			(hoveredPattern == i ? Sprites.PATTERN_H : Sprites.PATTERN_N).blit(poseStack, leftPos, topPos + 1 + Sprites.FRAME_TOP.h + 16 * i);

		//patterns
		for (int i = 0; i < DISPLAY_PATTERN_COUNT; i++) {
			int y = topPos + 1 + Sprites.FRAME_TOP.h + 16 * i;

			CraftingPattern pattern = pocketPatterns.get(visiblePatternIds[i]);
			if (pattern == null)
				continue;
			var inputCounts = pattern.getInputCountMap();
			var outputCounts = pattern.getOutputCountMap();
			
			ElementTypeStack outputStack = ElementTypeStack.of(requiredOutput, pattern.getOutputCount(requiredOutput));
			
			boolean hasMoreInput = inputCounts.length > 9;
			boolean hasMoreOutput = outputCounts.length > 1;
			//more items
			if (hasMoreInput || hasMoreOutput) {
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, TEXTURE);
				DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
				if (hasMoreInput)
					Sprites.MORE_ITEMS.blit(poseStack, leftPos + 149, y);
				if (hasMoreOutput)
					Sprites.MORE_ITEMS.blit(poseStack, leftPos + 197, y);
			}
			//input
			for (int j = 0; j < 9 && j < inputCounts.length; j++) {
				ElementTypeStack stack = inputCounts[j];
				dpClientHelper.renderElementTypeStack(
								poseStack,
								leftPos + 5 + 16 * j, y,
								stack,
								itemRenderer, font
				);
			}
			//output
			dpClientHelper.renderElementTypeStack(
							poseStack,
							leftPos + 181, y,
							outputStack,
							itemRenderer, font
			);
		}
		//text
		font.draw(poseStack, "Select Recipe", leftPos + 5, topPos + 5, 0xDDDDDD);
		drawPageText(poseStack, "/", leftPos + 109, topPos + VIEW_HEIGHT - 17);
		drawPageText(poseStack, "" + (pageIndex + 1), leftPos + 99, topPos + VIEW_HEIGHT - 17);
		drawPageText(poseStack, "" + pageCount, leftPos + 119, topPos + VIEW_HEIGHT - 17);

		//tooltip
		if (hoverNextPage)
			renderTooltip(poseStack, Component.literal("Next Page"), mx, my);
		if (hoverPrevPage)
			renderTooltip(poseStack, Component.literal("Previous Page"), mx, my);
		if (0 <= hoveredPattern && hoveredPattern < visiblePatternIds.length) {
			CraftingPattern pattern = pocketPatterns.get(visiblePatternIds[hoveredPattern]);
			if (pattern != null) {
				ItemStack displayItemTooltip = CraftingPatternItem.createItem(pattern.getInput(), pattern.getOutput());
				displayItemTooltip.setHoverName(Component.literal("Select").withStyle(Style.EMPTY.withItalic(false)));
				renderTooltip(poseStack, displayItemTooltip, mx, my);
			}
		}
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
		if (0 <= hoveredPattern && hoveredPattern < 8) {
			UUID patternId = visiblePatternIds[hoveredPattern];
			if (patternId != null) {
				DeepPocketUtils.playClickSound();
				selectPattern(patternId);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int Modifiers) {
		switch (keyCode) {
			case InputConstants.KEY_ESCAPE -> onClose();
			case InputConstants.KEY_LEFT -> prevPage();
			case InputConstants.KEY_RIGHT -> nextPage();
			default -> { return false; }
		}
		return true;
	}

	@Override
	public void onClose() {
		onSelect.accept(null);
	}

	private void selectPattern(UUID patternId) {
		onSelect.accept(patternId);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private enum Sprites {
		OUTLINE_TOP(0, 0, 218, 1),
		OUTLINE_MIDDLE(0, 1, 218, 1),
		OUTLINE_BOTTOM(0, 2, 218, 1),
		FRAME_TOP(0, 3, 218, 16),
		FRAME_MIDDLE(0, 19, 218, 16),
		FRAME_BOTTOM(0, 35, 218, 24),
		PATTERN_N(0, 59, 218, 16), PATTERN_H(0, 75, 218, 16),
		PREV_PAGE_N(218, 0, 16, 16), PREV_PAGE_H(218, 16, 16, 16),
		NEXT_PAGE_N(234, 0, 16, 16), NEXT_PAGE_H(234, 16, 16, 16),
		MORE_ITEMS(218, 32, 16, 16),
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

		@SuppressWarnings("SameParameterValue")
		private void blit(PoseStack stack, int x, int y, int displayW, int displayH) {
			Screen.blit(stack, x, y, displayW, displayH, u, v, w, h, 256, 256);
		}
	}
}
