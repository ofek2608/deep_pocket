package com.ofek2608.deep_pocket.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class CraftingDisplayWidget extends SimpleContainerWidget implements GuiEventListener, NonNarratableEntry {
	public static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/widget/crafting.png");
	private final int slotIndex;
	private final MenuItemRenderer renderer;
	private final Supplier<Boolean> canBulkCraft;
	private final ImageButton btnClearUp, btnClearDown, btnBulkCraft;
	
	private int hoveredSlotIndex = -1;
	
	public CraftingDisplayWidget(int slotIndex, MenuItemRenderer renderer, Runnable clearUp, Runnable clearDown, Runnable bulkCraft, Supplier<Boolean> canBulkCraft) {
		this.slotIndex = slotIndex;
		this.renderer = renderer;
		this.canBulkCraft = canBulkCraft;
		children.add(btnClearUp = new ImageButton(0, 0, 16, 16, 208, 0, TEXTURE, btn -> clearUp.run()));
		children.add(btnClearDown = new ImageButton(0, 0, 16, 16, 224, 0, TEXTURE, btn -> clearDown.run()));
		children.add(btnBulkCraft = new ImageButton(0, 0, 16, 16, 240, 0, TEXTURE, btn -> bulkCraft.run()));
	}
	
	@Override
	protected void updatePositions() {
		btnClearUp.x = offX + 4;
		btnClearUp.y = offY;
		btnClearDown.x = offX + 4;
		btnClearDown.y = offY + 32;
		btnBulkCraft.x = offX + 132;
		btnBulkCraft.y = offY + 16;
	}
	
	private void updateHoverSlotIndex(int mx, int my) {
		mx -= offX;
		my -= offY;
		if (22 <= mx && mx < 70 && 0 <= my && my < 48)
			hoveredSlotIndex = (mx - 22) / 16 + my / 16 * 3;
		else if (102 <= mx && mx < 126 && 12 <= my && my < 36)
			hoveredSlotIndex = 9;
		else
			hoveredSlotIndex = -1;
	}
	
	@Override
	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		btnBulkCraft.active = canBulkCraft.get();
		updateHoverSlotIndex(mx, my);
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);
		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		
		//Background
		Screen.blit(
				poseStack,
				offX, offY,
				0, 0,
				152, 48,
				256, 256
		);
		//Crafting slots
		for (int gridY = 0; gridY < 3; gridY++) {
			for (int gridX = 0; gridX < 3; gridX++) {
				Screen.blit(
						poseStack,
						offX + 22 + 16 * gridX, offY + 16 * gridY,
						192, hoveredSlotIndex == gridX + 3 * gridY ? 16 : 0,
						16, 16,
						256, 256
				);
			}
		}
		//Result slot
		Screen.blit(
				poseStack,
				offX + 106, offY + 12,
				152, hoveredSlotIndex == 9 ? 24 : 0,
				24, 24,
				256, 256
		);
		
		//Buttons
		super.render(poseStack, mx, my, partialTick);
		
		//Items
		for (int gridY = 0; gridY < 3; gridY++)
			for (int gridX = 0; gridX < 3; gridX++)
				renderer.renderItem(poseStack, offX + 22 + 16 * gridX, offY + 16 * gridY, slotIndex + gridX + 3 * gridY);
		renderer.renderItem(poseStack, offX + 110, offY + 16, slotIndex + 9);
	}
	
	@Override
	public void renderTooltip(Screen screen, PoseStack poseStack, int mx, int my) {
		if (btnClearUp.isMouseOver(mx, my))
			screen.renderTooltip(poseStack, Component.literal("Clear To Pocket"), mx, my);
		if (btnClearDown.isMouseOver(mx, my))
			screen.renderTooltip(poseStack, Component.literal("Clear To Inventory"), mx, my);
		if (btnBulkCraft.isMouseOver(mx, my))
			screen.renderTooltip(poseStack, Component.literal("Bulk Crafting"), mx, my);
	}
	
	public int getHoverSlotIndex() {
		return hoveredSlotIndex < 0 ? -1 : slotIndex + hoveredSlotIndex;
	}
}
