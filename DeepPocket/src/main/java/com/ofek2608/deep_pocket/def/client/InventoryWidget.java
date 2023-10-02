package com.ofek2608.deep_pocket.def.client;

import com.ofek2608.deep_pocket.api.utils.PocketWidgetsRenderer;
import com.ofek2608.deep_pocket.api.utils.Rect;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class InventoryWidget extends AbstractWidget {
	private final LocalPlayer player;
	private Rect renderRect = Rect.ZERO;
	private boolean displayed = false;
	private int hoveredSlotIndex;
	
	public void setRenderRect(Rect renderRect) {
		this.renderRect = renderRect;
	}
	
	public void setDisplayed(boolean display) {
		this.displayed = display;
	}
	
	public InventoryWidget(LocalPlayer player) {
		super(0, 0, 0, 0, Component.translatable("container.inventory"));
		this.player = player;
	}
	
	@Override
	protected void renderWidget(GuiGraphics graphics, int mx, int my, float partialTick) {
		if (!displayed) {
			return;
		}
		int renderMidX = renderRect.midX();
		
		//rows
		PocketWidgetsRenderer.renderBackground(
						renderMidX - 72, renderRect.y1() - 77,
						renderMidX + 72, renderRect.y1() - 73
		);
		PocketWidgetsRenderer.renderBackground(
						renderMidX - 72, renderRect.y1() - 25,
						renderMidX + 72, renderRect.y1() - 21
		);
		
		//sides
		PocketWidgetsRenderer.renderBackground(
						renderRect.x0() + 1, renderRect.y1() - 77,
						renderMidX - 72, renderRect.y1() - 5
		);
		PocketWidgetsRenderer.renderBackground(
						renderMidX + 72, renderRect.y1() - 77,
						renderRect.x1() - 1, renderRect.y1() - 5
		);
		
		//items
		hoveredSlotIndex = -1;
		renderInventoryRow(mx, my, renderRect.y1() - 21, 0);
		renderInventoryRow(mx, my, renderRect.y1() - 73, 9);
		renderInventoryRow(mx, my, renderRect.y1() - 57, 18);
		renderInventoryRow(mx, my, renderRect.y1() - 41, 27);
		
		renderInventoryItemsRow(graphics, renderRect.y1() - 21, 0);
		renderInventoryItemsRow(graphics, renderRect.y1() - 73, 9);
		renderInventoryItemsRow(graphics, renderRect.y1() - 57, 18);
		renderInventoryItemsRow(graphics, renderRect.y1() - 41, 27);
		
		//FIXME render hover text
	}
	
	private void renderInventoryRow(int mx, int my, int y, int inventoryOffset) {
		int renderMidX = renderRect.midX();
		for (int i = 0; i < 9; i++) {
			int x = renderMidX - 72 + 16 * i;
			boolean hover = x <= mx && mx < x + 16 && y <= my && my < y + 16;
			PocketWidgetsRenderer.renderSlot(x, y, hover);
			if (hover) {
				hoveredSlotIndex = inventoryOffset + i;
			}
		}
	}
	
	private void renderInventoryItemsRow(GuiGraphics graphics, int y, int inventoryOffset) {
		int renderMidX = renderRect.midX();
		Inventory inventory = player.getInventory();
		for (int i = 0; i < 9; i++) {
			int x = renderMidX - 72 + 16 * i;
			int slotIndex = inventoryOffset + i;
			boolean hover = hoveredSlotIndex == slotIndex;
			graphics.renderItem(inventory.getItem(slotIndex), x, y);
		}
	}
	
	@Override
	protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
}
