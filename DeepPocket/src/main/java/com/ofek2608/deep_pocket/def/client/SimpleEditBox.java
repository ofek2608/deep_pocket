package com.ofek2608.deep_pocket.def.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
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
	public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		int oldX = this.x;
		int oldY = this.y;
		int oldW = this.width;
		int oldH = this.height;
		
		x += 4;
		y += (height - 8) / 2;
		width = width - 8;
		height = 8;
		setBordered(false);
		
		super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
		
		x = oldX;
		y = oldY;
		width = oldW;
		height = oldH;
		
		setBordered(true);
	}
}
