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
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import com.ofek2608.deep_pocket.client.client_screens.ClientScreens;
import com.ofek2608.deep_pocket.client.widget.*;
import com.ofek2608.deep_pocket.integration.DeepPocketJEI;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import com.ofek2608.deep_pocket.registry.items.crafting_pattern.CraftingPatternItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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
import java.util.Arrays;
import java.util.function.LongConsumer;
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
	//focus fields
	private final ItemTypeAmount[] patternInput = new ItemTypeAmount[9];
	private final ItemTypeAmount[] patternOutput = new ItemTypeAmount[9];
	
	private final PocketWidget pocketWidget;
	private final DPTextWidget searchWidget;
	private final PocketTabWidget pocketTabWidget;
	private final PatternWidget patternWidget;
	
	public PocketScreen(PocketMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
		clearPattern();
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
	
	private Predicate<ItemType> createFilter() {
		Predicate<ItemStack> searchFilter = DeepPocketUtils.createFilter(searchWidget.getValue());
		return type->searchFilter.test(type.create());
	}
	
	private void clearPattern() {
		Arrays.fill(patternInput, new ItemTypeAmount(ItemType.EMPTY, 0));
		Arrays.fill(patternOutput, new ItemTypeAmount(ItemType.EMPTY, 0));
	}

	public void setPattern(ItemType[] input, ItemStack output) {
		patternWidget.setPattern(input, output);
		clearPattern();
		int inputLen = Math.min(input.length, 9);
		for (int i = 0; i < inputLen; i++)
			patternInput[i] = new ItemTypeAmount(input[i], 1);
		patternOutput[0] = new ItemTypeAmount(new ItemType(output), output.getCount());
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
			for (int gridY = 0; gridY < 3; gridY++)
				for (int gridX = 0; gridX < 3; gridX++)
					if (isInSlot(mx - 18 - 16 * gridX, my - 16 * gridY))
						return 46 + gridX + gridY * 3;
			for (int gridY = 0; gridY < 3; gridY++)
				for (int gridX = 0; gridX < 3; gridX++)
					if (isInSlot(mx - 78 - 16 * gridX, my - 16 * gridY))
						return 55 + gridX + gridY * 3;
			if (isInSlot(mx - 128, my - 16))
				return 64;
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
		if (pocketDisplayMode == PocketDisplayMode.CREATE_PATTERN) {
			if (isInSlot(mx - 19, my - 16)) return 8;
		}
		return -1;
	}


	private int renderAndMove(PoseStack stack, Sprites sprite, int x, int y, int height) {
		sprite.blit(stack, x, y, sprite.w, height);
		return y + height;
	}

	private int renderAndMove(PoseStack stack, Sprites sprite, int x, int y) {
		return renderAndMove(stack, sprite, x, y, sprite.h);
	}

	private void renderOutline(PoseStack stack) {
		int y = topPos;
		y = renderAndMove(stack, Sprites.OUTLINE_0, leftPos, y, 1);
		y = renderAndMove(stack, Sprites.OUTLINE_1, leftPos, y, 13);
		y = renderAndMove(stack, Sprites.OUTLINE_2, leftPos, y, 1);
		y = renderAndMove(stack, Sprites.OUTLINE_3, leftPos, y, 48);
		y = renderAndMove(stack, Sprites.OUTLINE_4, leftPos, y, 1);
		y = renderAndMove(stack, Sprites.OUTLINE_5, leftPos, y, 16 * rowCount - 41);
		y = renderAndMove(stack, Sprites.OUTLINE_6, leftPos, y, 1);
		y = renderAndMove(stack, Sprites.OUTLINE_7, leftPos, y, pocketDisplayMode == PocketDisplayMode.NORMAL ? 71 : 123);
		renderAndMove(stack, Sprites.OUTLINE_8, leftPos, y, 1);
	}

	private void renderFrame(PoseStack stack) {
		Sprites.BUTTONS_FRAME.blit(stack, leftPos + 1, topPos + 1);

		int x = leftPos + 15;
		int y = topPos + 1;
		y = renderAndMove(stack, Sprites.TOP, x, y);
		y = renderAndMove(stack, Sprites.GAP_SCROLL, x, y);
		y += 16 * rowCount; // container
		y = renderAndMove(stack, Sprites.GAP_SCROLL, x, y);
		if (pocketDisplayMode == PocketDisplayMode.CRAFTING) {
			y = renderAndMove(stack, Sprites.CRAFTING_FRAME, x, y);
			y = renderAndMove(stack, Sprites.GAP_SMALL, x, y);
		}
		if (pocketDisplayMode == PocketDisplayMode.CREATE_PATTERN) {
			y = renderAndMove(stack, Sprites.CREATE_PATTERN_FRAME, x, y);
			y = renderAndMove(stack, Sprites.GAP_SMALL, x, y);
		}
		y = renderAndMove(stack, Sprites.ROW_SMALL, x, y, 48);
		y = renderAndMove(stack, Sprites.GAP_SMALL, x, y);
		y = renderAndMove(stack, Sprites.ROW_SMALL, x, y, 16);
		renderAndMove(stack, Sprites.GAP_SMALL, x, y);
	}

	private void renderSlotBase(PoseStack stack, int x, int y, int slotIndex) {
		boolean isHover = slotIndex == hoverSlotIndex;
		isHover = isHover || isQuickCrafting && 0 <= slotIndex && slotIndex < menu.slots.size() && quickCraftSlots.contains(menu.getSlot(slotIndex));
		(isHover ? Sprites.SLOT_BASE_H : Sprites.SLOT_BASE_N).blit(stack, x, y);
	}

	private int renderSlotBaseContainer(PoseStack stack, int x, int y, int height, int slotIndex) {
		for (int row = 0; row < height; row++)
			for (int col = 0; col < 9; col++)
				renderSlotBase(stack, x + 16 * col, y + 16 * row, slotIndex++);
		return y + height * 16 + 4;
	}

	private int renderCraftingSlotBases(PoseStack stack, int x, int y) {
		int slotIndex = 36;
		for (int gridY = 0; gridY < 3; gridY++)
			for (int gridX = 0; gridX < 3; gridX++)
				renderSlotBase(stack, x + 18 + 16 * gridX, y + 16 * gridY, slotIndex++);
		//Rendering the crafting output
		boolean isHover = 45 == hoverSlotIndex;
		(isHover ? Sprites.SLOT_BASE_CRAFTING_OUTPUT_H : Sprites.SLOT_BASE_CRAFTING_OUTPUT_N).blit(stack, x + 102, y + 12);
		return y + 52;
	}

	private int renderCreatePatternSlotBases(PoseStack stack, int x, int y) {
		int slotIndex = 46;
		for (int gridY = 0; gridY < 3; gridY++)
			for (int gridX = 0; gridX < 3; gridX++)
				renderSlotBase(stack, x + 18 + 16 * gridX, y + 16 * gridY, slotIndex++);
		for (int gridY = 0; gridY < 3; gridY++)
			for (int gridX = 0; gridX < 3; gridX++)
				renderSlotBase(stack, x + 78 + 16 * gridX, y + 16 * gridY, slotIndex++);
		renderSlotBase(stack, x + 128, y + 16, 64);
		return y + 52;
	}

	private void renderAllSlotBases(PoseStack stack) {
		int x = leftPos + 19;
		int y = topPos + 19;
		y += rowCount * 16 + 4; // Container
		if (pocketDisplayMode == PocketDisplayMode.CRAFTING)
			y = renderCraftingSlotBases(stack, x, y);
		if (pocketDisplayMode == PocketDisplayMode.CREATE_PATTERN)
			y = renderCreatePatternSlotBases(stack, x, y);
		y = renderSlotBaseContainer(stack, x, y, 3, 0);
		renderSlotBaseContainer(stack, x, y, 1, 27);
	}

	private void renderHoverAbles(PoseStack stack) {
		{ //Render: Buttons
			getSettingsButton(hoverButton == 0).blit(stack, leftPos + 5, topPos + 5);
			getSearchModeButton(hoverButton == 1).blit(stack, leftPos + 5, topPos + 19);
			getSortingOrderButton(hoverButton == 2).blit(stack, leftPos + 5, topPos + 29);
			getSortAscendingButton(hoverButton == 3).blit(stack, leftPos + 5, topPos + 39);
			getDisplayAscendingButton(hoverButton == 4).blit(stack, leftPos + 5, topPos + 49);
			if (pocketDisplayMode == PocketDisplayMode.CRAFTING) {
				getClearUButton(hoverButton == 5).blit(stack, leftPos + 19, topPos + 23 + 16 * rowCount);
				getClearDButton(hoverButton == 6).blit(stack, leftPos + 19, topPos + 55 + 16 * rowCount);
				getBulkCraftButton(menu.getSlot(45).hasItem(), hoverButton == 7).blit(stack, leftPos + 147, topPos + 39 + 16 * rowCount);
			}
			if (pocketDisplayMode == PocketDisplayMode.CREATE_PATTERN) {
				getClearPatternButton(hoverButton == 8).blit(stack, leftPos + 19, topPos + 39 + 16 * rowCount);
			}
		}
		renderAllSlotBases(stack);
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
			int slotIndex;
			slotIndex = 0;
			for (int gridY = 0; gridY < 3; gridY++)
				for (int gridX = 0; gridX < 3; gridX++)
					dpClientHelper.renderItemAmount(poseStack, xOffset + 18 + 16 * gridX, yOffset + 16 * gridY, patternInput[slotIndex++], itemRenderer, font);
			slotIndex = 0;
			for (int gridY = 0; gridY < 3; gridY++)
				for (int gridX = 0; gridX < 3; gridX++)
					dpClientHelper.renderItemAmount(poseStack, xOffset + 78 + 16 * gridX, yOffset + 16 * gridY, patternOutput[slotIndex++], itemRenderer, font);
			if (!isEmptyPattern()) {
				dpClientHelper.renderItem(poseStack, xOffset + 128, yOffset + 16,
								canCreatePattern() ?
												CraftingPatternItem.createItem(patternInput, patternOutput) :
												new ItemStack(DeepPocketRegistry.EMPTY_CRAFTING_PATTERN_ITEM.get()),
								itemRenderer, font
				);
			}
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
	protected void renderLabels(PoseStack stack, int mx, int my) {
		Pocket pocket = menu.getPocket();
		if (pocket != null) {
			String pocketName = pocket.getName();
			String displayPocketName = font.width(pocketName) < 54 ? pocketName : font.plainSubstrByWidth("..." + pocketName, 54).substring(3) + "...";
			font.draw(stack, displayPocketName, 20, 6, 0xFFFFFF);
		}
	}

	@Override
	protected void renderTooltip(PoseStack stack, int x, int y) {
		if (!menu.getCarried().isEmpty())
			return;
		if (hoverButton >= 0)
			this.renderButtonTooltip(stack, x, y);
		else if (hoverSlotIndex <= 45)
			super.renderTooltip(stack, x, y);
		else if (hoverSlotIndex <= 64)
			this.renderCreatePatternTooltip(stack, x, y);
		
		for (var child : children())
			if (child instanceof WidgetWithTooltip tooltip)
				tooltip.renderTooltip(this, stack, x, y);
	}

	private void renderButtonTooltip(PoseStack stack, int x, int y) {
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
		super.renderTooltip(stack, text, x, y);
	}

	private boolean isEmptyPattern() {
		for (ItemTypeAmount output : patternOutput)
			if (!output.isEmpty())
				return false;
		return true;
	}

	private boolean canCreatePattern() {
		Pocket pocket = menu.getPocket();
		return pocket != null && pocket.getMaxExtract(DeepPocketClientApi.get().getKnowledge(), new ItemType(DeepPocketRegistry.EMPTY_CRAFTING_PATTERN_ITEM.get())) != 0;
	}

	private void renderCreatePatternTooltip(PoseStack stack, int x, int y) {
		int index = hoverSlotIndex - 46;
		if (index < 0 || 19 <= index)
			return;
		ItemStack item;
		if (index < 9)
			item = patternInput[index].getItemType().create();
		else if (index < 18)
			item = patternOutput[index - 9].getItemType().create();
		else if (isEmptyPattern())
			return;
		else if (canCreatePattern())
			item = CraftingPatternItem.createItem(patternInput, patternOutput);
		else {
			super.renderTooltip(stack, Component.literal("Missing Empty Crafting Patterns").withStyle(ChatFormatting.RED), x, y);
			return;
		}

		if (!item.isEmpty())
			super.renderTooltip(stack, item, x, y);
	}

	@Override
	public void render(PoseStack stack, int mx, int my, float partialTick) {
		reloadPosition(mx, my);
		menu.clearHoverSlotIndex();
		
		renderBackground(stack);
		super.render(stack, mx, my, partialTick);
		renderSlotItems(stack);
		renderTooltip(stack, mx, my);
		
		menu.setHoverSlotIndex(hoverSlotIndex, my - topPos);
		
		if (pocketWidget.getLastHoveredType() != null) {
			hoveredSlot = new FakeConstantSlot(pocketWidget.getLastHoveredType().getItemType().create(), mx - 8, my - 8);
		}
	}

	@Override
	protected void containerTick() {
		Pocket pocket = menu.getPocket();
		if (pocket != null && DeepPocketClientApi.get().getPocket(pocket.getPocketId()) == null)
			onClose();
	}

	@Override
	protected void renderBg(PoseStack stack, float partialTick, int mx, int my) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);

		Pocket pocket = menu.getPocket();
		DeepPocketUtils.setRenderShaderColor(pocket == null ? 0xFFFFFF : pocket.getColor());
		renderOutline(stack);
		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		renderFrame(stack);
		renderHoverAbles(stack);
	}

	private void mouseClickedCreatePattern(int button) {
		if (button == InputConstants.MOUSE_BUTTON_LEFT)
			DeepPocketPacketHandler.sbPatternCreate(patternInput, patternOutput, !Screen.hasShiftDown());
	}

	private void mouseClickedPattern(int button) {
		int clickedSlot = hoverSlotIndex - 46;
		ItemStack carried = menu.getCarried();
		boolean control = Screen.hasControlDown();
		boolean shift = Screen.hasShiftDown();

		if (shift || !control) {
			if (shift)
				carried = ItemStack.EMPTY;
			long count = carried.isEmpty() ? 0 : button == InputConstants.MOUSE_BUTTON_LEFT ? carried.getCount() : 1;
			if (0 <= clickedSlot && clickedSlot < 9)
				patternInput[clickedSlot] = new ItemTypeAmount(new ItemType(carried), count);
			else if (9 <= clickedSlot && clickedSlot < 18)
				patternOutput[clickedSlot - 9] = new ItemTypeAmount(new ItemType(carried), count);
			return;
		}
		LongConsumer resultConsumer;
		long currentAmount;
		if (0 <= clickedSlot && clickedSlot < 9) {
			currentAmount = patternInput[clickedSlot].getAmount();
			resultConsumer = newAmount->patternInput[clickedSlot] = new ItemTypeAmount(patternInput[clickedSlot].getItemType(), newAmount);
		} else if (9 <= clickedSlot && clickedSlot < 18) {
			currentAmount = patternOutput[clickedSlot - 9].getAmount();
			resultConsumer = newAmount->patternOutput[clickedSlot - 9] = new ItemTypeAmount(patternOutput[clickedSlot - 9].getItemType(), newAmount);
		} else
			return;
		if (currentAmount == 0)
			return;
		Pocket pocket = menu.getPocket();
		ClientScreens.selectNumber(Component.literal("Select Amount"), pocket == null ? 0 : pocket.getColor(), currentAmount, newAmount->{
			resultConsumer.accept(newAmount);
			Minecraft.getInstance().setScreen(this);
		});
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
			case 8 -> clearPattern();
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
		if (hoverSlotIndex == 64) {
			mouseClickedCreatePattern(button);
			return true;
		}
		if (hoverSlotIndex >= 46) {
			mouseClickedPattern(button);
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

	private static Sprites getClearPatternButton(boolean hover) {
		return hover ? Sprites.CLEAR_PATTERN_H : Sprites.CLEAR_PATTERN_N;
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
//		int x = (targetIndex % 3) * 16 + (targetIndex < 9 ? 37 : 97) + leftPos;
//		int y = ((targetIndex / 3) % 3) * 16 + rowCount * 16 + 23 + topPos;
//		return new Rect2i(x, y, 16, 16);
	}

	public void acceptJEIGhostIngredient(int targetIndex, ItemStack ghostIngredient) {
		patternWidget.acceptJEIGhostIngredient(targetIndex, ghostIngredient);
//		if (0 <= targetIndex && targetIndex < 9) {
//			patternInput[targetIndex] = new ItemTypeAmount(new ItemType(ghostIngredient), ghostIngredient.getCount());
//		} else if (9 <= targetIndex && targetIndex < 18) {
//			patternOutput[targetIndex - 9] = new ItemTypeAmount(new ItemType(ghostIngredient), ghostIngredient.getCount());
//		}
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
