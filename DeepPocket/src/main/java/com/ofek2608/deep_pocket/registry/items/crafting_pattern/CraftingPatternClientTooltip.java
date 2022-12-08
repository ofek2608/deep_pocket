package com.ofek2608.deep_pocket.registry.items.crafting_pattern;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;

public class CraftingPatternClientTooltip implements ClientTooltipComponent {
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/tooltip/crafting_pattern.png");
	private final DeepPocketClientHelper dpClientHelper = DeepPocketClientHelper.get();
	private final CraftingPatternTooltip param;
	private final int gridSizeInput;
	private final int gridSizeOutput;

	public CraftingPatternClientTooltip(CraftingPatternTooltip param) {
		this.param = param;
		this.gridSizeInput = calcGridSize(param.input.length);
		this.gridSizeOutput = calcGridSize(param.output.length);
	}

	private static int calcGridSize(int length) {
		return DeepPocketUtils.sqrt(length - 1) + 1;
	}

	@Override
	public int getHeight() {
		return 12 + Math.max(gridSizeInput, gridSizeOutput) * 16;
	}

	@Override
	public int getWidth(Font font) {
		return 46 + (gridSizeInput + gridSizeOutput) * 16;
	}

	@Override
	public void renderImage(Font font, int mx, int my, PoseStack poseStack, ItemRenderer itemRenderer, int blitOffset) {
		int totalW = getWidth(font);
		int totalH = getHeight() - 2;

		poseStack.pushPose();
		poseStack.translate(mx, my, blitOffset);
		renderBackground(poseStack, gridSizeInput, gridSizeOutput, totalW, totalH);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0, 0, blitOffset);
		renderItems(gridSizeInput, gridSizeOutput, totalW, totalH, (x,y,slotIndex)->{
			if (slotIndex < param.input.length)
				dpClientHelper.renderItemAmount(poseStack, mx + x, my + y, param.input[slotIndex], itemRenderer, font);
		}, (x,y,slotIndex)->{
			if (slotIndex < param.output.length)
				dpClientHelper.renderItemAmount(poseStack, mx + x, my + y, param.output[slotIndex], itemRenderer, font);
		});
		poseStack.popPose();
	}

	private static void renderBackground(PoseStack poseStack, int gridSizeInput, int gridSizeOutput, int w, int h) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.setShaderColor(1, 1, 1, 1);

		renderOutline(poseStack, w, h);
		renderGridBackground(poseStack, 5, gridSizeInput, h);
		renderArrow(poseStack, 5 + 16 * gridSizeInput, h);
		renderGridBackground(poseStack, 41 + 16 * gridSizeInput, gridSizeOutput, h);
	}

	private static void renderItems(int gridSizeInput, int gridSizeOutput, int w, int h, SlotItemRenderer inputRenderer, SlotItemRenderer outputRenderer) {
		renderGridItems(5, gridSizeInput, h, inputRenderer);
		renderGridItems(41 + 16 * gridSizeInput, gridSizeOutput, h, outputRenderer);
	}

	private static void renderOutline(PoseStack poseStack, int w, int h) {
		Sprites.OUTLINE_TOP.blit(poseStack, 0, 0, w, 1);
		Sprites.OUTLINE_BOTTOM.blit(poseStack, 0, h - 1, w, 1);
		Sprites.OUTLINE_LEFT.blit(poseStack, 0, 1, 1, h - 2);
		Sprites.OUTLINE_RIGHT.blit(poseStack, w - 1, 1, 1, h - 2);
		Sprites.FRAME_LEFT.blit(poseStack, 1, 1, 4, h - 2);
		Sprites.FRAME_RIGHT.blit(poseStack, w - 5, 1, 4, h - 2);
	}

	private static void renderArrow(PoseStack poseStack, int x, int h) {
		int y = (h - 18) / 2;
		Sprites.FRAME_ARROW_TOP.blit(poseStack, x, 1, 36, y);
		Sprites.CONTENT_ARROW.blit(poseStack, x, y + 1);
		Sprites.FRAME_ARROW_BOTTOM.blit(poseStack, x, y + 17, 36, h - y - 18);
	}

	private static void renderGridBackground(PoseStack poseStack, int x, int gridSize, int h) {
		int gridSize16 = 16 * gridSize;
		int y = (h - 2 - gridSize16) / 2;
		Sprites.FRAME_SLOT_TOP.blit(poseStack, x, 1, gridSize16, y);
		Sprites.FRAME_SLOT_BOTTOM.blit(poseStack, x, y + 1 + gridSize16, gridSize16, h - y - 2 - gridSize16);
		for (int slotY = 0; slotY < gridSize; slotY++)
			for (int slotX = 0; slotX < gridSize; slotX++)
				Sprites.CONTENT_SLOT.blit(poseStack, x + 16 * slotX, y + 1 + 16 * slotY);
	}

	private static void renderGridItems(int x, int gridSize, int h, SlotItemRenderer renderer) {
		int y = (h - 2 - 16 * gridSize) / 2 + 1;
		int slotIndex = 0;
		for (int slotY = 0; slotY < gridSize; slotY++)
			for (int slotX = 0; slotX < gridSize; slotX++)
				renderer.render(x + slotX * 16, y + slotY * 16, slotIndex++);
	}

	private interface SlotItemRenderer {
		public void render(int x, int y, int index);
	}


	private enum Sprites {
		//Outline
		OUTLINE_TOP(0,0,62,1),
		OUTLINE_BOTTOM(0,25,62,1),
		OUTLINE_LEFT(0,1,1,24),
		OUTLINE_RIGHT(0,1,1,24),
		//Frame
		FRAME_LEFT(1,1,4,24),
		FRAME_RIGHT(57,1,4,24),
		FRAME_SLOT_TOP(5,1,16,4),
		FRAME_SLOT_BOTTOM(5,21,16,4),
		FRAME_ARROW_TOP(21,1,36,4),
		FRAME_ARROW_BOTTOM(21,21,36,4),
		//Content
		CONTENT_SLOT(5,5,16,16),
		CONTENT_ARROW(21,5,36,16),
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
