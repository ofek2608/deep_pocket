package com.ofek2608.deep_pocket.registry.pocket_screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.enums.PocketDisplayMode;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.client.client_screens.ClientScreens;
import com.ofek2608.deep_pocket.client.widget.*;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
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

public class PocketScreen extends AbstractContainerScreen<PocketMenu> {
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/pocket.png");
	private final DeepPocketClientHelper dpClientHelper = DeepPocketClientHelper.get();

	//render fields
	private PocketDisplayMode pocketDisplayMode;
	//render hover fields
	private int hoverButton;
	//widgets
	private final PocketSearchWidget pocketSearchWidget;
	private final PocketTabWidget pocketTabWidget;
	private final PatternWidget patternWidget;
	private final InventoryDisplayWidget inventoryDisplayWidget;
	private final CraftingDisplayWidget craftingDisplayWidget;
	
	public PocketScreen(PocketMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
		menu.screen = this;
		addRenderableWidget(pocketSearchWidget = new PocketSearchWidget(this, menu::getPocket));
		addRenderableWidget(pocketTabWidget = new PocketTabWidget(40, 0, menu::getPocket));
		addRenderableWidget(patternWidget = new PatternWidget(this));
		addRenderableWidget(inventoryDisplayWidget = new InventoryDisplayWidget(0, this::renderSlotItemSingle));
		addRenderableWidget(craftingDisplayWidget = new CraftingDisplayWidget(
				36,
				this::renderSlotItemSingle,
				()->DeepPocketPacketHandler.sbClearCraftingGrid(true),
				()->DeepPocketPacketHandler.sbClearCraftingGrid(false),
				()-> {
					Pocket pocket = menu.getPocket();
					if (pocket != null && menu.getSlot(45).hasItem())
						ClientScreens.bulkCrafting(pocket, menu.getCrafting());
				},
				()->menu.getSlot(45).hasItem()
		));
		
		Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
	}
	
	@Override
	protected void rebuildWidgets() {
		this.setFocused(null);
	}
	
	public void setPattern(ElementType[] input, ItemStack output) {
		patternWidget.setPattern(input, output);
	}


	private void reloadPosition() {
		this.pocketDisplayMode = DeepPocketClientHelper.get().getPocketDisplayMode();
		int imageHeightExcludingRows = pocketDisplayMode == PocketDisplayMode.NORMAL ? 110 : 162;
		int rowsHeight = Math.min(height - imageHeightExcludingRows, 144);
		this.imageWidth = 180;
		this.imageHeight = imageHeightExcludingRows + rowsHeight;
		this.leftPos = (this.width - 152) / 2 - 15;
		this.topPos = (this.height - this.imageHeight) / 2;
		
		this.pocketSearchWidget.setPos(leftPos + 15, topPos + 33);
		this.pocketSearchWidget.setHeight(rowsHeight);
		
		this.pocketTabWidget.offX = leftPos + 15;
		this.pocketTabWidget.offY = topPos + 1;
		
		this.patternWidget.setPos(leftPos + 15, pocketDisplayMode == PocketDisplayMode.CREATE_PATTERN ? pocketSearchWidget.getOffY() + pocketSearchWidget.getHeight() + 4 : -0xFFFFFF);
		
		this.inventoryDisplayWidget.offX = leftPos + 15;
		this.inventoryDisplayWidget.offY = topPos + imageHeight - 77;
		
		this.craftingDisplayWidget.setPos(leftPos + 15, pocketDisplayMode == PocketDisplayMode.CRAFTING ? pocketSearchWidget.getOffY() + pocketSearchWidget.getHeight() + 4 : -0xFFFFFF);
	}

	private void reloadPosition(int mx, int my) {
		reloadPosition();

		this.hoverButton = getHoverButton(mx, my);
		
		this.hoveredSlot = null;
		reloadHoveredSlotIndex(my);
	}

	private int getHoverButton(int mx, int my) {
		mx -= leftPos;
		my -= topPos;
		if (5 <= mx && mx <= 14) {
			if (5 <= my && my <= 14) return 0;
			if (49 <= my && my <= 58) return 4;
		}
		return -1;
	}


	private int renderAndMove(PoseStack poseStack, Sprites sprite, int x, int y, int height) {
		sprite.blit(poseStack, x, y, sprite.w, height);
		return y + height;
	}

	private void renderOutline(PoseStack poseStack) {
		int y = topPos;
		y = renderAndMove(poseStack, Sprites.OUTLINE_0, leftPos, y, 1);
		y = renderAndMove(poseStack, Sprites.OUTLINE_1, leftPos, y, 41);
		y = renderAndMove(poseStack, Sprites.OUTLINE_2, leftPos, y, 1);
		y = renderAndMove(poseStack, Sprites.OUTLINE_3, leftPos, y, 20);
		y = renderAndMove(poseStack, Sprites.OUTLINE_4, leftPos, y, 1);
		y = renderAndMove(poseStack, Sprites.OUTLINE_5, leftPos, y, pocketSearchWidget.getHeight() - 27);
		y = renderAndMove(poseStack, Sprites.OUTLINE_6, leftPos, y, 1);
		y = renderAndMove(poseStack, Sprites.OUTLINE_7, leftPos, y, pocketDisplayMode == PocketDisplayMode.NORMAL ? 71 : 123);
		renderAndMove(poseStack, Sprites.OUTLINE_8, leftPos, y, 1);
	}

