package com.ofek2608.deep_pocket.def.client.widget;

import com.ofek2608.deep_pocket.api.DPClientAPI;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.utils.GuiUtils;
import com.ofek2608.deep_pocket.api.utils.PocketWidgetsRenderer;
import com.ofek2608.deep_pocket.api.utils.Rect;
import com.ofek2608.deep_pocket.def.client.screen.PocketSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PocketBackgroundWidget extends AbstractWidget {
	private final DPClientAPI api;
	private final Pocket pocket;
	
	private Rect renderRect = Rect.ZERO, contentRect = Rect.ZERO;
	private boolean hoverBackButton;
	
	public PocketBackgroundWidget(DPClientAPI api, Pocket pocket) {
		super(0, 0, 0, 0, Component.literal("Back"));
		this.api = api;
		this.pocket = pocket;
	}
	
	public void setRects(Rect renderRect, Rect contentRect) {
		this.renderRect = renderRect;
		this.contentRect = contentRect;
	}
	
	@Override
	protected void renderWidget(GuiGraphics graphics, int mx, int my, float partialTick) {
		// outline
		GuiUtils.setShaderColor(pocket.getProperties().getColor());
		GuiUtils.renderOutline(renderRect.x0(), renderRect.x1(), renderRect.y0(), renderRect.y1());
		
		// header top
		PocketWidgetsRenderer.renderBackground(
						renderRect.x0() + 1, renderRect.y0() + 1,
						renderRect.x1() - 1, renderRect.y0() + 5
		);
		// header middle
		PocketWidgetsRenderer.renderBackground(
						renderRect.x0() + 1, renderRect.y0() + 5,
						renderRect.x0() + 5, renderRect.y0() + 21
		);
		PocketWidgetsRenderer.renderBackground(
						renderRect.x0() + 21, renderRect.y0() + 5,
						renderRect.x1() - 1, renderRect.y0() + 21
		);
		int backButtonX = renderRect.x0() + 5;
		int backButtonY = renderRect.y0() + 5;
		hoverBackButton = backButtonX <= mx && mx < backButtonX + 16 && backButtonY <= my && my < backButtonY + 16;
		
		var properties = pocket.getProperties();
		PocketWidgetsRenderer.renderButtonLeft(backButtonX, backButtonY, hoverBackButton ? 1 : 0);
		var icon = properties.getIcon();
		api.getEntryCategory(icon.category()).render(graphics, icon, renderRect.x0() + 25, renderRect.y0() + 5);
		graphics.drawString(Minecraft.getInstance().font, properties.getName(), renderRect.x0() + 45, renderRect.y0() + 9, 0xFFFFFF);
		
		// header bottom
		PocketWidgetsRenderer.renderBackground(
						renderRect.x0() + 1, renderRect.y0() + 21,
						renderRect.x1() - 1, renderRect.y0() + 25
		);
		
		// content sides
		
		PocketWidgetsRenderer.renderBackground(
						renderRect.x0() + 1, contentRect.y0(),
						contentRect.x0(), contentRect.y1()
		);
		PocketWidgetsRenderer.renderBackground(
						contentRect.x1(), contentRect.y0(),
						renderRect.x1() - 1, contentRect.y1()
		);
		
		// bottom
		
		PocketWidgetsRenderer.renderBackground(
						renderRect.x0() + 1, renderRect.y1() - 5,
						renderRect.x1() - 1, renderRect.y1() - 1
		);
		
		if (hoverBackButton) {
			Screen screen = Minecraft.getInstance().screen;
			if (screen != null) {
				screen.setTooltipForNextRenderPass(Tooltip.create(Component.literal("Back")), this.createTooltipPositioner(), false);
			}
		}
	}
	
	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button != 0 || !hoverBackButton) {
			return false;
		}
		GuiUtils.playClickSound();
		Minecraft.getInstance().setScreen(new PocketSelectionScreen(api));
		return true;
	}
	
	@Override
	protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
}
