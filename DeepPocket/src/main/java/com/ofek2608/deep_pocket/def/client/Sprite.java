package com.ofek2608.deep_pocket.def.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;

final class Sprite {
	public final int x, y, w, h;
	public final float u0, v0, u1, v1;
	
	private Sprite(
			int x, int y, int w, int h,
			float u0, float v0, float u1, float v1
	) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.u0 = u0;
		this.v0 = v0;
		this.u1 = u1;
		this.v1 = v1;
	}
	
	
	public static Sprite rect(int x, int y, int w, int h) {
		return rect(x, y, w, h, 256);
	}
	
	public static Sprite rect(int x, int y, int w, int h, int textureSize) {
		return new Sprite(
				x, y, w, h,
				(float)x / textureSize, (float)y / textureSize,
				(float)(x + w) / textureSize, (float)(y + h) / textureSize
		);
	}
	
	public int blit(int x, int y) {
		return blit(x, y, w, h);
	}
	
	public int blit(int x0, int y0, int w, int h) {
		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		int x1 = x0 + w;
		int y1 = y0 + h;
		
		bufferbuilder.vertex(x0, y1, 0).uv(u0, v1).endVertex();
		bufferbuilder.vertex(x1, y1, 0).uv(u1, v1).endVertex();
		bufferbuilder.vertex(x1, y0, 0).uv(u1, v0).endVertex();
		bufferbuilder.vertex(x0, y0, 0).uv(u0, v0).endVertex();
		BufferUploader.drawWithShader(bufferbuilder.end());
		
		return y1;
	}
	
	public float blit(float x, float y) {
		return blit(x, y, w, h);
	}
	
	public float blit(float x0, float y0, float w, float h) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		float x1 = x0 + w;
		float y1 = y0 + h;
		
		bufferbuilder.vertex(x0, y1, 0).uv(u0, v1).endVertex();
		bufferbuilder.vertex(x1, y1, 0).uv(u1, v1).endVertex();
		bufferbuilder.vertex(x1, y0, 0).uv(u1, v0).endVertex();
		bufferbuilder.vertex(x0, y0, 0).uv(u0, v0).endVertex();
		BufferUploader.drawWithShader(bufferbuilder.end());
		
		return y1;
	}
}
