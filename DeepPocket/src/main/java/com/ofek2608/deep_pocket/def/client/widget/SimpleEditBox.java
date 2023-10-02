package com.ofek2608.deep_pocket.def.client.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class SimpleEditBox extends EditBox {
	public SimpleEditBox(Font font, int x, int y, int width, int height, Component message) {
		super(font, x, y, width, height, message);
	}
	
	public SimpleEditBox(Font font, int x, int y, int width, int height, @Nullable EditBox copy, Component message) {
		super(font, x, y, width, height, copy, message);
	}
	
	@Override
	public void renderWidget(GuiGraphics graphics, int mx, int my, float partialTick) {
		int oldX = this.getX();
		int oldY = this.getY();
		int oldW = this.width;
		int oldH = this.height;
		
		setX(oldX + 4);
		setY(oldY + (height - 8) / 2);
		width = width - 8;
		height = 8;
		setBordered(false);
		
		super.renderWidget(graphics, mx, my, partialTick);
		
		setX(oldX);
		setY(oldY);
		width = oldW;
		height = oldH;
		
		setBordered(true);
	}
}
