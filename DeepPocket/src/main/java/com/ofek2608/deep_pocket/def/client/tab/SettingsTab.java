package com.ofek2608.deep_pocket.def.client.tab;

import com.ofek2608.deep_pocket.api.DPClientAPI;
import com.ofek2608.deep_pocket.api.enums.PocketAccess;
import com.ofek2608.deep_pocket.api.implementable.TabContentWidget;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.pocket.PocketProperties;
import com.ofek2608.deep_pocket.api.utils.PocketWidgetsRenderer;
import com.ofek2608.deep_pocket.api.utils.Rect;
import com.ofek2608.deep_pocket.def.client.widget.CompactEditBox;
import com.ofek2608.deep_pocket.def.client.widget.SimpleButton;
import com.ofek2608.deep_pocket.def.client.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public final class SettingsTab extends WidgetGroup implements TabContentWidget {
	private final DPClientAPI api;
	private final LocalPlayer player;
	private final Pocket pocket;
	
	private final Font font;
	private final EditBox nameInput;
	private final SimpleButton accessButton;
	private final SimpleButton confirmButton;
	
	private PocketAccess access;
	
	private Rect rect = Rect.ZERO;
	
	
	public SettingsTab(DPClientAPI api, LocalPlayer player, Pocket pocket) {
		super(0, 0, 0, 0, Component.literal("settings"));
		this.api = api;
		this.player = player;
		this.pocket = pocket;
		this.font = Minecraft.getInstance().font;
		PocketProperties properties = pocket.getProperties();
		
		access = properties.getAccess();
		
		this.nameInput = new CompactEditBox(font, 0, 0, 96 - font.width("name"), 12, Component.literal("Rename"));
		this.accessButton = new SimpleButton(font, 0, 0, 96 - font.width("access"), 12, Component.empty(), this::toggleAccess);
		this.confirmButton = new SimpleButton(font, 0, 0, 50, 12, Component.literal("confirm"), this::confirm);
		
		nameInput.setValue(pocket.getProperties().getName());
		updateAccessButton();
		children.add(nameInput);
		children.add(accessButton);
		children.add(confirmButton);
	}
	
	private void toggleAccess() {
		access = switch (access) {
			case PRIVATE -> api.getServerConfig().allowPublicPockets() ? PocketAccess.PUBLIC : PocketAccess.TEAM;
			case PUBLIC -> PocketAccess.TEAM;
			case TEAM -> PocketAccess.PRIVATE;
		};
		updateAccessButton();
	}
	
	private void updateAccessButton() {
		accessButton.setMessage(access.display);
	}
	
	@Override
	public void setRect(Rect rect) {
		this.rect = rect;
		
		int widthName = font.width("name") + 4;
		int widthIcon = font.width("icon") + 4;
		int widthAccess = font.width("access") + 4;
		int widthColor = font.width("color") + 4;
		
		nameInput.setX(rect.x0() + widthName);
		nameInput.setY(rect.y0());
		nameInput.setWidth(rect.w() - widthName);
		nameInput.setHeight(12);
		
		accessButton.setX(rect.x0() + widthAccess);
		accessButton.setY(rect.y0() + 36);
		accessButton.setWidth(rect.w() - widthAccess);
		accessButton.setHeight(12);
		
		confirmButton.setX(rect.x0() + rect.w() / 4);
		confirmButton.setY(rect.y1() - 12);
		confirmButton.setWidth(rect.w() / 2);
		confirmButton.setHeight(12);
	}
	
	private void confirm() {
		//TODO send to server
		System.out.println("Modify");
		System.out.println("  name: " + nameInput.getValue());
		System.out.println("  access: " + access);
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
		
		super.renderWidget(graphics, mx, my, partialTick);
		
		
		graphics.drawString(font, "name", rect.x0(), rect.y0() + 2, 0xFFFFFF);
		graphics.drawString(font, "icon", rect.x0(), rect.y0() + 20, 0xFFFFFF);
		graphics.drawString(font, "access", rect.x0(), rect.y0() + 38, 0xFFFFFF);
		graphics.drawString(font, "color", rect.x0(), rect.y0() + 54, 0xFFFFFF);
		
		PocketWidgetsRenderer.renderSlot(rect.x0() + font.width("icon") + 4, rect.y0() + 16, false);
		//FIXME
	}
	
	@Override
	protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
}
