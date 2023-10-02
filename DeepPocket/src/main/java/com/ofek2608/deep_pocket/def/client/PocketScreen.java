package com.ofek2608.deep_pocket.def.client;

import com.mojang.blaze3d.platform.Window;
import com.ofek2608.deep_pocket.api.DPClientAPI;
import com.ofek2608.deep_pocket.api.implementable.TabContentWidget;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.utils.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.common.MinecraftForge;

public final class PocketScreen extends AbstractContainerScreen<InventoryMenu> {
	private final TabSelectionWidget tabSelectionWidget;
	private final PocketBackgroundWidget pocketBackgroundWidget;
	private final InventoryWidget inventoryWidget;
	private TabContentWidget tabContentWidget;
	private final PocketScrollWidget pocketScrollWidget;
	
	public static boolean open(DPClientAPI api, LocalPlayer player, Pocket pocket) {
		try {
			PocketScreen screen = new PocketScreen(api, player, pocket);
			Minecraft.getInstance().setScreen(screen);
			return true;
		} catch (Exception ignored) {
			return false;
		}
	}
	
	private PocketScreen(DPClientAPI api, LocalPlayer player, Pocket pocket) {
		super(player.inventoryMenu, player.getInventory(), Component.empty());
		
		this.tabSelectionWidget = new TabSelectionWidget(api, player, pocket, this::setTabContentWidget);
		this.pocketBackgroundWidget = new PocketBackgroundWidget(api, pocket);
		this.inventoryWidget =  new InventoryWidget(player);
		this.pocketScrollWidget = new PocketScrollWidget();
	}
	
	public void setTabContentWidget(TabContentWidget tabContentWidget) {
		this.tabContentWidget = tabContentWidget;
		if (!children().isEmpty()) {
			rebuildWidgets();
		}
	}
	
	@Override
	protected void init() {
		addRenderableWidget(tabSelectionWidget);
		addRenderableWidget(pocketBackgroundWidget);
		addRenderableWidget(inventoryWidget);
		if (tabContentWidget != null) {
			addRenderableWidget(tabContentWidget);
		}
		addRenderableWidget(pocketScrollWidget);
	}
	
	@Override
	public void render(GuiGraphics graphics, int mx, int my, float partialTick) {
		tabSelectionWidget.updateTabs();
		if (tabContentWidget == null) {
			onClose();
			return;
		}
		
		updateRects(tabContentWidget);
		fireBackgroundEvent(graphics, mx, my);
		renderBackground(graphics);
		tabContentWidget.render(graphics, mx, my, partialTick);
		for(Renderable renderable : this.renderables) {
			renderable.render(graphics, mx, my, partialTick);
		}
		//TODO render dragging slot
		
		fireForegroundEvent(graphics, mx, my);
	}
	
	
	private void updateRects(TabContentWidget tabContentWidget) {
		Window window = Minecraft.getInstance().getWindow();
		int windowWidth = window.getGuiScaledWidth();
		int windowHeight = window.getGuiScaledHeight();
		int height = Math.min(windowHeight * 3 / 4, 360);
		
		int leftWidth = tabContentWidget.getLeftWidth();
		int rightWidth = tabContentWidget.getRightWidth();
		boolean isDisplayInventory = tabContentWidget.isDisplayInventory();
		int renderMidX = windowWidth / 2;
		Rect renderRect = new Rect(
						renderMidX - (Math.max(leftWidth, 72) + 5),
						renderMidX + (Math.max(rightWidth, 72) + 5),
						(windowHeight - height) / 2,
						(windowHeight - height) / 2 + height
		);
		Rect contentRect = new Rect(
				renderMidX - leftWidth,
				renderMidX + rightWidth,
				renderRect.y0() + 25,
				renderRect.y1() - (isDisplayInventory ? 77 : 5)
		);
		
		tabSelectionWidget.setRenderRect(renderRect);
		pocketBackgroundWidget.setRects(renderRect, contentRect);
		inventoryWidget.setRenderRect(renderRect);
		inventoryWidget.setDisplayed(isDisplayInventory);
		pocketScrollWidget.setContentRect(contentRect);
		pocketScrollWidget.setTabContentWidget(tabContentWidget);
		tabContentWidget.setRect(contentRect);
		
		leftPos = renderRect.x();
		topPos = renderRect.y();
		imageWidth = renderRect.w();
		imageHeight = renderRect.h();
	}
	
	@Override
	public boolean mouseScrolled(double mx, double my, double delta) {
		if (pocketScrollWidget.mouseScrolled(mx, my, delta)) {
			return true;
		}
		return tabContentWidget.mouseScrolled(mx, my, delta);
	}
	
	@Override
	public boolean mouseReleased(double mx, double my, int button) {
		boolean changed = false;
		for (GuiEventListener child : this.children()) {
			if (child.mouseReleased(mx, my, button)) {
				changed = true;
			}
		}
		return changed;
	}
	
	@Override
	public void mouseMoved(double mx, double my) {
		for(GuiEventListener child : this.children()) {
			child.mouseMoved(mx, my);
		}
	}
	
	@SuppressWarnings("UnstableApiUsage")
	private void fireBackgroundEvent(GuiGraphics graphics, int mx, int my) {
		MinecraftForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Background(this, graphics, mx, my));
	}
	
	@SuppressWarnings("UnstableApiUsage")
	private void fireForegroundEvent(GuiGraphics graphics, int mx, int my) {
		MinecraftForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Foreground(this, graphics, mx, my));
	}
	
	//
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// This function is disabled since I don't call super.render in this.render
	@Override
	public void renderBg(GuiGraphics graphics, float partialTick, int mx, int my) {
		throw new UnsupportedOperationException("Disabled");
	}
}
