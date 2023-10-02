package com.ofek2608.deep_pocket.api.implementable;

import com.ofek2608.deep_pocket.api.utils.Rect;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public interface TabContentWidget extends GuiEventListener, Renderable, NarratableEntry {
	void setRect(Rect rect);
	default void onClose() {}
	//layout
	default boolean isDisplayInventory() { return false; }
	default int getLeftWidth() { return 0; }
	default int getRightWidth() { return 0; }
	//scroll
	default int getScrollRowElementCount() { return 0; }
	default int getScrollElementCount() { return 0; }
	default int getScrollElementHeight() { return 0; }
	default Rect getScrollRect(int height) { return Rect.ZERO; }
	default int getScrollbarX() { return 0; }
	default void renderScrollElement(GuiGraphics graphics, float partialTick, int mx, int my, int x, int y, int index, boolean hovered) {}
}
