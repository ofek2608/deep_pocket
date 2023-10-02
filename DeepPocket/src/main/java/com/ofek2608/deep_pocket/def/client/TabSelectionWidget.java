package com.ofek2608.deep_pocket.def.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DPClientAPI;
import com.ofek2608.deep_pocket.api.implementable.PocketTabDefinition;
import com.ofek2608.deep_pocket.api.implementable.TabContentWidget;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.utils.GuiUtils;
import com.ofek2608.deep_pocket.api.utils.PocketWidgetsRenderer;
import com.ofek2608.deep_pocket.api.utils.Rect;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class TabSelectionWidget extends AbstractWidget {
	private final DPClientAPI api;
	private final LocalPlayer player;
	private final Pocket pocket;
	private final Consumer<TabContentWidget> onSelect;
	private Rect renderRect = Rect.ZERO;
	
	private List<ResourceLocation> visibleTabs;
	private int tabPage = 0;
	private int tabPageCount = 0;
	private int hoverTabIndex = -1;
	private ResourceLocation currentTabId;
	private TabContentWidget tabContentWidget;
	
	public TabSelectionWidget(DPClientAPI api, LocalPlayer player, Pocket pocket, Consumer<TabContentWidget> onSelect) {
		super(0, 0, 0, 0, Component.literal("tabs"));
		this.api = api;
		this.player = player;
		this.pocket = pocket;
		this.onSelect = onSelect;
		visibleTabs = api.getVisiblePocketTabs(player, pocket);
		
		//TODO api.getPreferredTab()
		setTab(DeepPocketMod.loc("items"));
	}
	
	public void setRenderRect(Rect renderRect) {
		this.renderRect = renderRect;
	}
	
	public @Nullable TabContentWidget updateTabs() {
		visibleTabs = api.getVisiblePocketTabs(player, pocket);
		if (validateCurrentTab()) {
			return tabContentWidget;
		}
		return null;
	}
	
	@Override
	protected void renderWidget(GuiGraphics graphics, int mx, int my, float partialTick) {
		tabPageCount = Math.max((visibleTabs.size() + 6) / 7, 1);
		tabPage =  Math.min(tabPage, tabPageCount - 1);
		
		
		int renderMidX = renderRect.midX();
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
	
	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {}
	
	public boolean validateCurrentTab() {
		if (visibleTabs.contains(currentTabId)) {
			return true;
		}
		if (visibleTabs.isEmpty()) {
			return false;
		}
		return setTab(visibleTabs.get(0));
	}
	
	public boolean setTab(ResourceLocation id) {
		Optional<PocketTabDefinition> definition = api.getPocketTab(id);
		if (visibleTabs.contains(id) && definition.isPresent()) {
			if (tabContentWidget != null) {
				tabContentWidget.onClose();
			}
			setTab(id, definition.get());
			return true;
		}
		if (tabContentWidget != null) {
			return false;
		}
		// this case is when we just started, and the required id isn't available
		if (!visibleTabs.isEmpty()) {
			id = visibleTabs.get(0);
			definition = api.getPocketTab(id);
			if (definition.isPresent()) { //should always be true
				setTab(id, definition.get());
				return true;
			}
		}
		return false;
	}
	
	private void setTab(ResourceLocation id, PocketTabDefinition definition) {
		currentTabId = id;
		tabContentWidget = definition.createWidget(api, player, pocket);
		onSelect.accept(tabContentWidget);
		//TODO api.setPreferredTab(id)
	}
	
	public TabContentWidget getTabContentWidget() {
		return tabContentWidget;
	}
	
	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button != 0 || hoverTabIndex < 0) {
			return false;
		}
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
		if (0 <= listIndex && listIndex < visibleTabs.size()) {
			if (setTab(visibleTabs.get(listIndex))) {
				GuiUtils.playClickSound();
			}
			return true;
		}
		return false;
	}
}
