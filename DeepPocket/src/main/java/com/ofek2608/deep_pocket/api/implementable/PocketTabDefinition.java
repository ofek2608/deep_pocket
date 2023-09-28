package com.ofek2608.deep_pocket.api.implementable;

import com.ofek2608.deep_pocket.api.DPClientAPI;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.utils.Rect;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;

public interface PocketTabDefinition<Data> {
	boolean isVisible(DPClientAPI api, LocalPlayer player, Pocket pocket);
	Data onOpen(DPClientAPI api, LocalPlayer player, Pocket pocket);
	default void onClose(Data data) { }
	
	default boolean isDisplayInventory(Data data) { return false; }
	default int getLeftWidth(Data data) { return 0; }
	default int getRightWidth(Data data) { return 0; }
	default int getScrollRowElementCount(Data data) { return 0; }
	default int getScrollElementCount(Data data) { return 0; }
	default int getScrollElementHeight(Data data) { return 0; }
	default Rect getScrollRect(Data data, int height) { return new Rect(0, 0, 0, 0); }
	default int getScrollbarX(Data data) { return 0; }
	
	default void renderBackground(Data data, GuiGraphics graphics, float partialTick, int mx, int my, Rect rect) {}
	default void renderForeground(Data data, GuiGraphics graphics, float partialTick, int mx, int my, Rect rect) {}
	default void renderScrollElement(Data data, GuiGraphics graphics, float partialTick, int mx, int my, int x, int y, int index, boolean hovered) {}
	
	
	final class TabHandler<Data> {
		private final PocketTabDefinition<Data> definition;
		private final Data data;
		
		public TabHandler(PocketTabDefinition<Data> definition, DPClientAPI api, LocalPlayer player, Pocket pocket) {
			this.definition = definition;
			this.data = definition.onOpen(api, player, pocket);
		}
		
		public void onClose() { definition.onClose(data); }
		
		public boolean isDisplayInventory() { return definition.isDisplayInventory(data); }
		public int getLeftWidth() { return definition.getLeftWidth(data); }
		public int getRightWidth() { return definition.getRightWidth(data); }
		public int getScrollRowElementCount() { return definition.getScrollRowElementCount(data); }
		public int getScrollElementCount() { return definition.getScrollElementCount(data); }
		public int getScrollElementHeight() { return definition.getScrollElementHeight(data); }
		public Rect getScrollRect(int height) { return definition.getScrollRect(data, height); }
		public int getScrollbarX() { return definition.getScrollbarX(data); }
		
		public void renderBackground(GuiGraphics graphics, float partialTick, int mx, int my, Rect rect) {
			definition.renderBackground(data, graphics, partialTick, mx, my, rect);
		}
		
		public void renderForeground(GuiGraphics graphics, float partialTick, int mx, int my, Rect rect) {
			definition.renderForeground(data, graphics, partialTick, mx, my, rect);
		}
		
		public void renderScrollElement(GuiGraphics graphics, float partialTick, int mx, int my, int x, int y, int index, boolean hovered) {
			definition.renderScrollElement(data, graphics, partialTick, mx, my, x, y, index, hovered);
		}
	}
}
