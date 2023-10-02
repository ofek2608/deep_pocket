package com.ofek2608.deep_pocket.def.client.tab;

import com.ofek2608.deep_pocket.api.DPClientAPI;
import com.ofek2608.deep_pocket.api.implementable.TabContentWidget;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.utils.PocketWidgetsRenderer;
import com.ofek2608.deep_pocket.api.utils.Rect;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public final class ItemsTab extends AbstractWidget implements TabContentWidget {
	private final DPClientAPI api;
	private final LocalPlayer player;
	private final Pocket pocket;
	private Rect rect = Rect.ZERO;
	
	public ItemsTab(DPClientAPI api, LocalPlayer player, Pocket pocket) {
		super(0, 0, 0, 0, Component.literal("items"));
		this.api = api;
		this.player = player;
		this.pocket = pocket;
	}
	
	@Override
	public void setRect(Rect rect) {
		this.rect = rect;
	}
	
	@Override
	public void onClose() {
		//FIXME
	}
	
	@Override
	public int getLeftWidth() {
		return 50; //FIXME
	}
	
	@Override
	public int getRightWidth() {
		return 58; //FIXME
	}
	
	@Override
	public int getScrollRowElementCount() {
		return 9;
	}
	
	@Override
	public int getScrollElementHeight() {
		return 16;
	}
	
	@Override
	public int getScrollbarX() {
		return 96; //FIXME
	}
	
	@Override
	public Rect getScrollRect(int height) {
		return new Rect(
				4, 92,
				4, height - 8
		); //FIXME
	}
	
	@Override
	public int getScrollElementCount() {
		return 0; //FIXME
	}
	
	@Override
	public void renderScrollElement(GuiGraphics graphics, float partialTick, int mx, int my, int x, int y, int index, boolean hovered) {
		//FIXME
	}
	
	@Override
	public boolean isDisplayInventory() {
		return true;
	}
	
	@Override
	protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		PocketWidgetsRenderer.renderBackground(rect.x0(), rect.y0(), rect.x1(), rect.y1());
	}
	
	@Override
	protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
}
