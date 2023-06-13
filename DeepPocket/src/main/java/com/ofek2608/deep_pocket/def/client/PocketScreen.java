package com.ofek2608.deep_pocket.def.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DPClientAPI;
import com.ofek2608.deep_pocket.api.implementable.PocketTabDefinition;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.utils.GuiUtils;
import com.ofek2608.deep_pocket.api.utils.PocketWidgetsRenderer;
import com.ofek2608.deep_pocket.api.utils.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;
import java.util.Optional;

public final class PocketScreen extends AbstractContainerScreen<InventoryMenu> {
	private final DPClientAPI api;
	private final LocalPlayer player;
	private final Pocket pocket;
	private List<ResourceLocation> visibleTabs;
	private ResourceLocation currentTabId;
	private PocketTabDefinition.TabHandler<?> currentTabHandler;
	//render fields
	private int renderMidX;
	private Rect renderRect, contentRect;
	private final ScrollComponent scrollComponent = new ScrollComponent();
	private int hoveredSlotIndex;
	
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
		this.api = api;
		this.player = player;
		this.pocket = pocket;
		this.visibleTabs = api.getVisiblePocketTabs(player, pocket);
		
		setTab(DeepPocketMod.loc("items")); //TODO use api.getPreferredTab() instead
	}
	
	private boolean validateCurrentTab() {
		if (visibleTabs.contains(currentTabId)) {
			return true;
		}
		if (visibleTabs.size() == 0) {
			return false;
		}
		return setTab(visibleTabs.get(0));
	}
	
	public boolean setTab(ResourceLocation id) {
		Optional<PocketTabDefinition<?>> definition = api.getPocketTab(id);
		if (visibleTabs.contains(id) && definition.isPresent()) {
			if (currentTabHandler != null) {
				currentTabHandler.onClose();
			}
			setTab(id, definition.get());
			return true;
		}
		if (currentTabHandler != null) {
			return false;
		}
		// this case is when we just started, and the required id isn't available
		if (visibleTabs.size() > 0) {
			id = visibleTabs.get(0);
			definition = api.getPocketTab(id);
			if (definition.isPresent()) { //should always be true
				setTab(id, definition.get());
				return true;
			}
		}
		// we shouldn't reach here usually.
		// we don't want to continue the code execution since currentTabHandler is null
		throw new RuntimeException("Couldn't setup initial tab");
	}
	
	private void setTab(ResourceLocation id, PocketTabDefinition<?> definition) {
		currentTabId = id;
		currentTabHandler = new PocketTabDefinition.TabHandler<>(definition, api, player, pocket);
		//TODO api.setPreferredTab(id)
	}
	
	
	
	
	
	
	@Override
	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		visibleTabs = api.getVisiblePocketTabs(player, pocket);
		if (!validateCurrentTab()) {
			return;
		}
		renderBackground(poseStack);
		updateRenderFields(mx, my, partialTick);
		fireBackgroundEvent(poseStack, mx, my);
		renderOutline(poseStack, mx, my, partialTick);
		renderTabs(poseStack, mx, my, partialTick);
		renderContent(poseStack, mx, my, partialTick);
		renderInventory(mx, my);
		renderScroll(mx, my, partialTick);
		
		PocketWidgetsRenderer.renderBackground(
				renderRect.x0() + 1, renderRect.y1() - 5,
				renderRect.x1() - 1, renderRect.y1() - 1
		);
		
		fireForegroundEvent(poseStack, mx, my);
	}
	
	private void updateRenderFields(int mx, int my, float partialTick) {
		Window window = Minecraft.getInstance().getWindow();
		int width = window.getGuiScaledWidth();
		int height = window.getGuiScaledHeight();
		
		int leftWidth = currentTabHandler.getLeftWidth();
		int rightWidth = currentTabHandler.getLeftWidth();
		renderMidX = width / 2;
		renderRect = new Rect(
				renderMidX - (Math.max(leftWidth, 72) + 5),
				renderMidX + (Math.max(rightWidth, 72) + 5),
				height / 8,
				height * 7 / 8
		);
		contentRect = new Rect(
				renderMidX - leftWidth,
				renderMidX + rightWidth,
				renderRect.y0() + 5,
				renderRect.y1() - 77
		);
		
		scrollComponent.rowElementCount = currentTabHandler.getScrollRowElementCount();
		scrollComponent.elementHeight = currentTabHandler.getScrollElementHeight();
		scrollComponent.elementCount = currentTabHandler.getScrollElementCount();
		
		Rect scrollRect = currentTabHandler.getScrollRect(renderRect.h());
		scrollComponent.minX = scrollRect.x0() + renderRect.x();
		scrollComponent.maxX = scrollRect.x1() + renderRect.x();
		scrollComponent.minY = scrollRect.y0() + renderRect.y();
		scrollComponent.maxY = scrollRect.y1() + renderRect.y();
		
		scrollComponent.scrollbarX = currentTabHandler.getScrollbarX() + renderRect.x();
		
		
	}
	
	private void renderOutline(PoseStack poseStack, int mx, int my, float partialTick) {
		GuiUtils.setShaderColor(pocket.getProperties().getColor());
		GuiUtils.renderOutline(renderRect.x0(), renderRect.x1(), renderRect.y0(), renderRect.y1());
	}
	
	private void renderTabs(PoseStack poseStack, int mx, int my, float partialTick) {
		//TODO implement
	}
	
	private void renderContent(PoseStack poseStack, int mx, int my, float partialTick) {
		currentTabHandler.render(poseStack, partialTick, mx, my, contentRect);
	}
	
	private void renderInventory(int mx, int my) {
		if (!currentTabHandler.isDisplayInventory()) {
			return;
		}
		//items
		hoveredSlotIndex = -1;
		renderInventoryRow(mx, my, renderRect.y1() - 21, 0);
		renderInventoryRow(mx, my, renderRect.y1() - 73, 9);
		renderInventoryRow(mx, my, renderRect.y1() - 57, 18);
		renderInventoryRow(mx, my, renderRect.y1() - 41, 27);
		
		//rows
		PocketWidgetsRenderer.renderBackground(
				renderMidX - 72, renderRect.y1() - 77,
				renderMidX + 72, renderRect.y1() - 73
		);
		PocketWidgetsRenderer.renderBackground(
				renderMidX - 72, renderRect.y1() - 25,
				renderMidX + 72, renderRect.y1() - 21
		);
		
		//sides
		PocketWidgetsRenderer.renderBackground(
				renderRect.x0() + 1, renderRect.y1() - 77,
				renderMidX - 72, renderRect.y1() - 5
		);
		PocketWidgetsRenderer.renderBackground(
				renderMidX + 72, renderRect.y1() - 77,
				renderRect.x1() - 1, renderRect.y1() - 5
		);
	}
	
	private void renderInventoryRow(int mx, int my, int y, int inventoryOffset) {
		for (int i = 0; i < 9; i++) {
			int x = renderMidX - 72 + 16 * i;
			boolean hover = x <= mx && mx < x + 16 && y <= my && my < y + 16;
			PocketWidgetsRenderer.renderSlot(x, y, hover);
			if (hover) {
				hoveredSlotIndex = inventoryOffset + i;
			}
		}
	}
	
	private void renderScroll(int mx, int my, float partialTick) {
		PoseStack modelViewStack = RenderSystem.getModelViewStack();
		scrollComponent.render(mx, my, partialTick, i -> {
			currentTabHandler.renderScrollElement(
					modelViewStack,
					partialTick,
					mx,
					my,
					scrollComponent.minX,
					scrollComponent.minY,
					i,
					i == scrollComponent.hoveredIndex
			);
		});
	}
	
	@Override
	public void mouseMoved(double mx, double my) {
		super.mouseMoved(mx, my);
		scrollComponent.mouseMoved(mx, my);
	}
	
	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (scrollComponent.mouseClicked(mx, my, button)) {
			return true;
		}
		return super.mouseClicked(mx, my, button);
	}
	
	@Override
	public boolean mouseReleased(double mx, double my, int button) {
		scrollComponent.mouseReleased(mx, my, button);
		return super.mouseReleased(mx, my, button);
	}
	
	@SuppressWarnings("UnstableApiUsage")
	private void fireBackgroundEvent(PoseStack poseStack, int mx, int my) {
		MinecraftForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Background(this, poseStack, mx, my));
	}
	
	@SuppressWarnings("UnstableApiUsage")
	private void fireForegroundEvent(PoseStack poseStack, int mx, int my) {
		MinecraftForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Foreground(this, poseStack, mx, my));
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// This function is disabled since I don't call super.render in this.render
	@Override
	public void renderBg(PoseStack poseStack, float partialTick, int mx, int my) {
		throw new UnsupportedOperationException("Disabled");
	}
}
