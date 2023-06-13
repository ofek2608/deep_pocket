package com.ofek2608.deep_pocket.api.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public final class GuiUtils {
	private GuiUtils() {}
	
	public static void playClickSound() {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}
	
	public static void setShaderColor(int color) {
		RenderSystem.setShaderColor(
				(float)((color >> 16) & 0xFF) / 0xFF,
				(float)((color >> 8) & 0xFF) / 0xFF,
				(float)(color & 0xFF) / 0xFF,
				1
		);
	}
	
	public static void setShaderTransparentColor(int color) {
		RenderSystem.setShaderColor(
				(float)((color >> 16) & 0xFF) / 0xFF,
				(float)((color >> 8) & 0xFF) / 0xFF,
				(float)(color & 0xFF) / 0xFF,
				(float)(color >> 24) / 0xFF
		);
	}
	
	public static void renderOutline(int x0, int x1, int y0, int y1) {
		RenderSystem.setShader(GameRenderer::getPositionShader);
		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
		addRect(bufferbuilder, x0, x1, y0, y0 + 1);
		addRect(bufferbuilder, x0, x1, y1 - 1, y1);
		addRect(bufferbuilder, x0, x0 + 1, y0, y1);
		addRect(bufferbuilder, x1 - 1, x1, y0, y1);
		BufferUploader.drawWithShader(bufferbuilder.end());
	}
	
	public static void addRect(BufferBuilder bufferbuilder, int x0, int x1, int y0, int y1) {
		bufferbuilder.vertex(x0, y1, 0).endVertex();
		bufferbuilder.vertex(x1, y1, 0).endVertex();
		bufferbuilder.vertex(x1, y0, 0).endVertex();
		bufferbuilder.vertex(x0, y0, 0).endVertex();
	}
}
