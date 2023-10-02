package com.ofek2608.deep_pocket.def.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.utils.ScissorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.function.IntConsumer;

import static com.ofek2608.deep_pocket.def.client.Sprite.rect;

public final class ScrollComponent {
	public float scrollValue;
	public int elementHeight, rowElementCount;
	public int elementCount;
	public int minX, maxX, minY, maxY;
	public int hoveredIndex = -1;
	public int scrollbarX;
	public boolean hoveringScrollbar;
	public boolean draggingScrollbar;
	
	public void setRect(int minX, int maxX, int minY, int maxY) {
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
	}
	
	public int getW() {
		return maxX - minX;
	}
	
	public int getH() {
		return maxY - minY;
	}
	
	public int getRowCount() {
		if (rowElementCount <= 0 || elementCount <= 0) {
			return 0;
		}
		return (elementCount + rowElementCount - 1) / rowElementCount;
	}
	
	public void render(int mx, int my, float partialTick, IntConsumer renderIndex) {
		if (rowElementCount <= 0) {
			return;
		}
		
		hoveringScrollbar = scrollbarX <= mx && mx < scrollbarX + 8 && minY <= my && my < maxY;
		
		PoseStack modelViewStack = RenderSystem.getModelViewStack();
		
		int windowHeight = maxY - minY;
		int totalScrollHeight = getRowCount() * elementHeight;
		float windowOffset;
		int minRenderIndex, maxRenderIndex;
		
		if (totalScrollHeight <= windowHeight) {
			windowOffset = 0;
			minRenderIndex = 0;
			maxRenderIndex = elementCount;
		} else {
			windowOffset = scrollValue * (totalScrollHeight - windowHeight);
			//rounding by gui scale to get rid of visual glitches
			double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
			windowOffset = (int)(windowOffset * guiScale) / (float)guiScale;
			
			minRenderIndex = (int) (windowOffset / elementHeight);
			maxRenderIndex = Math.min((int) ((windowOffset + windowHeight) / elementHeight + 1), elementCount);
		}
		
		int hoveredRow = (int)Math.floor((my + windowOffset - minY) / elementHeight);
		int hoveredCol = (int)Math.floor((float)rowElementCount * (mx - minX) / (maxX - minX));
		
		hoveredIndex = hoveredRow * rowElementCount + hoveredCol;
		if (mx < minX || maxX <= mx || my < minY || maxY <= my || hoveredIndex < 0 || elementCount <= hoveredIndex) {
			hoveredIndex = -1;
		}
		
		ScissorManager.push(minX, minY, maxX, maxY);
		modelViewStack.pushPose();
		modelViewStack.translate(0, minY - windowOffset, 0);
		RenderSystem.applyModelViewMatrix();
		
		for (int i = minRenderIndex; i < maxRenderIndex; i++) {
			renderIndex.accept(i);
		}
		
		modelViewStack.popPose();
		
		ScissorManager.pop();
		
		
		RenderSystem.applyModelViewMatrix();
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, Sprites.TEXTURE);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		
		
		boolean renderH = hoveringScrollbar || draggingScrollbar;
		float thumbY = scrollValue * (maxY - minY - 2) + minY;
		
		(renderH ? Sprites.BAR_H : Sprites.BAR_N).blit(scrollbarX, minY, Sprites.WIDTH, maxY - minY);
		(renderH ? Sprites.THUMB_H : Sprites.THUMB_N).blit(scrollbarX, thumbY);
	}
	
	public void setValue(double value) {
		scrollValue = (float)Math.min(Math.max(0, value), 1);
	}
	
	public void updateScroll(double my) {
		setValue((my - minY - 1) / (maxY - minY - 2));
	}
	
	
	
	
	public void mouseMoved(double mx, double my) {
		if (rowElementCount <= 0) {
			return;
		}
		if (draggingScrollbar) {
			updateScroll(my);
		}
	}
	
	public boolean mouseClicked(double mx, double my, int button) {
		if (rowElementCount <= 0) {
			return false;
		}
		if (button == 0 && hoveringScrollbar) {
			draggingScrollbar = true;
			updateScroll(my);
			return true;
		}
		return false;
	}
	
	public boolean mouseReleased(double mx, double my, int button) {
		if (draggingScrollbar && button == 0) {
			draggingScrollbar = false;
			return true;
		}
		return false;
	}
	
	public boolean mouseScrolled(double mx, double my, double delta) {
		int rowCount = getRowCount();
		if (rowCount <= 0) {
			return false;
		}
		setValue(scrollValue - (float)delta / rowCount);
		return true;
	}
	
	private interface Sprites {
		ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/scroll.png");
		int WIDTH = 8;
		Sprite BAR_N = rect(0, 0, WIDTH, 2, WIDTH);
		Sprite BAR_H = rect(0, 2, WIDTH, 2, WIDTH);
		Sprite THUMB_N = rect(0, 4, WIDTH, 2, WIDTH);
		Sprite THUMB_H = rect(0, 6, WIDTH, 2, WIDTH);
	}
}
