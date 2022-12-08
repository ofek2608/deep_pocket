package com.ofek2608.deep_pocket.registry.pocket_screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.enums.PocketDisplayMode;
import com.ofek2608.deep_pocket.api.enums.SearchMode;
import com.ofek2608.deep_pocket.api.enums.SortingOrder;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.client.client_screens.ClientScreens;
import com.ofek2608.deep_pocket.client.widget.*;
import com.ofek2608.deep_pocket.integration.DeepPocketJEI;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class PocketScreen extends AbstractContainerScreen<PocketMenu> {
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/pocket.png");
	private final DeepPocketClientHelper dpClientHelper = DeepPocketClientHelper.get();

	//render fields
	private PocketDisplayMode pocketDisplayMode;
	private int rowCount;
	//render hover fields
	@Nullable private String lastJeiSearch = DeepPocketJEI.getSearch();
	private int hoverSlotIndex;
	private int hoverButton;
	//widgets
	private final PocketWidget pocketWidget;
	private final DPTextWidget searchWidget;
	private final PocketTabWidget pocketTabWidget;
	private final PatternWidget patternWidget;
	
	public PocketScreen(PocketMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
		menu.screen = this;
		addRenderableWidget(searchWidget = new DPTextWidget(0, 0, 88));
		addRenderableWidget(pocketWidget = new PocketWidget(this, 0, 0, 144, menu::getPocket, this::createFilter));
		addRenderableWidget(pocketTabWidget = new PocketTabWidget(40, 0, menu::getPocket));
		addRenderableWidget(patternWidget = new PatternWidget(this));
		
		patternWidget.setPos(100, 50);
		
		searchWidget.setResponder(newValue -> {
			if (DeepPocketClientApi.get().getSearchMode().syncTo)
				DeepPocketJEI.setSearch(newValue);
		});
	}
	
	@Override
	protected void init() {
		super.init();
	}
	
	@Override
	protected void rebuildWidgets() {
		this.setFocused(null);
	}
	
	private Predicate<Pocket.Entry> createFilter() {
		Predicate<ElementType> searchFilter = DeepPocketUtils.createFilter(searchWidget.getValue());
		return entry -> {
			ElementType type = entry.getType();
			return (type instanceof ElementType.TItem ||
					type instanceof ElementType.TFluid
			) && searchFilter.test(type);
		};
	}

	public void setPattern(ItemType[] input, ItemStack output) {
		patternWidget.setPattern(input, output);
	}


	private void reloadPosition() {
		this.pocketDisplayMode = DeepPocketClientApi.get().getPocketDisplayMode();
		int imageHeightExcludingRows = pocketDisplayMode == PocketDisplayMode.NORMAL ? 96 : 148;
		this.rowCount = Math.max(Math.min((height - imageHeightExcludingRows) / 16, 9), 3);
		this.imageWidth = 180;
		this.imageHeight = rowCount * 16 + imageHeightExcludingRows;
		this.leftPos = (this.width - 152) / 2 - 15;
		this.topPos = (this.height - this.imageHeight) / 2;
		
		this.pocketWidget.offX = leftPos + 15;
		this.pocketWidget.offY = topPos + 19;
		this.pocketWidget.height = rowCount * 16;
		
		this.searchWidget.x = leftPos + 75;
		this.searchWidget.y = topPos + 5;
		
		this.patternWidget.setPos(leftPos + 15, pocketDisplayMode == PocketDisplayMode.CREATE_PATTERN ? pocketWidget.offY + pocketWidget.height + 4 : -0xFFFFFF);
	}

	private void reloadPosition(int mx, int my) {
		reloadPosition();
		DeepPocketClientApi api = DeepPocketClientApi.get();

		{//Update jei search
			String jeiSearch = DeepPocketJEI.getSearch();
			if (api.getSearchMode().syncFrom && jeiSearch != null && !jeiSearch.equals(lastJeiSearch))
				searchWidget.setValue(jeiSearch);
			lastJeiSearch = jeiSearch;
		}

//		this.hoverSearch = isHoverSearch(mx, my);
		this.hoverSlotIndex = getHoverSlotIndex(mx, my);
		this.hoverButton = getHoverButton(mx, my);
		
		menu.setHoverSlotIndex(hoverSlotIndex, my - topPos);
	}


	private boolean isInSlot(int mx, int my) {
		return (mx & 0xF) == mx && (my & 0xF) == my;
	}

	private int getHoverSlotIndex(int mx, int my) {
		mx -= leftPos + 19;
		my -= topPos + 19;
		my -= rowCount * 16 + 4;//pocket size + gap
		//Check: Crafting
		if (pocketDisplayMode == PocketDisplayMode.CRAFTING) {
			for (int gridY = 0; gridY < 3; gridY++)
				for (int gridX = 0; gridX < 3; gridX++)
					if (isInSlot(mx - 18 - 16 * gridX, my - 16 * gridY))
						return 36 + gridX + gridY * 3;
			if (102 <= mx && mx <= 125 && 12 <= my && my <= 35)
				return 45;
			my -= 52;//crafting size + gap
		}
		if (pocketDisplayMode == PocketDisplayMode.CREATE_PATTERN) {
			my -= 52;//patterns size + gap
		}
		//Check: Inventory
		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 9; x++)
				if (isInSlot(mx - x * 16, my - y * 16))
					return x + y * 9;
		my -= 52;//inventory size + gap
		//Check: Hotbar
		for (int x = 0; x < 9; x++)
			if (isInSlot(mx - x * 16, my))
				return 27 + x;
		//Don't hover a slot
		return -1;
	}

	private int getHoverButton(int mx, int my) {
		mx -= leftPos;
		my -= topPos;
		if (5 <= mx && mx <= 14) {
			if (5 <= my && my <= 14) return 0;
			if (19 <= my && my <= 28) return 1;
			if (29 <= my && my <= 38) return 2;
			if (39 <= my && my <= 48) return 3;
			if (49 <= my && my <= 58) return 4;
		}
		my -= 23 + rowCount * 16;
		if (pocketDisplayMode == PocketDisplayMode.CRAFTING) {
			if (19 <= mx && mx <= 34) {
				if (0 <= my && my <= 15) return 5;
				if (32 <= my && my <= 47) return 6;
			}
			if (isInSlot(mx - 147, my - 16)) return 7;
		}
		return -1;
	}


	private int renderAndMove(PoseStack poseStack, Sprites sprite, int x, int y, int height) {
		sprite.blit(poseStack, x, y, sprite.w, height);
		return y + height;
	}

	private int renderAndMove(PoseStack poseStack, Sprites sprite, int x, int y) {
		return renderAndMove(poseStack, sprite, x, y, sprite.h);
	}

	private void renderOutline(PoseStack poseStack) {
		int y = topPos;
		y = renderAndMove(poseStack, Sprites.OUTLINE_0, leftPos, y, 1);
		y = renderAndMove(poseStack, Sprites.OUTLINE_1, leftPos, y, 13);
		y = renderAndMove(poseStack, Sprites.OUTLINE_2, leftPos, y, 1);
		y = renderAndMove(poseStack, Sprites.OUTLINE_3, leftPos, y, 48);
		y = renderAndMove(poseStack, Sprites.OUTLINE_4, leftPos, y, 1);
		y = renderAndMove(poseStack, Sprites.OUTLINE_5, leftPos, y, 16 * rowCount - 41);
		y = renderAndMove(poseStack, Sprites.OUTLINE_6, leftPos, y, 1);
		y = renderAndMove(poseStack, Sprites.OUTLINE_7, leftPos, y, pocketDisplayMode == PocketDisplayMode.NORMAL ? 71 : 123);
		renderAndMove(poseStack, Sprites.OUTLINE_8, leftPos, y, 1);
	}

	private void renderFrame(PoseStack poseStack) {
		Sprites.BUTTONS_FRAME.blit(poseStack, leftPos + 1, topPos + 1);

		int x = leftPos + 15;
		int y = topPos + 1;
		y = renderAndMove(poseStack, Sprites.TOP, x, y);
		y = renderAndMove(poseStack, Sprites.GAP_SCROLL, x, y);
		y += 16 * rowCount; // container
		y = renderAndMove(poseStack, Sprites.GAP_SCROLL, x, y);
		if (pocketDisplayMode == PocketDisplayMode.CRAFTING) {
			y = renderAndMove(poseStack, Sprites.CRAFTING_FRAME, x, y);
			y = renderAndMove(poseStack, Sprites.GAP_SMALL, x, y);
		}
		if (pocketDisplayMode == PocketDisplayMode.CREATE_PATTERN) {
			y += Sprites.CREATE_PATTERN_FRAME.h;
			y = renderAndMove(poseStack, Sprites.GAP_SMALL, x, y);
		}
		y = renderAndMove(poseStack, Sprites.ROW_SMALL, x, y, 48);
		y = renderAndMove(poseStack, Sprites.GAP_SMALL, x, y);
		y = renderAndMove(poseStack, Sprites.ROW_SMALL, x, y, 16);
		renderAndMove(poseStack, Sprites.GAP_SMALL, x, y);
	}

	private void renderSlotBase(PoseStack poseStack, int x, int y, int slotIndex) {
		boolean isHover = slotIndex == hoverSlotIndex;
		isHover = isHover || isQuickCrafting && 0 <= slotIndex && slotIndex < menu.slots.size() && quickCraftSlots.contains(menu.getSlot(slotIndex));
		(isHover ? Sprites.SLOT_BASE_H : Sprites.SLOT_BASE_N).blit(poseStack, x, y);
	}

	private int renderSlotBaseContainer(PoseStack poseStack, int x, int y, int height, int slotIndex) {
		for (int row = 0; row < height; row++)
			for (int col = 0; col < 9; col++)
				renderSlotBase(poseStack, x + 16 * col, y + 16 * row, slotIndex++);
		return y + height * 16 + 4;
	}

	private int renderCraftingSlotBases(PoseStack poseStack, int x, int y) {
		int slotIndex = 36;
		for (int gridY = 0; gridY < 3; gridY++)
			for (int gridX = 0; gridX < 3; gridX++)
				renderSlotBase(poseStack, x + 18 + 16 * gridX, y + 16 * gridY, slotIndex++);
		//Rendering the crafting output
		boolean isHover = 45 == hoverSlotIndex;
		(isHover ? Sprites.SLOT_BASE_CRAFTING_OUTPUT_H : Sprites.SLOT_BASE_CRAFTING_OUTPUT_N).blit(poseStack, x + 102, y + 12);
		return y + 52;
	}

	private void renderAllSlotBases(PoseStack poseStack) {
		int x = leftPos + 19;
		int y = topPos + 19;
		y += rowCount * 16 + 4; // Container
		if (pocketDisplayMode == PocketDisplayMode.CRAFTING)
			y = renderCraftingSlotBases(poseStack, x, y);
		if (pocketDisplayMode == PocketDisplayMode.CREATE_PATTERN)
			y += 52;
		y = renderSlotBaseContainer(poseStack, x, y, 3, 0);
		renderSlotBaseContainer(poseStack, x, y, 1, 27);
	}

	private void renderHoverAbles(PoseStack poseStack) {
		{ //Render: Buttons
			getSettingsButton(hoverButton == 0).blit(poseStack, leftPos + 5, topPos + 5);
			getSearchModeButton(hoverButton == 1).blit(poseStack, leftPos + 5, topPos + 19);
			getSortingOrderButton(hoverButton == 2).blit(poseStack, leftPos + 5, topPos + 29);
			getSortAscendingButton(hoverButton == 3).blit(poseStack, leftPos + 5, topPos + 39);
			getDisplayAscendingButton(hoverButton == 4).blit(poseStack, leftPos + 5, topPos + 49);
			if (pocketDisplayMode == PocketDisplayMode.CRAFTING) {
				getClearUButton(hoverButton == 5).blit(poseStack, leftPos + 19, topPos + 23 + 16 * rowCount);
				getClearDButton(hoverButton == 6).blit(poseStack, leftPos + 19, topPos + 55 + 16 * rowCount);
				getBulkCraftButton(menu.getSlot(45).hasItem(), hoverButton == 7).blit(poseStack, leftPos + 147, topPos + 39 + 16 * rowCount);
			}
		}
		renderAllSlotBases(poseStack);
	}

	private void renderSlotItemSingle(PoseStack poseStack, int x, int y, int slotIndex) {
		Slot slot = menu.getSlot(slotIndex);
		ItemStack slotItem = slot.getItem();

		ItemStack carried = this.menu.getCarried();
		if (this.isQuickCrafting && this.quickCraftSlots.contains(slot) && !carried.isEmpty() &&
						this.quickCraftSlots.size() != 1 && AbstractContainerMenu.canItemQuickReplace(slot, carried, true) && this.menu.canDragTo(slot)) {
			int count = slotItem.isEmpty() ? 0 : slotItem.getCount();
			slotItem = carried.copy();
			AbstractContainerMenu.getQuickCraftSlotCount(this.quickCraftSlots, super.quickCraftingType, slotItem, count);
			int maxStackSize = Math.min(slotItem.getMaxStackSize(), slot.getMaxStackSize(slotItem));
			if (slotItem.getCount() > maxStackSize)
				slotItem.setCount(maxStackSize);
		}

		dpClientHelper.renderItem(poseStack, x, y, slotItem, itemRenderer, font);
	}

	private void renderSlotItemsRow(PoseStack poseStack, int x, int y, int slotIndex) {
		for (int i = 0; i < 9; i++)
			renderSlotItemSingle(poseStack, x + 16 * i, y, slotIndex + i);
	}

	private void renderSlotItems(PoseStack poseStack) {
		int xOffset = leftPos + 19;
		int yOffset = topPos + 19;
		yOffset += 16 * rowCount + 4;
		if (pocketDisplayMode == PocketDisplayMode.CRAFTING) { //Render: Crafting Slots
			int slotIndex = 36;
			for (int gridY = 0; gridY < 3; gridY++)
				for (int gridX = 0; gridX < 3; gridX++)
					renderSlotItemSingle(poseStack, xOffset + 18 + 16 * gridX, yOffset + 16 * gridY, slotIndex++);
			renderSlotItemSingle(poseStack, xOffset + 106, yOffset + 16, 45);
			yOffset += 52;
		}
		if (pocketDisplayMode == PocketDisplayMode.CREATE_PATTERN) { //Render: Crafting Slots
			yOffset += 52;
		}
		{ //Render: Inventory Slots
			renderSlotItemsRow(poseStack, xOffset, yOffset, 0);
			renderSlotItemsRow(poseStack, xOffset, yOffset + 16, 9);
			renderSlotItemsRow(poseStack, xOffset, yOffset + 32, 18);
			renderSlotItemsRow(poseStack, xOffset, yOffset + 52, 27);//hotbar
		}
	}


	@Override
	protected void renderLabels(PoseStack poseStack, int mx, int my) {
		Pocket pocket = menu.getPocket();
		if (pocket != null) {
			String pocketName = pocket.getName();
			String displayPocketName = font.width(pocketName) < 54 ? pocketName : font.plainSubstrByWidth("..." + pocketName, 54).substring(3) + "...";
			font.draw(poseStack, displayPocketName, 20, 6, 0xFFFFFF);
		}
	}

	@Override
	protected void renderTooltip(PoseStack poseStack, int x, int y) {
		if (!menu.getCarried().isEmpty())
			return;
		if (hoverButton >= 0)
			this.renderButtonTooltip(poseStack, x, y);
		else if (hoverSlotIndex <= 45)
			super.renderTooltip(poseStack, x, y);
		
		for (var child : children())
			if (child instanceof WidgetWithTooltip tooltip)
				tooltip.renderTooltip(this, poseStack, x, y);
	}

	private void renderButtonTooltip(PoseStack poseStack, int x, int y) {
		Component text = switch (hoverButton) {
			case 0 -> Component.literal("Settings");
			case 1 -> Component.literal("Search Mode: ").append(Component.literal(DeepPocketClientApi.get().getSearchMode().displayName).withStyle(ChatFormatting.AQUA));
			case 2 -> Component.literal("Sort: ").append(Component.literal(DeepPocketClientApi.get().getSortingOrder().displayName).withStyle(ChatFormatting.AQUA));
			case 3 -> Component.literal("Sort Direction: ").append(Component.literal(DeepPocketClientApi.get().isSortAscending() ? "Ascending" : "Descending").withStyle(ChatFormatting.AQUA));
			case 4 -> Component.literal("Display Mode: ").append(Component.literal(DeepPocketClientApi.get().getPocketDisplayMode().displayName).withStyle(ChatFormatting.AQUA));
			case 5 -> Component.literal("Clear To Pocket");
			case 6 -> Component.literal("Clear To Inventory");
			case 7 -> Component.literal("Bulk Crafting");
			default -> null;
		};
		if (text == null)
			return;
		super.renderTooltip(poseStack, text, x, y);
	}

	@Override
	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		reloadPosition(mx, my);
		menu.clearHoverSlotIndex();
		
		renderBackground(poseStack);
		super.render(poseStack, mx, my, partialTick);
		renderSlotItems(poseStack);
		renderTooltip(poseStack, mx, my);
		
		menu.setHoverSlotIndex(hoverSlotIndex, my - topPos);
		
		ItemStack hoveredItem = pocketWidget.getHoveredItem();
		if (!hoveredItem.isEmpty()) {
			hoveredSlot = new FakeConstantSlot(hoveredItem, mx - 8, my - 8);
		}
	}

	@Override
	protected void containerTick() {
		Pocket pocket = menu.getPocket();
		if (pocket != null && DeepPocketClientApi.get().getPocket(pocket.getPocketId()) == null)
			onClose();
	}

	@Override
	protected void renderBg(PoseStack poseStack, float partialTick, int mx, int my) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);

		Pocket pocket = menu.getPocket();
		DeepPocketUtils.setRenderShaderColor(pocket == null ? 0xFFFFFF : pocket.getColor());
		renderOutline(poseStack);
		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		renderFrame(poseStack);
		renderHoverAbles(poseStack);
	}

	private boolean handleButtonClick() {
		switch (hoverButton) {
			case 0 -> {
				Pocket pocket = menu.getPocket();
				if (pocket != null && pocket.getOwner().equals(menu.playerInventory.player.getUUID()))
					ClientScreens.settingsEdit(pocket);
			}
			case 1 -> toggleSearchMode();
			case 2 -> toggleSortingOrder();
			case 3 -> toggleSortAscending();
			case 4 -> toggleDisplayCrafting();
			case 5 -> DeepPocketPacketHandler.sbClearCraftingGrid(true);
			case 6 -> DeepPocketPacketHandler.sbClearCraftingGrid(false);
			case 7 -> {
				Pocket pocket = menu.getPocket();
				if (pocket != null && menu.getSlot(45).hasItem())
					ClientScreens.bulkCrafting(pocket, menu.getCrafting());
			}
			default -> { return false; }
		}
		DeepPocketUtils.playClickSound();
		return true;
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		reloadPosition((int)mx, (int)my);
		
		
		//Buttons
		if (button == InputConstants.MOUSE_BUTTON_LEFT) {
			if (handleButtonClick())
				return true;
		}
		return super.mouseClicked(mx, my, button);
	}

	@Override
	public void mouseMoved(double mx, double my) {
		pocketWidget.mouseMoved(mx, my);
	}

	@Override
	public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
		pocketWidget.mouseReleased(pMouseX, pMouseY, pButton);
		return super.mouseReleased(pMouseX, pMouseY, pButton);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double delta) {
		return pocketWidget.mouseScrolled(mx, my, delta);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (getFocused() == searchWidget) {
			searchWidget.keyPressed(keyCode, scanCode, modifiers);
			if (keyCode == InputConstants.KEY_ESCAPE)
				this.onClose();
			return true;
		}
		super.keyPressed(keyCode, scanCode, modifiers);
		return true;
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		return super.charTyped(codePoint, modifiers);
	}

	private static Sprites getSearchModeButton(boolean hover) {
		return switch (DeepPocketClientApi.get().getSearchMode()) {
			case NORMAL -> hover ? Sprites.SEARCH_MODE_0H : Sprites.SEARCH_MODE_0N;
			case SYNC_JEI -> hover ? Sprites.SEARCH_MODE_1H : Sprites.SEARCH_MODE_1N;
			case SYNC_FROM_JEI -> hover ? Sprites.SEARCH_MODE_2H : Sprites.SEARCH_MODE_2N;
			case SYNC_TO_JEI -> hover ? Sprites.SEARCH_MODE_3H : Sprites.SEARCH_MODE_3N;
		};
	}

	private static Sprites getSortingOrderButton(boolean hover) {
		return switch (DeepPocketClientApi.get().getSortingOrder()) {
			case COUNT -> hover ? Sprites.SORTING_ORDER_0H : Sprites.SORTING_ORDER_0N;
			case ID -> hover ? Sprites.SORTING_ORDER_1H : Sprites.SORTING_ORDER_1N;
			case NAME -> hover ? Sprites.SORTING_ORDER_2H : Sprites.SORTING_ORDER_2N;
			case MOD -> hover ? Sprites.SORTING_ORDER_3H : Sprites.SORTING_ORDER_3N;
		};
	}

	private static Sprites getSortAscendingButton(boolean hover) {
		return DeepPocketClientApi.get().isSortAscending() ?
						hover ? Sprites.SORT_ASCENDING_1H : Sprites.SORT_ASCENDING_1N :
						hover ? Sprites.SORT_ASCENDING_0H : Sprites.SORT_ASCENDING_0N;
	}

	private static Sprites getDisplayAscendingButton(boolean hover) {
		return switch (DeepPocketClientApi.get().getPocketDisplayMode()) {
			case NORMAL -> hover ? Sprites.DISPLAY_CRAFTING_0H : Sprites.DISPLAY_CRAFTING_0N;
			case CRAFTING -> hover ? Sprites.DISPLAY_CRAFTING_1H : Sprites.DISPLAY_CRAFTING_1N;
			case CREATE_PATTERN -> hover ? Sprites.DISPLAY_CRAFTING_2H : Sprites.DISPLAY_CRAFTING_2N;
		};
	}

	private static Sprites getSettingsButton(boolean hover) {
		return hover ? Sprites.SETTINGS_H : Sprites.SETTINGS_N;
	}

	private static Sprites getClearUButton(boolean hover) {
		return hover ? Sprites.CLEAR_UH : Sprites.CLEAR_UN;
	}

	private static Sprites getClearDButton(boolean hover) {
		return hover ? Sprites.CLEAR_DH : Sprites.CLEAR_DN;
	}

	private static Sprites getBulkCraftButton(boolean active, boolean hover) {
		return active ? hover ? Sprites.BULK_CRAFTING_H : Sprites.BULK_CRAFTING_N : Sprites.BULK_CRAFTING_D;
	}

	private static void toggleSearchMode() {
		DeepPocketClientApi.get().setSearchMode(switch (DeepPocketClientApi.get().getSearchMode()) {
			case NORMAL -> SearchMode.SYNC_JEI;
			case SYNC_JEI -> SearchMode.SYNC_FROM_JEI;
			case SYNC_FROM_JEI -> SearchMode.SYNC_TO_JEI;
			case SYNC_TO_JEI -> SearchMode.NORMAL;
		});
	}

	private static void toggleSortingOrder() {
		DeepPocketClientApi.get().setSortingOrder(switch (DeepPocketClientApi.get().getSortingOrder()) {
			case COUNT -> SortingOrder.ID;
			case ID -> SortingOrder.NAME;
			case NAME -> SortingOrder.MOD;
			case MOD -> SortingOrder.COUNT;
		});
	}

	private static void toggleSortAscending() {
		DeepPocketClientApi.get().setSortAscending(!DeepPocketClientApi.get().isSortAscending());
	}

	private static void toggleDisplayCrafting() {
		DeepPocketClientApi.get().setPocketDisplayMode(switch (DeepPocketClientApi.get().getPocketDisplayMode()) {
			case NORMAL -> PocketDisplayMode.CRAFTING;
			case CRAFTING -> PocketDisplayMode.CREATE_PATTERN;
			case CREATE_PATTERN -> PocketDisplayMode.NORMAL;
		});
	}

	public int getJEITargetCount() {
		return pocketDisplayMode == PocketDisplayMode.CREATE_PATTERN ? 18 : 0;
	}

	public Rect2i getJEITargetArea(int targetIndex) {
		return patternWidget.getJEITargetArea(targetIndex);
	}

	public void acceptJEIGhostIngredient(int targetIndex, ItemStack ghostIngredient) {
		patternWidget.acceptJEIGhostIngredient(targetIndex, ghostIngredient);
	}


	private enum Sprites {
		//Outline
		OUTLINE_0(0,34,180,1),
		OUTLINE_1(0,35,180,1),
		OUTLINE_2(0,36,180,1),
		OUTLINE_3(0,37,180,1),
		OUTLINE_4(0,38,180,1),
		OUTLINE_5(0,39,180,1),
		OUTLINE_6(0,40,180,1),
		OUTLINE_7(0,41,180,1),
		OUTLINE_8(0,42,180,1),
		//Frame
		TOP(0,0,152,14),
		GAP_SMALL(0,14,152,4),
		ROW_SMALL(0,18,152,16),
		GAP_SCROLL(0,14,164,4),
		ROW_SCROLL(0,18,164,16),
		BUTTONS_FRAME(202,0,14,62),
		CRAFTING_FRAME(0,43,152,48),
		CREATE_PATTERN_FRAME(0,91,152,48),
		//Slot Base
		SLOT_BASE_N(186, 0, 16, 16),
		SLOT_BASE_H(186, 16, 16, 16),
		SLOT_BASE_CRAFTING_OUTPUT_N(152, 43, 24, 24),
		SLOT_BASE_CRAFTING_OUTPUT_H(152, 67, 24, 24),
		//Scroll
		SCROLL_N(152,10,8,2),
		SCROLL_H(152,12,8,2),
		//Buttons
		SEARCH_MODE_0N(216, 0, 10, 10),SEARCH_MODE_0H(216, 30, 10, 10),
		SEARCH_MODE_1N(226, 0, 10, 10),SEARCH_MODE_1H(226, 30, 10, 10),
		SEARCH_MODE_2N(236, 0, 10, 10),SEARCH_MODE_2H(236, 30, 10, 10),
		SEARCH_MODE_3N(246, 0, 10, 10),SEARCH_MODE_3H(246, 30, 10, 10),
		SORTING_ORDER_0N(216, 10, 10, 10),SORTING_ORDER_0H(216, 40, 10, 10),
		SORTING_ORDER_1N(226, 10, 10, 10),SORTING_ORDER_1H(226, 40, 10, 10),
		SORTING_ORDER_2N(236, 10, 10, 10),SORTING_ORDER_2H(236, 40, 10, 10),
		SORTING_ORDER_3N(246, 10, 10, 10),SORTING_ORDER_3H(246, 40, 10, 10),
		SORT_ASCENDING_0N(216, 20, 10, 10),SORT_ASCENDING_0H(216, 50, 10, 10),
		SORT_ASCENDING_1N(226, 20, 10, 10),SORT_ASCENDING_1H(226, 50, 10, 10),
		DISPLAY_CRAFTING_0N(236, 20, 10, 10),DISPLAY_CRAFTING_0H(236, 50, 10, 10),
		DISPLAY_CRAFTING_1N(246, 20, 10, 10),DISPLAY_CRAFTING_1H(246, 50, 10, 10),
		DISPLAY_CRAFTING_2N(236, 60, 10, 10),DISPLAY_CRAFTING_2H(246, 60, 10, 10),
		SETTINGS_N(216, 60, 10, 10),SETTINGS_H(226, 60, 10, 10),
		CLEAR_UN(224, 70, 16, 16),CLEAR_UH(240, 70, 16, 16),
		CLEAR_DN(224, 86, 16, 16),CLEAR_DH(240, 86, 16, 16),
		BULK_CRAFTING_N(224, 102, 16, 16),BULK_CRAFTING_H(240, 102, 16, 16),BULK_CRAFTING_D(208, 102, 16, 16),
		CLEAR_PATTERN_N(224, 118, 16, 16),CLEAR_PATTERN_H(240, 118, 16, 16),
		;

		private final int u, v, w, h;

		Sprites(int u, int v, int w, int h) {
			this.u = u;
			this.v = v;
			this.w = w;
			this.h = h;
		}

		private void blit(PoseStack stack, int x, int y) {
			Screen.blit(stack, x, y, u, v, w, h, 256, 256);
		}

		private void blit(PoseStack stack, int x, int y, int displayW, int displayH) {
			Screen.blit(stack, x, y, displayW, displayH, u, v, w, h, 256, 256);
		}
	}
}
