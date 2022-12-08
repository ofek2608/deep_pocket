package com.ofek2608.deep_pocket.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import com.ofek2608.deep_pocket.api.enums.PocketDisplayFilter;
import com.ofek2608.deep_pocket.api.enums.SearchMode;
import com.ofek2608.deep_pocket.api.enums.SortingOrder;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SearchWidget extends SimpleContainerWidget {
	private static final DeepPocketClientHelper HELPER = DeepPocketClientHelper.get();
	public static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/widget/search.png");
	private final DPTextWidget searchWidget;
	private final ImageButton btnSearchMode;
	private final ImageButton btnSortingOrder;
	private final ImageButton btnSortAscending;
	private final ImageButton btnDisplayFilter;
	
	public SearchWidget() {
		children.add(searchWidget = new DPTextWidget(100));
		children.add(btnSearchMode = new ImageButton(0, 0, 10, 10, 0, 0, TEXTURE, btn->{
			HELPER.setSearchMode(switch (HELPER.getSearchMode()) {
				case NORMAL -> SearchMode.SYNC_JEI;
				case SYNC_JEI -> SearchMode.SYNC_FROM_JEI;
				case SYNC_FROM_JEI -> SearchMode.SYNC_TO_JEI;
				case SYNC_TO_JEI -> SearchMode.NORMAL;
			});
			updateXOffset();
		}));
		children.add(btnSortingOrder = new ImageButton(0, 0, 10, 10, 0, 20, TEXTURE, btn->{
			HELPER.setSortingOrder(switch (HELPER.getSortingOrder()) {
				case COUNT -> SortingOrder.ID;
				case ID -> SortingOrder.NAME;
				case NAME -> SortingOrder.MOD;
				case MOD -> SortingOrder.COUNT;
			});
			updateXOffset();
		}));
		children.add(btnSortAscending = new ImageButton(0, 0, 10, 10, 0, 40, TEXTURE, btn->{
			HELPER.setSortAscending(!HELPER.isSortAscending());
			updateXOffset();
		}));
		children.add(btnDisplayFilter = new ImageButton(0, 0, 10, 10, 0, 60, TEXTURE, btn->{
			HELPER.setPocketDisplayFilter(switch (HELPER.getPocketDisplayFilter()) {
				case ITEMS_AND_FLUIDS -> PocketDisplayFilter.ITEMS;
				case ITEMS -> PocketDisplayFilter.FLUIDS;
				case FLUIDS -> PocketDisplayFilter.ITEMS_AND_FLUIDS;
			});
			updateXOffset();
		}));
		
		searchWidget.setResponder(HELPER::setSearch);
		
		updateXOffset();
	}
	
	@Override
	public void renderTooltip(Screen screen, PoseStack poseStack, int mx, int my) {
		if (btnSearchMode.isMouseOver(mx, my)) {
			screen.renderTooltip(poseStack,
					Component.literal("Search Mode: ").append(
							Component.literal(HELPER.getSearchMode().displayName)
									.withStyle(ChatFormatting.AQUA)),
					mx, my
			);
		}
		if (btnSortingOrder.isMouseOver(mx, my)) {
			screen.renderTooltip(poseStack,
					Component.literal("Sort: ").append(
							Component.literal(HELPER.getSortingOrder().displayName)
									.withStyle(ChatFormatting.AQUA)),
					mx, my
			);
		}
		if (btnSortAscending.isMouseOver(mx, my)) {
			screen.renderTooltip(poseStack,
					Component.literal("Sort Direction: ").append(
							Component.literal(HELPER.isSortAscending() ? "Ascending" : "Descending")
									.withStyle(ChatFormatting.AQUA)),
					mx, my
			);
		}
		if (btnDisplayFilter.isMouseOver(mx, my)) {
			screen.renderTooltip(poseStack,
					Component.literal("Display: ").append(
							Component.literal(HELPER.getPocketDisplayFilter().displayName)
									.withStyle(ChatFormatting.AQUA)),
					mx, my
			);
		}
	}
	
	@Override
	protected void updatePositions() {
		searchWidget.x = offX + 4;
		searchWidget.y = offY;
		
		btnSearchMode.x = offX + 108;
		btnSearchMode.y = offY;
		btnSortingOrder.x = offX + 118;
		btnSortingOrder.y = offY;
		btnSortAscending.x = offX + 128;
		btnSortAscending.y = offY;
		btnDisplayFilter.x = offX + 138;
		btnDisplayFilter.y = offY;
		
		updateXOffset();
	}
	
	private void updateXOffset() {
		btnSearchMode.xTexStart = 216 + 10 * HELPER.getSearchMode().ordinal();
		btnSortingOrder.xTexStart = 216 + 10 * HELPER.getSortingOrder().ordinal();
		btnSortAscending.xTexStart = HELPER.isSortAscending() ? 246 : 236;
		btnDisplayFilter.xTexStart = 226 + 10 * HELPER.getPocketDisplayFilter().ordinal();
	}
	
	@Override
	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		//Fixing search
		String searchNeededValue = HELPER.getSearch();
		if (!searchWidget.getValue().equals(searchNeededValue))
			searchWidget.setValue(searchNeededValue);
		
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);
		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		Screen.blit(
				poseStack,
				offX, offY,
				0, 0,
				152, 10,
				256, 256
		);
		super.render(poseStack, mx, my, partialTick);
	}
}
