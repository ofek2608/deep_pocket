package com.ofek2608.deep_pocket.def.client.tabs;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.api.DPClientAPI;
import com.ofek2608.deep_pocket.api.implementable.PocketTabDefinition;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.utils.PocketWidgetsRenderer;
import com.ofek2608.deep_pocket.api.utils.Rect;
import net.minecraft.client.player.LocalPlayer;

public final class ItemsTab implements PocketTabDefinition<ItemsTab.Data> {
	private ItemsTab() {}
	public static final ItemsTab INSTANCE = new ItemsTab();
	
	
	@Override
	public boolean isVisible(DPClientAPI api, LocalPlayer player, Pocket pocket) {
		return true;
	}
	
	@Override
	public Data onOpen(DPClientAPI api, LocalPlayer player, Pocket pocket) {
		return new Data(api, player, pocket);
	}
	
	@Override
	public void onClose(Data data) {
		//FIXME
	}
	
	@Override
	public int getLeftWidth(Data data) {
		return 50; //FIXME
	}
	
	@Override
	public int getRightWidth(Data data) {
		return 58; //FIXME
	}
	
	@Override
	public int getScrollRowElementCount(Data data) {
		return 9;
	}
	
	@Override
	public int getScrollElementHeight(Data data) {
		return 16;
	}
	
	@Override
	public int getScrollbarX(Data data) {
		return 96; //FIXME
	}
	
	@Override
	public Rect getScrollRect(Data data, int height) {
		return new Rect(
				4, 92,
				4, height - 8
		); //FIXME
	}
	
	@Override
	public int getScrollElementCount(Data data) {
		return 0; //FIXME
	}
	
	@Override
	public void renderScrollElement(Data data, PoseStack poseStack, float partialTick, int mx, int my, int x, int y, int index, boolean hovered) {
		//FIXME
	}
	
	@Override
	public void renderBackground(Data data, PoseStack poseStack, float partialTick, int mx, int my, Rect rect) {
		PocketWidgetsRenderer.renderBackground(rect.x0(), rect.y0(), rect.x1(), rect.y1());
		//FIXME
	}
	
	@Override
	public void renderForeground(Data data, PoseStack poseStack, float partialTick, int mx, int my, Rect rect) {
		//FIXME
	}
	
	@Override
	public boolean isDisplayInventory(Data data) {
		return true;
	}
	
	public static final class Data {
		private final DPClientAPI api;
		private final LocalPlayer player;
		private final Pocket pocket;
		
		
		private Data(DPClientAPI api, LocalPlayer player, Pocket pocket) {
			this.api = api;
			this.player = player;
			this.pocket = pocket;
		}
	}
}
