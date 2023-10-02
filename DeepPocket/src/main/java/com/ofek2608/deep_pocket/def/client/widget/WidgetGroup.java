package com.ofek2608.deep_pocket.def.client.widget;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.List;

public abstract class WidgetGroup extends AbstractWidget {
	protected final List<Object> children = Lists.newArrayList();
	private GuiEventListener focused;
	
	public WidgetGroup(int pX, int pY, int pWidth, int pHeight, Component pMessage) {
		super(pX, pY, pWidth, pHeight, pMessage);
	}
	
	@Override
	public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
	}
	
	@Override
	protected void renderWidget(GuiGraphics graphics, int mx, int my, float partialTick) {
		for (var child : children) {
			if (child instanceof Renderable renderable) {
				renderable.render(graphics, mx, my, partialTick);
			}
		}
	}
	
	
	
	
	@Override
	public void mouseMoved(double mx, double my) {
		for (var child : children) {
			if (child instanceof GuiEventListener listener) {
				listener.mouseMoved(mx, my);
			}
		}
	}
	
	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		for (var child : children) {
			if (child instanceof GuiEventListener listener) {
				if (listener.mouseClicked(mx, my, button)) {
					setFocused(listener);
					return true;
				}
			}
		}
		setFocused(null);
		return false;
	}
	
	@Override
	public boolean mouseReleased(double mx, double my, int button) {
		for (var child : children) {
			if (child instanceof GuiEventListener listener) {
				if (listener.mouseReleased(mx, my, button)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
		for (var child : children) {
			if (child instanceof GuiEventListener listener) {
				if (listener.mouseDragged(mx, my, button, dx, dy)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean mouseScrolled(double mx, double my, double delta) {
		for (var child : children) {
			if (child instanceof GuiEventListener listener) {
				if (listener.mouseScrolled(mx, my, delta)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		for (var child : children) {
			if (child instanceof GuiEventListener listener) {
				if (listener.keyPressed(keyCode, scanCode, modifiers)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		for (var child : children) {
			if (child instanceof GuiEventListener listener) {
				if (listener.keyReleased(keyCode, scanCode, modifiers)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		for (var child : children) {
			if (child instanceof GuiEventListener listener) {
				if (listener.charTyped(codePoint, modifiers)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public @Nullable GuiEventListener getFocused() {
		return focused;
	}
	
	public void setFocused(@Nullable GuiEventListener pListener) {
		if (this.focused != null) {
			this.focused.setFocused(false);
		}
		
		if (pListener != null) {
			pListener.setFocused(true);
		}
		
		this.focused = pListener;
	}
}
