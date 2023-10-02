package com.ofek2608.deep_pocket.api.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.ofek2608.deep_pocket.DeepPocketMod;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public final class PocketWidgetsRenderer {
	private PocketWidgetsRenderer() {}
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/widgets.png");
	
	public static void renderBackground(int x0, int y0, int x1, int y1) {
		renderGeneric(x0, y0, x1, y1, 0);
	}
	
	public static void renderButtonBackground(int x0, int y0, int x1, int y1, boolean hover) {
		renderGeneric(x0, y0, x1, y1, hover ? 2 : 1);
	}
	
	public static void renderSlot(int x, int y, boolean hover) {
		renderGeneric(x, y, hover ? 2 : 1);
	}
	
	public static void renderButtonPlus(int x, int y, int state) {
		if (state < 0 || 2 < state) {
			throw new IllegalArgumentException();
		}
		renderGeneric(x, y, 3 + state);
	}
	
	public static void renderButtonLeft(int x, int y, int state) {
		if (state < 0 || 2 < state) {
			throw new IllegalArgumentException();
		}
		renderGeneric(x, y, 6 + state);
	}
	
	public static void renderButtonRight(int x, int y, int state) {
		if (state < 0 || 2 < state) {
			throw new IllegalArgumentException();
		}
		renderGeneric(x, y, 9 + state);
	}
	
	
	private static void renderGeneric(int x, int y, int index) {
		renderGeneric(x, y, x + 16, y + 16, index);
	}
	
	private static void renderGeneric(
			int x0, int y0, int x1, int y1,
			int index
	) {
		int u0 = index % 16 * 16;
		int v0 = index / 16 * 16;
		int u1 = u0 + 16;
		int v1 = v0 + 16;
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		
		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		
		bufferbuilder.vertex(x0, y1, 0).uv(u0 / 256.0f, v1 / 256.0f).endVertex();
		bufferbuilder.vertex(x1, y1, 0).uv(u1 / 256.0f, v1 / 256.0f).endVertex();
		bufferbuilder.vertex(x1, y0, 0).uv(u1 / 256.0f, v0 / 256.0f).endVertex();
		bufferbuilder.vertex(x0, y0, 0).uv(u0 / 256.0f, v0 / 256.0f).endVertex();
		BufferUploader.drawWithShader(bufferbuilder.end());
	}
}
