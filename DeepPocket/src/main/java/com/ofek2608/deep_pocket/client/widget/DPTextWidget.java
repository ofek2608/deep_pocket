package com.ofek2608.deep_pocket.client.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.DeepPocketUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public class DPTextWidget extends EditBox {
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/widget/text.png");
	
	public DPTextWidget(int pX, int pY, int pWidth) {
		super(Minecraft.getInstance().font, pX, pY, pWidth, 10, Component.empty());
		setBordered(false);
	}
	
	
	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		boolean hover = x <= mx && mx < x + width && y <= my && my < y + height;
		if (hover && button == InputConstants.MOUSE_BUTTON_RIGHT) {
			setValue("");
			return true;
		}
		return super.mouseClicked(mx, my, button);
	}
	
	@Override
	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);
		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		Screen.blit(poseStack, x, y, 0, 0, width, 10, 256, 256);
		
		Font font = Minecraft.getInstance().font;
		String value = getValue();
		
		
		int i2 = 0xFFFFFF;
		int j = this.getCursorPosition() - this.displayPos;
		int k = this.highlightPos - this.displayPos;
		String s = font.plainSubstrByWidth(value.substring(this.displayPos), this.getInnerWidth());
		boolean flag = j >= 0 && j <= s.length();
		boolean flag1 = this.isFocused() && DeepPocketUtils.displayTimeSuffix() && flag;
		int l = this.x + 1;
		int i1 = this.y + 1;
		int j1 = l;
		if (k > s.length()) {
			k = s.length();
		}
		
		if (!s.isEmpty()) {
			String s1 = flag ? s.substring(0, j) : s;
			j1 = font.draw(poseStack, FormattedCharSequence.forward(s1, Style.EMPTY), (float)l, (float)i1, i2);
		}
		
		boolean flag2 = this.getCursorPosition() < value.length() || value.length() >= this.getMaxLength();
		int k1 = j1;
		if (!flag) {
			k1 = j > 0 ? l + this.width : l;
		} else if (flag2) {
			k1 = j1 - 1;
			--j1;
		}
		
		if (!s.isEmpty() && flag && j < s.length()) {
			font.draw(poseStack, FormattedCharSequence.forward(s.substring(j), Style.EMPTY), (float)(j1 + 1), (float)i1, i2);
		}
		
		if (flag1) {
			if (flag2) {
				GuiComponent.fill(poseStack, k1, y, k1 + 1, y + 10, 0xFFD0D0D0);
			} else {
				font.draw(poseStack, "_", (float)k1, (float)i1, i2);
			}
		}
		
		if (k != j) {
			int l1 = l + font.width(s.substring(0, k));
			this.renderHighlight(k1, y, l1 - 1, y + 10);
		}
	}
	
	private void renderHighlight(int pStartX, int pStartY, int pEndX, int pEndY) {
		if (pStartX < pEndX) {
			int i = pStartX;
			pStartX = pEndX;
			pEndX = i;
		}
		
		if (pStartY < pEndY) {
			int j = pStartY;
			pStartY = pEndY;
			pEndY = j;
		}
		
		if (pEndX > this.x + this.width) {
			pEndX = this.x + this.width;
		}
		
		if (pStartX > this.x + this.width) {
			pStartX = this.x + this.width;
		}
		
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionShader);
		RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
		RenderSystem.disableTexture();
		RenderSystem.enableColorLogicOp();
		RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
		bufferbuilder.vertex(pStartX, pEndY, 0.0D).endVertex();
		bufferbuilder.vertex(pEndX, pEndY, 0.0D).endVertex();
		bufferbuilder.vertex(pEndX, pStartY, 0.0D).endVertex();
		bufferbuilder.vertex(pStartX, pStartY, 0.0D).endVertex();
		tesselator.end();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableColorLogicOp();
		RenderSystem.enableTexture();
	}
	
	@Override
	protected void onFocusedChanged(boolean focused) {
		Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(focused);
	}
}
