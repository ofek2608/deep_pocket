package com.ofek2608.deep_pocket.def.client;

import com.ofek2608.deep_pocket.api.implementable.TabContentWidget;
import com.ofek2608.deep_pocket.api.utils.Rect;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class PocketScrollWidget extends AbstractWidget {
	private final ScrollComponent scrollComponent = new ScrollComponent();
	
	private TabContentWidget tabContentWidget;
	private Rect contentRect = Rect.ZERO;
	
	public PocketScrollWidget() {
		super(0, 0, 0, 0, Component.literal("scroll"));
	}
	
	public void setContentRect(Rect contentRect) {
		this.contentRect = contentRect;
	}
	
	public void setTabContentWidget(TabContentWidget tabContentWidget) {
		this.tabContentWidget = tabContentWidget;
	}
	
	@Override
	protected void renderWidget(GuiGraphics graphics, int mx, int my, float partialTick) {
		scrollComponent.rowElementCount = tabContentWidget.getScrollRowElementCount();
		scrollComponent.elementHeight = tabContentWidget.getScrollElementHeight();
		scrollComponent.elementCount = tabContentWidget.getScrollElementCount();
		
		Rect scrollRect = tabContentWidget.getScrollRect(contentRect.h());
		scrollComponent.minX = scrollRect.x0() + contentRect.x();
		scrollComponent.maxX = scrollRect.x1() + contentRect.x();
		scrollComponent.minY = scrollRect.y0() + contentRect.y();
		scrollComponent.maxY = scrollRect.y1() + contentRect.y();
		
		setX(scrollComponent.minX);
		setY(scrollComponent.minY);
		setWidth(scrollComponent.maxX - scrollComponent.minX);
		setHeight(scrollComponent.maxY - scrollComponent.minY);
		
		scrollComponent.scrollbarX = tabContentWidget.getScrollbarX() + contentRect.x();
		
		scrollComponent.render(mx, my, partialTick, i -> tabContentWidget.renderScrollElement(
						graphics,
						partialTick,
						mx,
						my,
						scrollComponent.minX,
						scrollComponent.minY,
						i,
						i == scrollComponent.hoveredIndex
		));
	}
	
	@Override
	protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
	
	@Override
	public void mouseMoved(double mx, double my) {
		scrollComponent.mouseMoved(mx, my);
	}
	
	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		return scrollComponent.mouseClicked(mx, my, button);
	}
	
	@Override
	public boolean mouseReleased(double mx, double my, int button) {
		return scrollComponent.mouseReleased(mx, my, button);
	}
	
	@Override
	public boolean mouseScrolled(double mx, double my, double delta) {
		return scrollComponent.mouseScrolled(mx, my, delta);
	}
}
