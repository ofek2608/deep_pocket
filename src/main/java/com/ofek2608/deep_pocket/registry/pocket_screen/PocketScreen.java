package com.ofek2608.deep_pocket.registry.pocket_screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.Pocket;
import com.ofek2608.deep_pocket.api.enums.SearchMode;
import com.ofek2608.deep_pocket.api.enums.SortingOrder;
import com.ofek2608.deep_pocket.client_screens.ClientScreens;
import com.ofek2608.deep_pocket.integration.DeepPocketJEI;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ContainerScreenEvent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class PocketScreen extends AbstractContainerScreen<PocketMenu> {
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/pocket.png");

	//render fields
	private boolean displayCrafting;
	private int rowCount;
	//render hover fields
	@Nullable private String lastJeiSearch = DeepPocketJEI.getSearch();
	private String search = DeepPocketClientApi.get().getSearchMode().syncFrom && lastJeiSearch != null ? lastJeiSearch : "";
	private boolean hoverScroll;
	private boolean hoverSearch;
	private int hoverSlot;
	private int hoverButton;
	private int maxScroll;
	private int scroll;
	private List<Map.Entry<ItemType,Long>> visiblePocketSlots = Collections.emptyList();
	//focus fields
	private boolean focusSearch;
	private boolean focusScroll;
	//simulate fields
	private int quickCraftingType;

	public PocketScreen(PocketMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}






	private void reloadPosition() {
		this.displayCrafting = DeepPocketClientApi.get().isDisplayCrafting();
		int imageHeightExcludingRows = displayCrafting ? 148 : 96;
		this.rowCount = Math.max(Math.min((height - imageHeightExcludingRows) / 16, 9), 3);
		this.imageWidth = 180;
		this.imageHeight = rowCount * 16 + imageHeightExcludingRows;
		this.leftPos = (this.width - 152) / 2 - 15;
		this.topPos = (this.height - this.imageHeight) / 2;
		menu.resetSlots(23 + rowCount * 16, displayCrafting);
	}

	private void reloadPosition(int mx, int my) {
		reloadPosition();
		DeepPocketClientApi api = DeepPocketClientApi.get();

		{//Update jei search
			String jeiSearch = DeepPocketJEI.getSearch();
			if (api.getSearchMode().syncFrom && jeiSearch != null && !jeiSearch.equals(lastJeiSearch))
				search = jeiSearch;
			lastJeiSearch = jeiSearch;
		}

		this.hoverScroll = isHoverScroll(mx, my);
		this.hoverSearch = isHoverSearch(mx, my);
		this.hoverSlot = getHoverSlot(mx, my);
		this.hoverButton = getHoverButton(mx, my);

		Predicate<ItemStack> searchFilter = DeepPocketUtils.createFilter(search);
		Pocket pocket = menu.getPocket();
		List<Map.Entry<ItemType,Long>> sortedKnowledge = pocket == null ?
						Collections.emptyList() :
						api.getSortedKnowledge(pocket).filter(entry->searchFilter.test(entry.getKey().create())).toList();
		maxScroll = Math.max((sortedKnowledge.size() - 1) / 9 + 1 - rowCount, 0);
		scroll = Math.max(Math.min(scroll, maxScroll), 0);
		visiblePocketSlots = sortedKnowledge.stream().skip(scroll * 9L).limit(rowCount * 9L).toList();
	}

	private boolean isHoverScroll(int mx, int my) {
		mx -= leftPos;
		my -= topPos;
		return 167 <= mx && mx <= 174 && 19 <= my && my <= 18 + 16 * rowCount;
	}

	private boolean isHoverSearch(int mx, int my) {
		mx -= leftPos;
		my -= topPos;
		return 75 <= mx && mx <= 162 && 5 <= my && my <= 14;
	}


	private boolean isInSlot(int mx, int my) {
		return (mx & 0xF) == mx && (my & 0xF) == my;
	}

	private int getHoverSlot(int mx, int my) {
		if (focusScroll)
			return -1;
		mx -= leftPos + 19;
		my -= topPos + 19;
		//Check: Pocket
		for (int y = 0; y < rowCount; y++)
			for (int x = 0; x < 9; x++)
				if (isInSlot(mx - x * 16, my - y * 16))
					return 46 + x + y * 9;
		my -= rowCount * 16 + 4;//pocket size + gap
		//Check: Crafting
		if (displayCrafting) {
			for (int gridY = 0; gridY < 3; gridY++)
				for (int gridX = 0; gridX < 3; gridX++)
					if (isInSlot(mx - 18 - 16 * gridX, my - 16 * gridY))
						return 36 + gridX + gridY * 3;
			if (102 <= mx && mx <= 125 && 12 <= my && my <= 35)
				return 45;
			my -= 52;//crafting size + gap
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
		if (displayCrafting && 19 <= mx && mx <= 34) {
			if (0 <= my && my <= 15) return 5;
			if (32 <= my && my <= 47) return 6;
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
		y = renderAndMove(stack, Sprites.OUTLINE_7, leftPos, y, displayCrafting ? 123 : 71);
		renderAndMove(stack, Sprites.OUTLINE_8, leftPos, y, 1);
	}

	private void renderFrame(PoseStack stack) {
		Sprites.BUTTONS_FRAME.blit(stack, leftPos + 1, topPos + 1);

		int x = leftPos + 15;
		int y = topPos + 1;
		y = renderAndMove(stack, Sprites.TOP, x, y);
		y = renderAndMove(stack, Sprites.GAP_SCROLL, x, y);
		y = renderAndMove(stack, Sprites.ROW_SCROLL, x, y, 16 * rowCount);
		y = renderAndMove(stack, Sprites.GAP_SCROLL, x, y);
		if (displayCrafting) {
			y = renderAndMove(stack, Sprites.CRAFTING_FRAME, x, y);
			y = renderAndMove(stack, Sprites.GAP_SMALL, x, y);
		}
		y = renderAndMove(stack, Sprites.ROW_SMALL, x, y, 48);
		y = renderAndMove(stack, Sprites.GAP_SMALL, x, y);
		y = renderAndMove(stack, Sprites.ROW_SMALL, x, y, 16);
		renderAndMove(stack, Sprites.GAP_SMALL, x, y);
	}

	private void renderSlotBase(PoseStack stack, int x, int y, int slotIndex) {
		boolean isHover = slotIndex == hoverSlot;
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
		boolean isHover = 45 == hoverSlot;
		(isHover ? Sprites.SLOT_BASE_CRAFTING_OUTPUT_H : Sprites.SLOT_BASE_CRAFTING_OUTPUT_N).blit(stack, x + 102, y + 12);
		return y + 52;
	}

	private void renderAllSlotBases(PoseStack stack) {
		int x = leftPos + 19;
		int y = topPos + 19;
		y = renderSlotBaseContainer(stack, x, y, rowCount, 46);
		if (displayCrafting)
			y = renderCraftingSlotBases(stack, x, y);
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
			getClearUButton(hoverButton == 5).blit(stack, leftPos + 19, topPos + 23 + 16 * rowCount);
			getClearDButton(hoverButton == 6).blit(stack, leftPos + 19, topPos + 55 + 16 * rowCount);
		}
		{ //Render: Scroll
			int x = leftPos + 167;
			int y = topPos + 19 + (maxScroll == 0 ? 0 : (16 * rowCount - 2) * scroll / maxScroll);
			(hoverScroll || focusScroll ? Sprites.SCROLL_H : Sprites.SCROLL_N).blit(stack, x, y);
		}
		renderAllSlotBases(stack);
	}

	private void renderSlotItemSingle(PoseStack poseStack, int x, int y, ItemStack itemStack) {
		if (itemStack.isEmpty())
			return;
		this.itemRenderer.renderAndDecorateItem(itemStack, x, y);
		this.itemRenderer.renderGuiItemDecorations(this.font, itemStack, x, y, null);
	}

	private void renderSlotItemSingle(PoseStack poseStack, int x, int y, int slotIndex) {
		Slot slot = menu.getSlot(slotIndex);
		ItemStack slotItem = slot.getItem();

		ItemStack carried = this.menu.getCarried();
		if (this.isQuickCrafting && this.quickCraftSlots.contains(slot) && !carried.isEmpty() &&
						this.quickCraftSlots.size() != 1 && AbstractContainerMenu.canItemQuickReplace(slot, carried, true) && this.menu.canDragTo(slot)) {
			int count = slotItem.isEmpty() ? 0 : slotItem.getCount();
			slotItem = carried.copy();
			AbstractContainerMenu.getQuickCraftSlotCount(this.quickCraftSlots, this.quickCraftingType, slotItem, count);
			int maxStackSize = Math.min(slotItem.getMaxStackSize(), slot.getMaxStackSize(slotItem));
			if (slotItem.getCount() > maxStackSize)
				slotItem.setCount(maxStackSize);
		}

		renderSlotItemSingle(poseStack, x, y, slotItem);
	}

	private void renderSlotItemsRow(PoseStack poseStack, int x, int y, int slotIndex) {
		for (int i = 0; i < 9; i++)
			renderSlotItemSingle(poseStack, x + 16 * i, y, slotIndex + i);
	}

	private void renderSlotItems(PoseStack poseStack) {
		int xOffset = leftPos + 19;
		int yOffset = topPos + 19;
		{ //Render: Pocket Slots
			int x = 0;
			int y = 0;
			for (var pocketSlot : visiblePocketSlots) {
				ItemStack itemStack = pocketSlot.getKey().create();
				int displayX = xOffset + x * 16;
				int displayY = yOffset + y * 16;
				renderSlotItemSingle(poseStack, displayX, displayY, itemStack);
				long count = pocketSlot.getValue();
				if (count != 1) {
					poseStack.pushPose();
					String displayText = DeepPocketUtils.advancedToString(count);
					poseStack.translate(0.0D, 0.0D, Minecraft.getInstance().getItemRenderer().blitOffset + 200.0F);
					poseStack.scale(0.5f, 0.5f, 1f);
					MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
					font.drawInBatch(displayText, (float)(displayX * 2 + 32 - font.width(displayText)), (float)(displayY * 2 + 24), 0xFFFFFF, true, poseStack.last().pose(), bufferSource, false, 0, 0xF000F0);
					bufferSource.endBatch();
					poseStack.popPose();
				}

				if (++x == 9) {
					x = 0;
					++y;
				}
			}
			yOffset += 16 * rowCount + 4;
		}
		if (displayCrafting) { //Render: Crafting Slots
			int slotIndex = 36;
			for (int gridY = 0; gridY < 3; gridY++)
				for (int gridX = 0; gridX < 3; gridX++)
					renderSlotItemSingle(poseStack, xOffset + 18 + 16 * gridX, yOffset + 16 * gridY, slotIndex++);
			renderSlotItemSingle(poseStack, xOffset + 106, yOffset + 16, 45);
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
			font.draw(stack, displayPocketName, leftPos + 20, topPos + 6, 0xFFFFFF);
		}
		String displaySearch = font.plainSubstrByWidth(search, focusSearch ? 80 : 86, true) + (focusSearch ? DeepPocketUtils.getTimedTextEditSuffix() : "");
		font.draw(stack, displaySearch, leftPos + 76, topPos + 6, 0xDDDDDD);
	}

	private int getQuickCraftRemaining() {
		ItemStack itemstack = this.menu.getCarried();
		if (itemstack.isEmpty() || !this.isQuickCrafting)
			return 0;
		if (this.quickCraftingType == 2) {
			return itemstack.getMaxStackSize();
		}
		int result = itemstack.getCount();

		for(Slot slot : this.quickCraftSlots) {
			ItemStack itemStack1 = itemstack.copy();
			ItemStack itemStack2 = slot.getItem();
			int i = itemStack2.isEmpty() ? 0 : itemStack2.getCount();
			AbstractContainerMenu.getQuickCraftSlotCount(this.quickCraftSlots, this.quickCraftingType, itemStack1, i);
			int j = Math.min(itemStack1.getMaxStackSize(), slot.getMaxStackSize(itemStack1));
			if (itemStack1.getCount() > j) {
				itemStack1.setCount(j);
			}

			result -= itemStack1.getCount() - i;
		}
		return result;
	}

	private void renderCarriedItem(PoseStack poseStack, int mx, int my) {
		poseStack.pushPose();
		poseStack.translate(0, 0, 32D);
		this.setBlitOffset(200);
		this.itemRenderer.blitOffset = 200.0F;

		ItemStack itemstack = this.menu.getCarried();
		if (this.isQuickCrafting && this.quickCraftSlots.size() > 1) {
			itemstack = itemstack.copy();
			itemstack.setCount(getQuickCraftRemaining());
		}
		renderSlotItemSingle(poseStack, mx - 8, my - 8, itemstack);

		poseStack.popPose();
		this.setBlitOffset(0);
		this.itemRenderer.blitOffset = 0.0F;
	}

	private void superRender(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		this.renderBg(pPoseStack, pPartialTick, pMouseX, pMouseY);
		//noinspection UnstableApiUsage
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Background(this, pPoseStack, pMouseX, pMouseY));
		RenderSystem.disableDepthTest();
		RenderSystem.applyModelViewMatrix();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		this.hoveredSlot = 0 <= hoverSlot && hoverSlot < menu.slots.size() ? menu.getSlot(hoverSlot) : null;
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		renderSlotItems(pPoseStack);

		this.renderLabels(pPoseStack, pMouseX, pMouseY);
		//noinspection UnstableApiUsage
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Foreground(this, pPoseStack, pMouseX, pMouseY));
		renderCarriedItem(pPoseStack, pMouseX, pMouseY);


		RenderSystem.applyModelViewMatrix();
		RenderSystem.enableDepthTest();
	}

	@Override
	protected void renderTooltip(PoseStack stack, int x, int y) {
		if (!menu.getCarried().isEmpty())
			return;
		if (hoverButton >= 0)
			this.renderButtonTooltip(stack, x, y);
		else if (hoverSlot >= 46)
			this.renderPocketTooltip(stack, x, y);
		else
			super.renderTooltip(stack, x, y);
	}

	private void renderButtonTooltip(PoseStack stack, int x, int y) {
		Component text = switch (hoverButton) {
			case 0 -> Component.literal("Settings");
			case 1 -> Component.literal("Search Mode: ").append(Component.literal(DeepPocketClientApi.get().getSearchMode().displayName).withStyle(ChatFormatting.AQUA));
			case 2 -> Component.literal("Sort: ").append(Component.literal(DeepPocketClientApi.get().getSortingOrder().displayName).withStyle(ChatFormatting.AQUA));
			case 3 -> Component.literal("Sort Direction: ").append(Component.literal(DeepPocketClientApi.get().isSortAscending() ? "Ascending" : "Descending").withStyle(ChatFormatting.AQUA));
			case 4 -> Component.literal("Display Crafting: ").append(Component.literal(DeepPocketClientApi.get().isDisplayCrafting() ? "True" : "False").withStyle(ChatFormatting.AQUA));
			case 5 -> Component.literal("Clear To Pocket");
			case 6 -> Component.literal("Clear To Inventory");
			default -> null;
		};
		if (text == null)
			return;
		super.renderTooltip(stack, text, x, y);
	}

	private void renderPocketTooltip(PoseStack stack, int x, int y) {
		int index = hoverSlot - 46;
		if (index < 0 || visiblePocketSlots.size() <= index)
			return;
		ItemType hoverType = visiblePocketSlots.get(index).getKey();
		super.renderTooltip(stack, hoverType.create(), x, y);
	}

	@Override
	public void render(PoseStack stack, int mx, int my, float partialTick) {
		reloadPosition(mx, my);
		superRender(stack, mx, my, partialTick);
		renderTooltip(stack, mx, my);
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

	private void mouseClickedPocket(int button) {
		int clickedSlot = hoverSlot - 46;
		ItemType clickedItem = clickedSlot < 0 || visiblePocketSlots.size() <= clickedSlot ? null : visiblePocketSlots.get(clickedSlot).getKey();
		ItemStack carried = menu.getCarried();
		boolean shift = Screen.hasShiftDown();
		if (carried.isEmpty() || new ItemType(carried).equals(clickedItem) || shift) {
			if (clickedItem != null) {
				byte count = switch (button) {
					case InputConstants.MOUSE_BUTTON_LEFT -> (byte)64;
					default -> (byte)1;
					case InputConstants.MOUSE_BUTTON_RIGHT -> (byte)32;
				};
				DeepPocketPacketHandler.sbPocketExtract(clickedItem, !shift, count);
			}
			return;
		}
		byte count = switch (button) {
			case InputConstants.MOUSE_BUTTON_LEFT -> (byte) 64;
			case InputConstants.MOUSE_BUTTON_MIDDLE -> (byte) ((carried.getCount() + 1) / 2);
			default -> (byte) 1;
		};
		DeepPocketPacketHandler.sbPocketInsert(count);
		carried.shrink(count);
		menu.setCarried(carried);
	}

	private boolean handleButtonClick() {
		switch (hoverButton) {
			case 0 -> {
				Pocket pocket = menu.getPocket();
				if (pocket != null && pocket.getOwner().equals(menu.playerInventory.player.getUUID()))
					ClientScreens.openScreenSettingsEdit(pocket);
			}
			case 1 -> toggleSearchMode();
			case 2 -> toggleSortingOrder();
			case 3 -> toggleSortAscending();
			case 4 -> toggleDisplayCrafting();
			case 5 -> DeepPocketPacketHandler.sbClearCraftingGrid(true);
			case 6 -> DeepPocketPacketHandler.sbClearCraftingGrid(false);
			default -> { return false; }
		}
		DeepPocketUtils.playClickSound();
		return true;
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		focusSearch = false;
		Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
		//Buttons
		if (button == InputConstants.MOUSE_BUTTON_LEFT) {
			if (handleButtonClick())
				return true;
			if (hoverScroll) { focusScroll = true; mouseMoved(mx, my); return true;}
			if (hoverSearch) { focusSearch = true; Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true); return true;}
		}
		if (hoverSlot >= 46) {
			mouseClickedPocket(button);
			return true;
		}
		//Run vanilla mouseClicked
		boolean isQuickCraftingBefore = isQuickCrafting;
		if (!super.mouseClicked(mx, my, button))
			return false;
		//Update quickCraftingType based on how the vanilla does
		if (!isQuickCraftingBefore && isQuickCrafting) {
			if (button == 0) {
				this.quickCraftingType = 0;
			} else if (button == 1) {
				this.quickCraftingType = 1;
			} else if (Minecraft.getInstance().options.keyPickItem.isActiveAndMatches(InputConstants.Type.MOUSE.getOrCreate(button))) {
				this.quickCraftingType = 2;
			}
		}
		return true;
	}

	@Override
	public void mouseMoved(double mx, double my) {
		if (focusScroll)
			scroll = Math.max(Math.min((((int)my - topPos - 19) * maxScroll / (rowCount * 8) + 1) / 2, maxScroll), 0);
	}

	@Override
	public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
		focusScroll = false;
		return super.mouseReleased(pMouseX, pMouseY, pButton);
	}

	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		if (pDelta > 0)
			scroll--;
		else if (pDelta < 0)
			scroll++;
		return super.mouseScrolled(pMouseX, pMouseY, pDelta);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (focusSearch) {
			if (keyCode == InputConstants.KEY_BACKSPACE && search.length() > 0) {
				search = search.substring(0, search.length() - 1);
				if (DeepPocketClientApi.get().getSearchMode().syncTo)
					DeepPocketJEI.setSearch(search);
			}
			return true;
		}
		super.keyPressed(keyCode, scanCode, modifiers);
		return true;
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (focusSearch) {
			search += codePoint;
			if (DeepPocketClientApi.get().getSearchMode().syncTo)
				DeepPocketJEI.setSearch(search);
		}
		return true;
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
		return DeepPocketClientApi.get().isDisplayCrafting() ?
						hover ? Sprites.DISPLAY_CRAFTING_1H : Sprites.DISPLAY_CRAFTING_1N :
						hover ? Sprites.DISPLAY_CRAFTING_0H : Sprites.DISPLAY_CRAFTING_0N;
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
		DeepPocketClientApi.get().setDisplayCrafting(!DeepPocketClientApi.get().isDisplayCrafting());
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
		SETTINGS_N(216, 60, 10, 10),SETTINGS_H(226, 60, 10, 10),
		CLEAR_UN(224, 70, 16, 16),CLEAR_UH(240, 70, 16, 16),
		CLEAR_DN(224, 86, 16, 16),CLEAR_DH(240, 86, 16, 16),
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
