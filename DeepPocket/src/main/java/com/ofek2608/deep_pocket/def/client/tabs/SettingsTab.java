package com.ofek2608.deep_pocket.def.client.tabs;

import com.ofek2608.deep_pocket.api.DPClientAPI;
import com.ofek2608.deep_pocket.api.implementable.TabContentWidget;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.utils.PocketWidgetsRenderer;
import com.ofek2608.deep_pocket.api.utils.Rect;
import com.ofek2608.deep_pocket.def.client.SimpleEditBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public final class SettingsTab extends AbstractWidget implements TabContentWidget {
	private final DPClientAPI api;
	private final LocalPlayer player;
	private final Pocket pocket;
	private Rect rect = Rect.ZERO;
	
	public SettingsTab(DPClientAPI api, LocalPlayer player, Pocket pocket) {
		super(0, 0, 0, 0, Component.literal("settings"));
		this.api = api;
		this.player = player;
		this.pocket = pocket;
	}
	
	@Override
	public void setRect(Rect rect) {
		this.rect = rect;
	}
	
	
	@Override
	public int getLeftWidth() {
		return 50;
	}
	
	@Override
	public int getRightWidth() {
		return 50;
	}
	
	@Override
	protected void renderWidget(GuiGraphics graphics, int mx, int my, float partialTick) {
		PocketWidgetsRenderer.renderBackground(rect.x0(), rect.y0(), rect.x1(), rect.y1());
		
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		graphics.drawString(font, "name", rect.x0(), rect.y0(), 0xFFFFFF);
		graphics.drawString(font, "icon", rect.x0(), rect.y0() + 10, 0xFFFFFF);
		graphics.drawString(font, "access", rect.x0(), rect.y0() + 20, 0xFFFFFF);
		graphics.drawString(font, "color", rect.x0(), rect.y0() + 30, 0xFFFFFF);
		
		int widthName = font.width("name");
		int widthIcon = font.width("icon");
		int widthAccess = font.width("access");
		int widthColor = font.width("color");
		
		SimpleEditBox editBox = new SimpleEditBox(
						font,
						rect.x0() + widthName,
						rect.y0(),
						rect.w() - widthName,
						10,
						Component.literal("Rename")
		);
		editBox.setValue(pocket.getProperties().getName());
		editBox.render(graphics, mx, my, partialTick);
		//FIXME
	}
	
	@Override
	protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
}
