package com.ofek2608.deep_pocket.def.client.widget;

import com.ofek2608.deep_pocket.api.utils.PocketWidgetsRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class CompactEditBox extends SimpleEditBox {
	public CompactEditBox(Font font, int x, int y, int width, int height, Component message) {
		super(font, x, y, width, height, message);
	}
	
	public CompactEditBox(Font font, int x, int y, int width, int height, @Nullable EditBox copy, Component message) {
		super(font, x, y, width, height, copy, message);
	}
	
	@Override
	public void renderWidget(GuiGraphics graphics, int mx, int my, float partialTick) {
		PocketWidgetsRenderer.renderButtonBackground(getX(), getY(), getX() + getWidth(), getY() + getHeight(), isHoveredOrFocused());
		super.renderWidget(graphics, mx, my, partialTick);
	}
}
