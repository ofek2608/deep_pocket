package com.ofek2608.deep_pocket.registry.interfaces.crafter;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.struct.client.ClientPocket;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.registry.items.crafting_pattern.CraftingPatternItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CrafterScreen extends AbstractContainerScreen<CrafterMenu> {
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/crafter_generic_54.png");
	private final DeepPocketClientHelper dpClientHelper = DeepPocketClientHelper.get();
	private final int containerRows;
	private int hoveredSlotIndex;
	private int quickCraftingType;

	public CrafterScreen(CrafterMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.passEvents = false;
		this.containerRows = menu.getRowCount();
		this.imageWidth = 154;
		this.imageHeight = 96 + this.containerRows * 16;
	}

	private int getHoveredSlotIndex(int mx, int my) {
		mx -= leftPos;
		my -= topPos;
		if (mx < 5 || 149 <= mx)
			return -1;
		int slotCol = (mx - 5) / 16;
		my -= 19;
		if (my < 0)
			return -1;
		int slotRow = my / 16;
		if (slotRow < containerRows)
			return slotCol + slotRow * 9;
		my -= containerRows * 16 + 4;
		if (my < 0)
			return -1;
		slotRow = my / 16;
		if (slotRow < 3)
			return slotCol + (containerRows + slotRow) * 9;
		if (52 <= my && my < 68)
			return slotCol + (containerRows + 3) * 9;
		return -1;
	}

	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		this.hoveredSlotIndex = getHoveredSlotIndex(mx, my);
		this.hoveredSlot = this.hoveredSlotIndex < 0 ? null : menu.getSlot(this.hoveredSlotIndex);

		this.renderBackground(poseStack);
		superRender(poseStack, mx, my, partialTick);
		this.renderTooltip(poseStack, mx, my);
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
		dpClientHelper.renderItem(poseStack, mx - 8, my - 8, itemstack, itemRenderer, font);

		poseStack.popPose();
		this.setBlitOffset(0);
		this.itemRenderer.blitOffset = 0.0F;
	}

	private void renderSlotItemSingle(PoseStack poseStack, int x, int y, int slotIndex, boolean getPattern) {
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
		if (!slotItem.isEmpty() && getPattern)
			slotItem = CraftingPatternItem.getCachedDisplayedResult(slotItem);

		dpClientHelper.renderItem(poseStack, x, y, slotItem, itemRenderer, font);
	}

	private void renderSlotItemsContainer(PoseStack poseStack, int y, int rows, int slotIndex, boolean getPattern) {
		for (int slotY = 0; slotY < rows; slotY++)
			for (int slotX = 0; slotX < 9; slotX++)
				renderSlotItemSingle(poseStack, 5 + 16 * slotX, y + 16 * slotY, slotIndex + slotX + slotY * 9, getPattern);
	}

	private void renderSlotItems(PoseStack poseStack) {
		renderSlotItemsContainer(poseStack, 19, containerRows, 0, true);
		renderSlotItemsContainer(poseStack, 23 + containerRows * 16, 3, containerRows * 9, false);
		renderSlotItemsContainer(poseStack, 75 + containerRows * 16, 1, (containerRows + 3) * 9, false);
	}

	private void superRender(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		this.renderBg(pPoseStack, pPartialTick, pMouseX, pMouseY);
		//noinspection UnstableApiUsage
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ContainerScreenEvent.Render.Background(this, pPoseStack, pMouseX, pMouseY));
		RenderSystem.disableDepthTest();
		PoseStack stack = RenderSystem.getModelViewStack();
		stack.pushPose();
		stack.translate(this.leftPos, this.topPos, 0.0D);
		RenderSystem.applyModelViewMatrix();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		renderSlotItems(pPoseStack);

		this.renderLabels(pPoseStack, pMouseX, pMouseY);
		//noinspection UnstableApiUsage
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ContainerScreenEvent.Render.Foreground(this, pPoseStack, pMouseX, pMouseY));

		stack.popPose();

		renderCarriedItem(pPoseStack, pMouseX, pMouseY);
		RenderSystem.applyModelViewMatrix();
		RenderSystem.enableDepthTest();
	}

	@Override
	protected void renderLabels(PoseStack poseStack, int mx, int my) {
		this.font.draw(poseStack, this.title, 6, 6, 0xFFFFFF);
	}

	protected void renderBg(PoseStack poseStack, float partialTick, int mx, int my) {
		poseStack.pushPose();
		poseStack.translate(leftPos, topPos, 0);

		ClientPocket pocket = menu.getClientPocket();

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);

		DeepPocketUtils.setRenderShaderColor(pocket == null ? 0xFFFFFF : pocket.getColor());
		renderOutline(poseStack);
		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		renderFrame(poseStack);
		renderSlotBases(poseStack);

		poseStack.popPose();
	}

	private void renderOutline(PoseStack poseStack) {
		Sprites.OUTLINE_TOP.blit(poseStack, 0, 0);
		Sprites.OUTLINE_MIDDLE.blit(poseStack, 0, 1, 154, imageHeight - 2);
		Sprites.OUTLINE_BOTTOM.blit(poseStack, 0, imageHeight - 1);
	}

	private void renderFrame(PoseStack poseStack) {
		Sprites.FRAME_TOP.blit(poseStack, 0, 1);
		Sprites.FRAME_GAP.blit(poseStack, 0, 15);
		Sprites.FRAME_SLOTS.blit(poseStack, 0, 19, 154, containerRows * 16);
		Sprites.FRAME_GAP.blit(poseStack, 0, imageHeight - 77);
		Sprites.FRAME_SLOTS.blit(poseStack, 0, imageHeight - 73, 154, 48);
		Sprites.FRAME_GAP.blit(poseStack, 0, imageHeight - 25);
		Sprites.FRAME_SLOTS.blit(poseStack, 0, imageHeight - 21);
		Sprites.FRAME_GAP.blit(poseStack, 0, imageHeight - 5);

	}

	private void renderSlotBases(PoseStack poseStack) {
		int hoverSlotIndex = this.hoveredSlotIndex;
		for (int row = 0; row < this.containerRows; row++)
			for (int col = 0; col < 9; col++)
				(hoverSlotIndex-- == 0 ? Sprites.SLOT_BASE_PATTERN_H : Sprites.SLOT_BASE_PATTERN_N).blit(poseStack, 5 + 16 * col, 19 + 16 * row);
		for (int row = 0; row < 4; row++)
			for (int col = 0; col < 9; col++)
				(hoverSlotIndex-- == 0 ? Sprites.SLOT_BASE_H : Sprites.SLOT_BASE_N).blit(poseStack, 5 + 16 * col, imageHeight + (row == 3 ? -21 : row * 16 - 73));
	}





	@Override
	public boolean mouseClicked(double mx, double my, int button) {
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








	private enum Sprites {
		//Outline
		OUTLINE_TOP(0,0,154,1),
		OUTLINE_MIDDLE(0,1,154,1),
		OUTLINE_BOTTOM(0,2,154,1),
		//Frame
		FRAME_TOP(0,3,154,14),
		FRAME_GAP(0,17,154,4),
		FRAME_SLOTS(0,21,154,16),
		//Slot Base
		SLOT_BASE_N(240, 0, 16, 16),
		SLOT_BASE_H(240, 16, 16, 16),
		SLOT_BASE_PATTERN_N(224, 0, 16, 16),
		SLOT_BASE_PATTERN_H(224, 16, 16, 16)
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

		@SuppressWarnings("SameParameterValue")
		private void blit(PoseStack stack, int x, int y, int displayW, int displayH) {
			Screen.blit(stack, x, y, displayW, displayH, u, v, w, h, 256, 256);
		}
	}
}
