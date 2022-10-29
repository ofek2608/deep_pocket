package com.ofek2608.deep_pocket.client_screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.struct.CraftingPattern;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import com.ofek2608.deep_pocket.registry.items.crafting_pattern.CraftingPatternItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

class RecipeSelectionScreen extends Screen {
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/select_recipe.png");
	private final DeepPocketClientHelper dpClientHelper = DeepPocketClientHelper.get();
	private static final int DISPLAY_PATTERN_COUNT = 8;
	private static final int VIEW_WIDTH = 218;
	private static final int VIEW_HEIGHT = 42 + DISPLAY_PATTERN_COUNT * 16;
	private final Player player;
	private final Pocket pocket;
	private final ItemType requiredOutput;
	private final Consumer<CraftingPattern> onSelect;

	//Update Fields
	private int leftPos;
	private int topPos;
	private boolean hoverPrevPage;
	private boolean hoverNextPage;
	private int hoveredPattern;
	private int pageCount;
	private int pageIndex;
	private final CraftingPattern[] visiblePatterns = new CraftingPattern[DISPLAY_PATTERN_COUNT];


	RecipeSelectionScreen(Player player, Pocket pocket, ItemType requiredOutput, Consumer<CraftingPattern> onSelect) {
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
				visiblePatterns[i] = null;
			return;
		}

		//find patterns of the page
		List<CraftingPattern> patterns = pocket.getPatternsMap().values().stream().filter(this::filterOutput).filter(DeepPocketUtils.distinctByKey(CraftingPattern::getInputCountMap)).toList();
		pageCount = (patterns.size() + DISPLAY_PATTERN_COUNT - 1) / DISPLAY_PATTERN_COUNT;
		if (pageCount == 0) pageCount = 1;
		pageIndex = Math.max(Math.min(pageIndex, pageCount - 1), 0);
		for (int i = 0; i < DISPLAY_PATTERN_COUNT; i++) {
			int pocketIndex = pageIndex * DISPLAY_PATTERN_COUNT + i;
			visiblePatterns[i] = pocketIndex < patterns.size() ? patterns.get(pocketIndex) : null;
		}
	}

	private boolean filterOutput(CraftingPattern pattern) {
		for (ItemTypeAmount output : pattern.getOutput())
			if (output.getItemType().equals(requiredOutput))
				return true;
		return false;
	}

	@Override
	public void render(PoseStack stack, int mx, int my, float partialTick) {
		updateFields(mx, my);

		renderBackground(stack);

		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);

		//outline
		DeepPocketUtils.setRenderShaderColor(pocket.getColor());
		Sprites.OUTLINE_TOP.blit(stack, leftPos, topPos);
		Sprites.OUTLINE_MIDDLE.blit(stack, leftPos, topPos + 1, VIEW_WIDTH, VIEW_HEIGHT - 2);
		Sprites.OUTLINE_BOTTOM.blit(stack, leftPos, topPos + VIEW_HEIGHT - 1);

		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		//frame
		Sprites.FRAME_TOP.blit(stack, leftPos, topPos + 1);
		Sprites.FRAME_MIDDLE.blit(stack, leftPos, topPos + 1 + Sprites.FRAME_TOP.h, VIEW_WIDTH, VIEW_HEIGHT - 2 - Sprites.FRAME_TOP.h - Sprites.FRAME_BOTTOM.h);
		Sprites.FRAME_BOTTOM.blit(stack, leftPos, topPos + VIEW_HEIGHT - 1 - Sprites.FRAME_BOTTOM.h);

		//buttons
		(hoverPrevPage ? Sprites.PREV_PAGE_H : Sprites.PREV_PAGE_N).blit(stack, leftPos + 72, topPos + VIEW_HEIGHT - 21);
		(hoverNextPage ? Sprites.NEXT_PAGE_H : Sprites.NEXT_PAGE_N).blit(stack, leftPos + 130, topPos + VIEW_HEIGHT - 21);
		for (int i = 0; i < DISPLAY_PATTERN_COUNT; i++)
			(hoveredPattern == i ? Sprites.PATTERN_H : Sprites.PATTERN_N).blit(stack, leftPos, topPos + 1 + Sprites.FRAME_TOP.h + 16 * i);

		//patterns
		for (int i = 0; i < DISPLAY_PATTERN_COUNT; i++) {
			int y = topPos + 1 + Sprites.FRAME_TOP.h + 16 * i;

			CraftingPattern pattern = visiblePatterns[i];
			if (pattern == null)
				continue;
			var inputCounts = new ArrayList<>(pattern.getInputCountMap().entrySet());
			var outputCounts = pattern.getOutputCountMap();
			if (!outputCounts.containsKey(requiredOutput))
				continue;

			long outputCount = outputCounts.get(requiredOutput);
			boolean hasMoreInput = inputCounts.size() > 9;
			boolean hasMoreOutput = outputCounts.size() > 1;
			//more items
			if (hasMoreInput || hasMoreOutput) {
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, TEXTURE);
				DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
				if (hasMoreInput)
					Sprites.MORE_ITEMS.blit(stack, leftPos + 149, y);
				if (hasMoreOutput)
					Sprites.MORE_ITEMS.blit(stack, leftPos + 197, y);
			}
			//input
			for (int j = 0; j < 9 && j < inputCounts.size(); j++) {
				var entry = inputCounts.get(j);
				dpClientHelper.renderItemAmount(
								stack,
								leftPos + 5 + 16 * j, y,
								entry.getKey().create(), entry.getValue(),
								itemRenderer, font
				);
			}
			//output
			dpClientHelper.renderItemAmount(
							stack,
							leftPos + 181, y,
							requiredOutput.create(), outputCount,
							itemRenderer, font
			);
		}
		//text
		font.draw(stack, "Select Recipe", leftPos + 5, topPos + 5, 0xDDDDDD);
		drawPageText(stack, "/", leftPos + 109, topPos + VIEW_HEIGHT - 17);
		drawPageText(stack, "" + (pageIndex + 1), leftPos + 99, topPos + VIEW_HEIGHT - 17);
		drawPageText(stack, "" + pageCount, leftPos + 119, topPos + VIEW_HEIGHT - 17);

		//tooltip
		if (hoverNextPage)
			renderTooltip(stack, Component.literal("Next Page"), mx, my);
		if (hoverPrevPage)
			renderTooltip(stack, Component.literal("Previous Page"), mx, my);
		if (0 <= hoveredPattern && hoveredPattern < visiblePatterns.length) {
			CraftingPattern pattern = visiblePatterns[hoveredPattern];
			if (pattern != null) {
				ItemStack displayItemTooltip = CraftingPatternItem.createItem(pattern.getInput(), pattern.getOutput());
				displayItemTooltip.setHoverName(Component.literal("Select").withStyle(Style.EMPTY.withItalic(false)));
				renderTooltip(stack, displayItemTooltip, mx, my);
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
			CraftingPattern pattern = visiblePatterns[hoveredPattern];
			if (pattern != null) {
				DeepPocketUtils.playClickSound();
				selectPattern(pattern);
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

	private void selectPattern(CraftingPattern pattern) {
		onSelect.accept(pattern);
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

		private void blit(PoseStack stack, int x, int y, int displayW, int displayH) {
			Screen.blit(stack, x, y, displayW, displayH, u, v, w, h, 256, 256);
		}
	}
}
