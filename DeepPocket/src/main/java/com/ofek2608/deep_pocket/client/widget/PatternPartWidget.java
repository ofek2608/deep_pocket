package com.ofek2608.deep_pocket.client.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

public class PatternPartWidget implements WidgetWithTooltip, GuiEventListener, NonNarratableEntry {
	@Override
	public void renderTooltip(Screen screen, PoseStack poseStack, int mx, int my) {
		//TODO
	}
	
	@Override
	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		//TODO
	}
	
	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		return false;
	}
}
