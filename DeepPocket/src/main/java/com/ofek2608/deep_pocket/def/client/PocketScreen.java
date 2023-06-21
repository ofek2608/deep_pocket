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
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
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
	private int tabPage = 0;
	private int tabPageCount = 0;
	//hover
	private int hoveredSlotIndex;
	private int hoverTabIndex = -1;
	
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
		renderTabs(mx, my, partialTick);
		renderOutline(mx, my, partialTick);
		renderInventory(mx, my);
		renderScroll(mx, my, partialTick);
		renderContentBackground(poseStack, mx, my, partialTick);
		//TODO render pocket name and icon
		renderInventoryItems();
		renderContentForeground(poseStack, mx, my, partialTick);
		//TODO render hover
		//TODO render content hover
		//TODO render dragging slot
		
		PocketWidgetsRenderer.renderBackground(
				renderRect.x0() + 1, renderRect.y1() - 5,
				renderRect.x1() - 1, renderRect.y1() - 1
		);
		
		fireForegroundEvent(poseStack, mx, my);
	}
	
	private void updateRenderFields(int mx, int my, float partialTick) {
		tabPageCount = Math.max((visibleTabs.size() + 6) / 7, 1);
		tabPage =  Math.min(tabPage, tabPageCount - 1);
		
		Window window = Minecraft.getInstance().getWindow();
		int windowWidth = window.getGuiScaledWidth();
		int windowHeight = window.getGuiScaledHeight();
		int height = Math.min(windowHeight * 3 / 4, 360);
		
		int leftWidth = currentTabHandler.getLeftWidth();
		int rightWidth = currentTabHandler.getRightWidth();
		boolean isDisplayInventory = currentTabHandler.isDisplayInventory();
		renderMidX = windowWidth / 2;
		renderRect = new Rect(
						renderMidX - (Math.max(leftWidth, 72) + 5),
						renderMidX + (Math.max(rightWidth, 72) + 5),
						(windowHeight - height) / 2,
						(windowHeight - height) / 2 + height
		);
		contentRect = new Rect(
				renderMidX - leftWidth,
				renderMidX + rightWidth,
				renderRect.y0() + 5,
				renderRect.y1() - (isDisplayInventory ? 77 : 5)
		);
		
		scrollComponent.rowElementCount = currentTabHandler.getScrollRowElementCount();
		scrollComponent.elementHeight = currentTabHandler.getScrollElementHeight();
		scrollComponent.elementCount = currentTabHandler.getScrollElementCount();
		
		Rect scrollRect = currentTabHandler.getScrollRect(contentRect.h());
		scrollComponent.minX = scrollRect.x0() + contentRect.x();
		scrollComponent.maxX = scrollRect.x1() + contentRect.x();
		scrollComponent.minY = scrollRect.y0() + contentRect.y();
		scrollComponent.maxY = scrollRect.y1() + contentRect.y();
		
		scrollComponent.scrollbarX = currentTabHandler.getScrollbarX() + contentRect.x();
	}
	
	private void renderOutline(int mx, int my, float partialTick) {
		GuiUtils.setShaderColor(pocket.getProperties().getColor());
		GuiUtils.renderOutline(renderRect.x0(), renderRect.x1(), renderRect.y0(), renderRect.y1());
	}
	
	private void renderTabs(int mx, int my, float partialTick) {
		int dmx = mx - (renderMidX - 72);
		int dmy = my - renderRect.y0();
		if (dmx < 0 || 144 <= dmx || dmy < -24 || 0 <= dmy) {
			hoverTabIndex = -1;
		} else {
			hoverTabIndex = dmx / 16;
			if ((hoverTabIndex == 0 || hoverTabIndex == 8) && dmy < -16) {
				hoverTabIndex = -1;
			}
		}
		
		Sprite TAB_NORMAL = Sprite.rect(0, 0, 16, 24);
		Sprite TAB_HOVER = Sprite.rect(16, 0, 16, 24);
		
		int pocketColor = pocket.getProperties().getColor();
		
		int visibleTabCount = Math.min(visibleTabs.size() - 7 * tabPage, 7);

		for (int i = 0; i < visibleTabCount; i++) {
			int tabIndex = tabPage * 7 + i;
			ResourceLocation tabId = visibleTabs.get(tabIndex);
			ResourceLocation tabTexture = new ResourceLocation(tabId.getNamespace(), "textures/deep_pocket_tab/" + tabId.getPath() + ".png");
			
			boolean isHover = hoverTabIndex == i + 1;
			int tabX = renderMidX - 56 + i * 16;
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1, 1, 1, 1);
			RenderSystem.setShaderTexture(0, DeepPocketMod.loc("textures/gui/tabs.png"));
			(isHover ? TAB_HOVER : TAB_NORMAL).blit(tabX, renderRect.y0() - 24);
			RenderSystem.setShaderTexture(0, tabTexture);
			GuiUtils.blitFullTexture(
							tabX, renderRect.y0() - (isHover ? 24 : 16),
							tabX + 16, renderRect.y0() - (isHover ? 8 : 0)
			);
			
			
			GuiUtils.setShaderColor(pocketColor);
			GuiUtils.renderHLine(tabX, tabX + 16, renderRect.y0() - (isHover ? 25 : 17));
			if (isHover) {
				GuiUtils.renderVLine(tabX - 1, renderRect.y0() - 25, renderRect.y0() - 17);
				GuiUtils.renderVLine(tabX + 16, renderRect.y0() - 25, renderRect.y0() - 17);
			}
		}
		
		if (tabPage > 0) {
			PocketWidgetsRenderer.renderButtonLeft(renderMidX - 72, renderRect.y0() - 16, hoverTabIndex == 0 ? 2 : 1);
			GuiUtils.setShaderColor(pocketColor);
			GuiUtils.renderVLine(renderMidX - 73, renderRect.y0() - 17, renderRect.y0());
			GuiUtils.renderHLine(renderMidX - 72, renderMidX - 56, renderRect.y0() - 17);
		} else {
			GuiUtils.setShaderColor(pocketColor);
			GuiUtils.renderVLine(renderMidX - 57, renderRect.y0() - 17, renderRect.y0());
		}
		if (tabPage < tabPageCount - 1) {
			PocketWidgetsRenderer.renderButtonRight(renderMidX - 56, renderRect.y0() - 16, hoverTabIndex == 8 ? 2 : 1);
			GuiUtils.setShaderColor(pocketColor);
			GuiUtils.renderVLine(renderMidX + 72, renderRect.y0() - 17, renderRect.y0());
			GuiUtils.renderHLine(renderMidX + 56, renderMidX + 72, renderRect.y0() - 17);
		} else {
			GuiUtils.setShaderColor(pocketColor);
			GuiUtils.renderVLine(renderMidX - 56 + visibleTabCount * 16, renderRect.y0() - 17, renderRect.y0());
		}
	}
	
	private void renderContentBackground(PoseStack poseStack, int mx, int my, float partialTick) {
		PocketWidgetsRenderer.renderBackground(
						contentRect.x0(), renderRect.y0() + 1,
						contentRect.x1(), contentRect.y0()
		);
		PocketWidgetsRenderer.renderBackground(
						renderRect.x0() + 1, renderRect.y0() + 1,
						contentRect.x0(), contentRect.y1()
		);
		PocketWidgetsRenderer.renderBackground(
						contentRect.x1(), renderRect.y0() + 1,
						renderRect.x1() - 1, contentRect.y1()
		);
		
		currentTabHandler.renderBackground(poseStack, partialTick, mx, my, contentRect);
	}
	
	private void renderContentForeground(PoseStack poseStack, int mx, int my, float partialTick) {
		currentTabHandler.renderForeground(poseStack, partialTick, mx, my, contentRect);
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
	
	private void renderInventoryItems() {
		if (!currentTabHandler.isDisplayInventory()) {
			return;
		}
		renderInventoryItemsRow(renderRect.y1() - 21, 0);
		renderInventoryItemsRow(renderRect.y1() - 73, 9);
		renderInventoryItemsRow(renderRect.y1() - 57, 18);
		renderInventoryItemsRow(renderRect.y1() - 41, 27);
	}
	
	private void renderInventoryItemsRow(int y, int inventoryOffset) {
		Minecraft minecraft = Minecraft.getInstance();
		ItemRenderer renderer = minecraft.getItemRenderer();
		Inventory inventory = player.getInventory();
		for (int i = 0; i < 9; i++) {
			int x = renderMidX - 72 + 16 * i;
			int slotIndex = inventoryOffset + i;
			boolean hover = hoveredSlotIndex == slotIndex;
			renderer.renderGuiItem(inventory.getItem(slotIndex), x, y);
			if (hover) {
				RenderSystem.setShaderColor(1, 1, 1, 0.2f);
				GuiUtils.renderRect(x, x + 16, y, y + 16);
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
		if (hoverTabIndex >= 0) {
			if (hoverTabIndex == 0 && tabPage > 0) {
				tabPage--;
				GuiUtils.playClickSound();
				return true;
			}
			if (hoverTabIndex == 8 && tabPage + 1 < tabPageCount) {
				tabPage++;
				GuiUtils.playClickSound();
				return true;
			}
			int listIndex = hoverTabIndex - 1 + tabPage * 7;
			if (listIndex < visibleTabs.size()) {
				if (setTab(visibleTabs.get(listIndex))) {
					GuiUtils.playClickSound();
				}
				return true;
			}
		}
//		return super.mouseClicked(mx, my, button);
		return false;
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
