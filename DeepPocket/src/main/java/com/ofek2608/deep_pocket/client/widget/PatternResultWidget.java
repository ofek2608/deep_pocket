package com.ofek2608.deep_pocket.client.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

public class PatternResultWidget implements WidgetWithTooltip, GuiEventListener, NonNarratableEntry {
	public int x;
	public int y;
	private final PatternPartWidget input;
	private final PatternPartWidget output;
	
	private boolean hover;
	
	public PatternResultWidget(PatternPartWidget input, PatternPartWidget output) {
		this.input = input;
		this.output = output;
	}
	
	@Override
	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		this.hover = x <= mx && mx < x + 16 && y <= my && my < y + 16;
		//TODO
	}
	
	@Override
	public void renderTooltip(Screen screen, PoseStack poseStack, int mx, int my) {
		//TODO
	}
	
	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		//TODO
		return hover;
	}
}
