package com.ofek2608.deep_pocket.def.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.function.IntConsumer;

public class ScrollComponent {
	public float scrollValue;
	public int elementHeight, rowElementCount;//TODO use row element count
	public int elementCount;
	public int minX, maxX, minY, maxY;
	public int hoveredIndex = -1;
	
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
	
	public void render(double mx, double my, float partialTick, IntConsumer renderIndex) {
		PoseStack modelViewStack = RenderSystem.getModelViewStack();
		
		int windowHeight = maxY - minY;
		int totalScrollHeight = elementCount * elementHeight;
		float windowOffset;
		int minRenderIndex, maxRenderIndex;
		
		if (totalScrollHeight <= windowHeight) {
			windowOffset = 0;
			minRenderIndex = 0;
			maxRenderIndex = elementCount;
		} else {
			windowOffset = scrollValue * (totalScrollHeight - windowHeight);
			minRenderIndex = (int) (windowOffset / elementHeight);
			maxRenderIndex = Math.min((int) ((windowOffset + windowHeight) / elementHeight + 1), elementCount);
		}
		
		hoveredIndex = (int)Math.floor((my + windowOffset - minY) / elementHeight);
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
	}
	
	public void setValue(double value) {
		scrollValue = (float)Math.min(Math.max(0, value), 1);
	}
	
	public void updateScroll(double my) {
		setValue((my - minY - 1) / (maxY - minY - 2));
	}
	
	public float getScrollbarY() {
		return scrollValue * (maxY - minY - 2) + minY;
	}
	
	public void onScroll(double delta) {
		if (elementCount > 0) {
			setValue(scrollValue - (float)delta / elementCount);
		}
	}
}
