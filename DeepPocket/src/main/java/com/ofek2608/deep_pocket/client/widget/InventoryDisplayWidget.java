package com.ofek2608.deep_pocket.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public class InventoryDisplayWidget implements Widget, GuiEventListener, NonNarratableEntry {
	public static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/widget/inventory.png");
	public int offX, offY;
	private final int slotIndex;
	private final MenuItemRenderer renderer;
	
	private int hoveredSlotIndex = -1;
	
	public InventoryDisplayWidget(int slotIndex, MenuItemRenderer renderer) {
		this.slotIndex = slotIndex;
		this.renderer = renderer;
	}
	
	private void updateHoverSlotIndex(int mx, int my) {
		mx -= offX;
		my -= offY;
		
		int slotX, slotY;
		if (4 <= mx && mx < 148) {
			slotX = (mx - 4) / 16;
		} else {
			hoveredSlotIndex = -1;
			return;
		}
		if (4 <= my && my < 52) {
			slotY = (my - 4) / 16;
		} else if (56 <= my && my < 72) {
			slotY = 3;
		} else {
			hoveredSlotIndex = -1;
			return;
		}
		hoveredSlotIndex = slotX + 9 * slotY;
	}
	
	public void renderBaseRow(PoseStack poseStack, int addY, int slotIndex) {
		for (int i = 0; i < 9; i++) {
			Screen.blit(
					poseStack,
					offX + 4 + i * 16, offY + addY,
					240, hoveredSlotIndex == slotIndex + i ? 16 : 0,
					16, 16,
					256, 256
			);
		}
	}
	
	public void renderItemRow(PoseStack poseStack, int addY, int slotIndex) {
		for (int i = 0; i < 9; i++)
			renderer.renderItem(poseStack, offX + 4 + 16 * i, offY + addY, slotIndex + i);
	}
	
	@Override
	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		updateHoverSlotIndex(mx, my);
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);
		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		
		//Background
		Screen.blit(
				poseStack,
				offX, offY,
				0, 0,
				152, 76,
				256, 256
		);
		
		renderBaseRow(poseStack, 4, 0);
		renderBaseRow(poseStack, 20, 9);
		renderBaseRow(poseStack, 36, 18);
		renderBaseRow(poseStack, 56, 27);
		
		renderItemRow(poseStack, 4, 0);
		renderItemRow(poseStack, 20, 9);
		renderItemRow(poseStack, 36, 18);
		renderItemRow(poseStack, 56, 27);
	}
	
	public int getHoveredSlotIndex() {
		return hoveredSlotIndex < 0 ? -1 : slotIndex + hoveredSlotIndex;
	}
}
