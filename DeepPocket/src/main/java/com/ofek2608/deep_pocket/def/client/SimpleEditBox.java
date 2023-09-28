package com.ofek2608.deep_pocket.def.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class SimpleEditBox extends EditBox {
	public SimpleEditBox(Font pFont, int pX, int pY, int pWidth, int pHeight, Component pMessage) {
		super(pFont, pX, pY, pWidth, pHeight, pMessage);
	}
	
	public SimpleEditBox(Font pFont, int pX, int pY, int pWidth, int pHeight, @Nullable EditBox p_94111_, Component pMessage) {
		super(pFont, pX, pY, pWidth, pHeight, p_94111_, pMessage);
	}
	
	@Override
	public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
		int oldX = this.getX();
		int oldY = this.getY();
		int oldW = this.width;
		int oldH = this.height;
		
		setX(oldX + 4);
		setY(oldY + (height - 8) / 2);
		width = width - 8;
		height = 8;
		setBordered(false);
		
		super.render(graphics, pMouseX, pMouseY, pPartialTick);
		
		setX(oldX);
		setY(oldY);
		width = oldW;
		height = oldH;
		
		setBordered(true);
	}
}
