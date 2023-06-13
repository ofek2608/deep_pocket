package com.ofek2608.deep_pocket.def.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DPClientAPI;
import com.ofek2608.deep_pocket.api.implementable.PocketTabDefinition;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.utils.GuiUtils;
import com.ofek2608.deep_pocket.api.utils.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
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
	private int renderMidX, renderX, renderY, renderW, renderH;
	private final ScrollComponent scrollComponent = new ScrollComponent();
	
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
		renderWindow(poseStack, mx, my, partialTick);
		renderScroll(mx, my, partialTick);
		
		fireForegroundEvent(poseStack, mx, my);
	}
	
	private void updateRenderFields(int mx, int my, float partialTick) {
		Window window = Minecraft.getInstance().getWindow();
		int width = window.getGuiScaledWidth();
		int height = window.getGuiScaledHeight();
		renderMidX = width / 2;
		renderY = height / 8 + 1;
		renderH = height * 7 / 8 - 1 - renderY;
		int leftW = currentTabHandler.getLeftWidth();
		int rightW = currentTabHandler.getRightWidth();
		renderX = renderMidX - leftW;
		renderW = leftW + rightW;
		
		scrollComponent.rowElementCount = currentTabHandler.getScrollRowElementCount();
		scrollComponent.elementHeight = currentTabHandler.getScrollElementHeight();
		scrollComponent.elementCount = currentTabHandler.getScrollElementCount();
		
		Rect scrollRect = currentTabHandler.getScrollRect();
		scrollComponent.minX = scrollRect.x0() + renderX;
		scrollComponent.maxX = scrollRect.x1() + renderX;
		scrollComponent.minY = scrollRect.y0() + renderY;
		scrollComponent.maxY = scrollRect.y1() + renderY;
		
		scrollComponent.scrollbarX = currentTabHandler.getScrollbarX() + renderX;
		
		
	}
	
	private void renderOutline(PoseStack poseStack, int mx, int my, float partialTick) {
		GuiUtils.setShaderColor(pocket.getProperties().getColor());
		GuiUtils.renderOutline(renderX - 1, renderX + renderW + 1, renderY - 1, renderY + renderH + 1);
		//TODO implement
	}
	
	private void renderTabs(PoseStack poseStack, int mx, int my, float partialTick) {
		//TODO implement
	}
	
	private void renderWindow(PoseStack poseStack, int mx, int my, float partialTick) {
		//TODO implement
	}
	
	private void renderScroll(int mx, int my, float partialTick) {
		if (scrollComponent.elementCount <= 0) {
			return;
		}
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
		
		
	}
	
	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		
		return super.mouseClicked(mx, my, button);
	}
	
	@Override
	public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
		return super.mouseReleased(pMouseX, pMouseY, pButton);
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
