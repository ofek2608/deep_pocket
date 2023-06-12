package com.ofek2608.deep_pocket.def.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;

public final class ScissorManager {
	private ScissorManager() {}
	
	private static final Stack<Rect> CUTS = new Stack<>();
	
	private static Rect getCut() {
		return CUTS.isEmpty() ? null : CUTS.peek();
	}
	
	public static void push(int x0, int y0, int x1, int y1) {
		Rect newCut = createNext(getCut(), x0, y0, x1, y1);
		CUTS.push(newCut);
		setScissor(newCut);
	}
	
	public static void pop() {
		CUTS.pop();
		setScissor(getCut());
	}
	
	private static Rect createNext(@Nullable Rect prev, int x0, int y0, int x1, int y1) {
		if (prev == null) {
			return new Rect(x0, y0, x1, y1);
		}
		if (prev.x1 <= x0 || prev.y1 <= y0) {
			return new Rect(0, 0, 0, 0);
		}
		return new Rect(
				Math.max(x0, prev.x0),
				Math.max(y0, prev.y0),
				Math.min(x1, prev.x1),
				Math.min(y1, prev.y1)
		);
	}
	
	private static void setScissor(@Nullable Rect rect) {
		if (rect == null) {
			RenderSystem.disableScissor();
		} else {
			Window window = Minecraft.getInstance().getWindow();
			double guiScale = window.getGuiScale();
			
			RenderSystem.enableScissor(
					(int) (guiScale * rect.x0),
					window.getHeight() - (int) (guiScale * rect.y1),
					(int) (guiScale * (rect.x1 - rect.x0)),
					(int) (guiScale * (rect.y1 - rect.y0))
			);
		}
	}
	
	private record Rect(int x0, int y0, int x1, int y1) {}
}
