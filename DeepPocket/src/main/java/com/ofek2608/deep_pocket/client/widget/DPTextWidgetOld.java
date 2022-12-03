package com.ofek2608.deep_pocket.client.widget;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.function.Consumer;

public class DPTextWidgetOld implements Widget, GuiEventListener, NonNarratableEntry {
	public int offX;
	public int offY;
	public int width;
	public Consumer<String> onType;
	private String typed;
	private boolean focused;
	private int selectS;
	private int selectE;
	
	public DPTextWidgetOld(int offX, int offY, int width, String initialValue, Consumer<String> onType) {
		this.offX = offX;
		this.offY = offY;
		this.width = width;
		this.onType = onType;
		this.typed = initialValue;
	}
	
	
	@Override
	public void render(PoseStack stack, int mx, int my, float partialTick) {
	
	}
	
	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (mx < offX || offX + width <= mx || my < offY || offY + 10 <= my)
			return false;
		if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
			if (typed.length() > 0) {
				typed = "";
				selectS = selectE = 0;
			}
			return focused;
		}
		return GuiEventListener.super.mouseClicked(mx, my, button);
	}
	
	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		return GuiEventListener.super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}
}
