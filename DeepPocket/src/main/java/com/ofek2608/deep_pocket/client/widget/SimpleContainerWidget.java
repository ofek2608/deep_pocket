package com.ofek2608.deep_pocket.client.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleContainerWidget implements WidgetWithTooltip, ContainerEventHandler, NonNarratableEntry {
	protected int offX, offY;
	private boolean dragging;
	private GuiEventListener focused;
	protected final List<GuiEventListener> children = new ArrayList<>();
	
	public void setPos(int x, int y) {
		offX = x;
		offY = y;
		updatePositions();
	}
	
	protected abstract void updatePositions();
	
	@Override
	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		for (GuiEventListener child : children)
			if (child instanceof Widget widget)
				widget.render(poseStack, mx, my, partialTick);
	}
	
	@Override
	public List<? extends GuiEventListener> children() {
		return children;
	}
	
	@Override
	public boolean isDragging() {
		return dragging;
	}
	
	@Override
	public void setDragging(boolean dragging) {
		this.dragging = dragging;
	}
	
	@Nullable
	@Override
	public GuiEventListener getFocused() {
		return focused;
	}
	
	@Override
	public void setFocused(@Nullable GuiEventListener focused) {
		this.focused = focused;
	}
}
