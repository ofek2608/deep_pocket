package com.ofek2608.deep_pocket.def.client.widget;

import com.ofek2608.deep_pocket.api.utils.PocketWidgetsRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.network.chat.Component;

public class SimpleButton extends AbstractWidget {
	private final Font font;
	private final Runnable onClick;
	
	public SimpleButton(Font font, int x, int y, int width, int height, Component message, Runnable onClick) {
		super(x, y, width, height, message);
		this.font = font;
		this.onClick = onClick;
	}
	
	@Override
	protected void renderWidget(GuiGraphics graphics, int mx, int my, float partialTick) {
		PocketWidgetsRenderer.renderButtonBackground(getX(), getY(), getX() + getWidth(), getY() + getHeight(), isHoveredOrFocused());
		graphics.drawCenteredString(font, getMessage(), getX() + getWidth() / 2, getY() + (getHeight() - font.lineHeight) / 2, 0xFFFFFF);
	}
	
	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		defaultButtonNarrationText(narrationElementOutput);
	}
	
	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (isHovered()) {
			onClick.run();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (CommonInputs.selected(keyCode)) {
			onClick.run();
			return true;
		}
		return false;
	}
}