	private void renderFrame(PoseStack poseStack) {
		int x = leftPos + 15;
		int y = topPos + 33 + pocketSearchWidget.getHeight();
		Sprites.GAP_SCROLL.blit(
				poseStack,
				x, y
		);
	}

	private void renderHoverAbles(PoseStack poseStack) {
		//TODO remove
		{ //Render: Buttons
			getSettingsButton(hoverButton == 0).blit(poseStack, leftPos + 5, topPos + 5);
			getDisplayAscendingButton(hoverButton == 4).blit(poseStack, leftPos + 5, topPos + 49);
		}
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


	@Override
	protected void renderLabels(PoseStack poseStack, int mx, int my) {}

	@Override
	protected void renderTooltip(PoseStack poseStack, int x, int y) {
		if (!menu.getCarried().isEmpty())
			return;
		if (hoverButton >= 0)
			this.renderButtonTooltip(poseStack, x, y);
		
		super.renderTooltip(poseStack, x, y);
		
		for (var child : children())
			if (child instanceof WidgetWithTooltip tooltip)
				tooltip.renderTooltip(this, poseStack, x, y);
	}

	private void renderButtonTooltip(PoseStack poseStack, int x, int y) {
		Component text = switch (hoverButton) {
			case 0 -> Component.literal("Settings");
			case 4 -> Component.literal("Display Mode: ").append(Component.literal(DeepPocketClientHelper.get().getPocketDisplayMode().displayName).withStyle(ChatFormatting.AQUA));
			default -> null;
		};
		if (text == null)
			return;
		super.renderTooltip(poseStack, text, x, y);
	}
	
	private void reloadHoveredSlotIndex(int my) {
		int hoveredSlotIndex = Math.max(inventoryDisplayWidget.getHoveredSlotIndex(), craftingDisplayWidget.getHoveredSlotIndex());
		menu.setHoveredSlotIndex(hoveredSlotIndex, my - topPos);
		if (0 <= hoveredSlotIndex && hoveredSlotIndex < menu.slots.size())
			hoveredSlot = menu.getSlot(hoveredSlotIndex);
	}

	@Override
	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		reloadPosition(mx, my);
		menu.clearHoverSlotIndex();
		
		renderBackground(poseStack);
		super.render(poseStack, mx, my, partialTick);
		
		reloadHoveredSlotIndex(my);
		renderTooltip(poseStack, mx, my);
		
		reloadHoveredSlotIndex(my);
		
		ItemStack hoveredItem = pocketSearchWidget.pocketWidget.getHoveredItem();
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
			case 4 -> toggleDisplayCrafting();
			default -> { return false; }
		}
		DeepPocketUtils.playClickSound();
		return true;
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		reloadPosition((int)mx, (int)my);
		//TODO remove
		//Buttons
		if (button == InputConstants.MOUSE_BUTTON_LEFT) {
			if (handleButtonClick())
				return true;
		}
		return super.mouseClicked(mx, my, button);
	}

	@Override
	public void mouseMoved(double mx, double my) {
		pocketSearchWidget.mouseMoved(mx, my);
	}

	@Override
	public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
		pocketSearchWidget.mouseReleased(pMouseX, pMouseY, pButton);
		return super.mouseReleased(pMouseX, pMouseY, pButton);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double delta) {
		return pocketSearchWidget.mouseScrolled(mx, my, delta);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (getFocused() == pocketSearchWidget) {
			pocketSearchWidget.keyPressed(keyCode, scanCode, modifiers);
			if (keyCode == InputConstants.KEY_ESCAPE)
				this.onClose();
			return true;
		}
		super.keyPressed(keyCode, scanCode, modifiers);
		return true;
	}
	
	@Override
	public void onClose() {
		Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
		super.onClose();
	}
	
	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		return super.charTyped(codePoint, modifiers);
	}

	private static Sprites getDisplayAscendingButton(boolean hover) {
		return switch (DeepPocketClientHelper.get().getPocketDisplayMode()) {
			case NORMAL -> hover ? Sprites.DISPLAY_CRAFTING_0H : Sprites.DISPLAY_CRAFTING_0N;
			case CRAFTING -> hover ? Sprites.DISPLAY_CRAFTING_1H : Sprites.DISPLAY_CRAFTING_1N;
			case CREATE_PATTERN -> hover ? Sprites.DISPLAY_CRAFTING_2H : Sprites.DISPLAY_CRAFTING_2N;
		};
	}

	private static Sprites getSettingsButton(boolean hover) {
		return hover ? Sprites.SETTINGS_H : Sprites.SETTINGS_N;
	}

	private static void toggleDisplayCrafting() {
		DeepPocketClientHelper.get().setPocketDisplayMode(switch (DeepPocketClientHelper.get().getPocketDisplayMode()) {
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
		GAP_SCROLL(0,14,164,4),
		//Buttons
		DISPLAY_CRAFTING_0N(236, 20, 10, 10),DISPLAY_CRAFTING_0H(236, 50, 10, 10),
		DISPLAY_CRAFTING_1N(246, 20, 10, 10),DISPLAY_CRAFTING_1H(246, 50, 10, 10),
		DISPLAY_CRAFTING_2N(236, 60, 10, 10),DISPLAY_CRAFTING_2H(246, 60, 10, 10),
		SETTINGS_N(216, 60, 10, 10),SETTINGS_H(226, 60, 10, 10),
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
