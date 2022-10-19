package com.ofek2608.deep_pocket.registry.interfaces.crafter;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CrafterScreen extends AbstractContainerScreen<CrafterMenu> {
	//FIXME make the screen look more like the other screens
	private static final ResourceLocation CONTAINER_BACKGROUND = DeepPocketMod.loc("textures/gui/crafter_generic_54.png");
	private final int containerRows;

	public CrafterScreen(CrafterMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.passEvents = false;
		this.containerRows = menu.getRowCount();
		this.imageHeight = 114 + this.containerRows * 18;
		this.inventoryLabelY = this.imageHeight - 94;
//		this.imageWidth = 154;
//		this.imageHeight = 30 + this.containerRows * 16;
	}

	public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		this.renderBackground(pPoseStack);
		super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
		this.renderTooltip(pPoseStack, pMouseX, pMouseY);
	}

//	@Override
//	protected void renderLabels(PoseStack poseStack, int mx, int my) {
//		this.font.draw(poseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 0xFFFFFF);
//	}

	protected void renderBg(PoseStack poseStack, float partialTick, int mx, int my) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, CONTAINER_BACKGROUND);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		this.blit(poseStack, i, j, 0, 0, this.imageWidth, this.containerRows * 18 + 17);
		this.blit(poseStack, i, j + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);
	}

//	private void renderOutline(PoseStack poseStack) {
//
//	}












	private enum Sprites {
		//Outline
		OUTLINE_TOP(0,0,154,1),
		OUTLINE_MIDDLE(0,1,154,1),
		OUTLINE_BOTTOM(0,2,154,1),
		//Frame
		TOP(0,0,152,14),
		GAP_SMALL(0,14,152,4),
		ROW_SMALL(0,18,152,16),
		GAP_SCROLL(0,14,164,4),
		ROW_SCROLL(0,18,164,16),
		BUTTONS_FRAME(202,0,14,62),
		CRAFTING_FRAME(0,43,152,48),
		CREATE_PATTERN_FRAME(0,91,152,48),
		//Slot Base
		SLOT_BASE_N(186, 0, 16, 16),
		SLOT_BASE_H(186, 16, 16, 16),
		SLOT_BASE_CRAFTING_OUTPUT_N(152, 43, 24, 24),
		SLOT_BASE_CRAFTING_OUTPUT_H(152, 67, 24, 24),
		//Scroll
		SCROLL_N(152,10,8,2),
		SCROLL_H(152,12,8,2),
		//Buttons
		SEARCH_MODE_0N(216, 0, 10, 10),SEARCH_MODE_0H(216, 30, 10, 10),
		SEARCH_MODE_1N(226, 0, 10, 10),SEARCH_MODE_1H(226, 30, 10, 10),
		SEARCH_MODE_2N(236, 0, 10, 10),SEARCH_MODE_2H(236, 30, 10, 10),
		SEARCH_MODE_3N(246, 0, 10, 10),SEARCH_MODE_3H(246, 30, 10, 10),
		SORTING_ORDER_0N(216, 10, 10, 10),SORTING_ORDER_0H(216, 40, 10, 10),
		SORTING_ORDER_1N(226, 10, 10, 10),SORTING_ORDER_1H(226, 40, 10, 10),
		SORTING_ORDER_2N(236, 10, 10, 10),SORTING_ORDER_2H(236, 40, 10, 10),
		SORTING_ORDER_3N(246, 10, 10, 10),SORTING_ORDER_3H(246, 40, 10, 10),
		SORT_ASCENDING_0N(216, 20, 10, 10),SORT_ASCENDING_0H(216, 50, 10, 10),
		SORT_ASCENDING_1N(226, 20, 10, 10),SORT_ASCENDING_1H(226, 50, 10, 10),
		DISPLAY_CRAFTING_0N(236, 20, 10, 10),DISPLAY_CRAFTING_0H(236, 50, 10, 10),
		DISPLAY_CRAFTING_1N(246, 20, 10, 10),DISPLAY_CRAFTING_1H(246, 50, 10, 10),
		DISPLAY_CRAFTING_2N(236, 60, 10, 10),DISPLAY_CRAFTING_2H(246, 60, 10, 10),
		SETTINGS_N(216, 60, 10, 10),SETTINGS_H(226, 60, 10, 10),
		CLEAR_UN(224, 70, 16, 16),CLEAR_UH(240, 70, 16, 16),
		CLEAR_DN(224, 86, 16, 16),CLEAR_DH(240, 86, 16, 16),
		BULK_CRAFTING_N(224, 102, 16, 16),BULK_CRAFTING_H(240, 102, 16, 16),BULK_CRAFTING_D(208, 102, 16, 16),
		CLEAR_PATTERN_N(224, 118, 16, 16),CLEAR_PATTERN_H(240, 118, 16, 16),
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
