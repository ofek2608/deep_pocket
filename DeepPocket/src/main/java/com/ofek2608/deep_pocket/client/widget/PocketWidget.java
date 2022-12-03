package com.ofek2608.deep_pocket.client.widget;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import com.ofek2608.deep_pocket.client.client_screens.ClientScreens;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PocketWidget implements Widget, GuiEventListener, NonNarratableEntry {
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/widget/pocket.png");
	private static final DeepPocketClientHelper dpClientHelper = DeepPocketClientHelper.get();
	private final AbstractContainerScreen<?> screen;
	public int offX;
	public int offY;
	public int height;
	public float scroll;
	public Supplier<Pocket> pocketSupplier;
	public Supplier<Predicate<ItemType>> filterSupplier;
	
	private int maxScroll;
	private boolean holdCraft;
	private boolean holdScroll;
	private boolean hoverScroll;
	private boolean insideContainer;
	private @Nullable ItemTypeAmount lastHoveredType;
	
	
	public PocketWidget(AbstractContainerScreen<?> screen, int offX, int offY, int height,
	                    Supplier<Pocket> pocketSupplier, Supplier<Predicate<ItemType>> filterSupplier) {
		this.screen = screen;
		this.offX = offX;
		this.offY = offY;
		this.height = height;
		this.pocketSupplier = pocketSupplier;
		this.filterSupplier = filterSupplier;
	}
	
	@Override
	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		poseStack.pushPose();
		
		lastHoveredType = null;
		hoverScroll = false;
		if (height <= 0)
			return;
		Pocket pocket = pocketSupplier.get();
		Predicate<ItemType> filter = filterSupplier.get();
		if (pocket == null || filter == null)
			return;
		
		List<ItemTypeAmount> types = DeepPocketClientApi.get()
				.getSortedKnowledge(pocket)
				.filter(itemTypeAmount -> filter.test(itemTypeAmount.getItemType()))
				.toList();
		
		mx -= offX;
		my -= offY;
		
		maxScroll = Math.max(((types.size() - 1) / 9 + 1) * 16 - height, 0);
		scroll = scroll < 0 ? 0 : scroll > maxScroll ? maxScroll : scroll;
		holdCraft = Screen.hasControlDown();
		hoverScroll = 152 <= mx && mx < 160 && 0 <= my && my < height;
		insideContainer = 4 <= mx && mx < 148 && 0 <= my && my < height;
		int hoverSlotIndex = -1;
		if (insideContainer) {
			int hoverSlotX = (mx - 4) / 16;
			int hoverSlotY = (int)(my + scroll) / 16;
			hoverSlotIndex = hoverSlotX + 9 * hoverSlotY;
		}
		lastHoveredType = 0 <= hoverSlotIndex && hoverSlotIndex < types.size() ? types.get(hoverSlotIndex) : null;
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);
		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		
		blitOutline(poseStack, offX, offY, height);
		blitScroll(poseStack, offX, offY, scroll, maxScroll, height, hoverScroll);
		
		GuiComponent.enableScissor(offX + 4, offY, offX + 148, offY + height);
		
		int slotsOffX = offX + 4;
		int slotsOffY = (int) (offY - scroll);
		int firstRow = (int)scroll / 16;
		int lastRow = (int)(scroll + height) / 16;
		
		
		blitSlotRange(poseStack, types, slotsOffX, slotsOffY, firstRow, lastRow, hoverSlotIndex);
		renderTypeRange(poseStack, types, slotsOffX, slotsOffY, firstRow - 1, lastRow + 1);
		
		GuiComponent.disableScissor();
		
		if (lastHoveredType != null && !lastHoveredType.isEmpty())
			screen.renderTooltip(poseStack, lastHoveredType.getItemType().create(), offX + mx, offY + my);
	}
	
	private void blitSlotRange(PoseStack poseStack, List<ItemTypeAmount> types, int offX, int offY, int firstRow, int lastRow, int hoverIndex) {
		int slotIndex = firstRow * 9;
		for (int slotY = firstRow; slotY <= lastRow; slotY++) {
			for (int slotX = 0; slotX < 9; slotX++) {
				blitSlot(poseStack, slotX * 16 + offX, slotY * 16 + offY, getSlotType(types, slotIndex), slotIndex == hoverIndex);
				slotIndex++;
			}
		}
	}
	
	private SlotType getSlotType(List<ItemTypeAmount> types, int index) {
		if (index < 0 || types.size() <= index)
			return SlotType.OUTSIDE;
		Pocket pocket = pocketSupplier.get();
		ItemTypeAmount type = types.get(index);
		if (DeepPocketClientApi.get().getItemConversions().hasValue(type.getItemType()))
			return SlotType.CONVERT;
		if (pocket != null && isCrafting(pocket, type, holdCraft))
			return SlotType.CRAFT;
		return SlotType.NORMAL;
	}
	
	private void renderTypeRange(PoseStack poseStack, List<ItemTypeAmount> types, int offX, int offY, int firstRow, int lastRow) {
		Pocket pocket = pocketSupplier.get();
		if (pocket == null)
			return;
		int slotIndex = firstRow * 9;
		for (int slotY = firstRow; slotY <= lastRow; slotY++) {
			for (int slotX = 0; slotX < 9; slotX++) {
				if (0 <= slotIndex && slotIndex < types.size())
					renderType(pocket, poseStack, slotX * 16 + offX, slotY * 16 + offY, types.get(slotIndex), holdCraft);
				slotIndex++;
			}
		}
	}
	
	private static void blitOutline(PoseStack poseStack, int offX, int offY, int height) {
		if (height < 0)
			return;
		Screen.blit(
				poseStack,
				offX, offY,
				164, height,
				0, 0,
				164, 16,
				256, 256
		);
	}
	
	private static void blitScroll(PoseStack poseStack, int offX, int offY, float scroll, int maxScroll, int height, boolean hover) {
		if (height < 2)
			return;
		poseStack.pushPose();
		poseStack.translate(0, maxScroll <= 0 ? 0 : (scroll / maxScroll) * (height - 2), 0);
		Screen.blit(
				poseStack,
				offX + 152, offY,
				164, hover ? 2 : 0,
				8, 2,
				256, 256
		);
		poseStack.popPose();
	}
	
	private static void blitSlot(PoseStack poseStack, int x, int y, SlotType slotType, boolean hover) {
		Screen.blit(
				poseStack,
				x, y,
				hover ? 240 : 224, slotType.ordinal() * 16,
				16, 16,
				256, 256
		);
	}
	
	private static void renderType(
			Pocket pocket, PoseStack poseStack,
			int offX, int offY,
			ItemTypeAmount type, boolean holdCraft
	) {
		Minecraft minecraft = Minecraft.getInstance();
		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		Font font = minecraft.font;
		
		dpClientHelper.renderItem(poseStack, offX, offY, type.getItemType().create(), itemRenderer, font);
		if (isCrafting(pocket, type, holdCraft))
			dpClientHelper.renderAmount(poseStack, offX, offY, "craft", itemRenderer, font);
		else
			dpClientHelper.renderAmount(poseStack, offX, offY, type.getAmount(), itemRenderer, font);
	}
	
	private static boolean isCrafting(Pocket pocket, ItemTypeAmount typeAmount, boolean holdCraft) {
		return dpClientHelper.getCraftableItems(pocket).anyMatch(typeAmount.getItemType()::equals) && (holdCraft || typeAmount.getAmount() == 0);
	}
	
	
	
	
	private void updateScroll(double my) {
		scroll = (float) (maxScroll * (my - offY - 1) / (height - 2));
	}
	
	@Override
	public boolean mouseScrolled(double mx, double my, double delta) {
		scroll -= delta * 16;
		return true;
	}
	
	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (insideContainer && extract(button))
			return true;
		if (hoverScroll && button == InputConstants.MOUSE_BUTTON_LEFT) {
			holdScroll = true;
			updateScroll(my);
			changeFocus(true);
			return true;
		}
		return false;
	}
	
	private boolean extract(int button) {
		Pocket pocket = pocketSupplier.get();
		if (pocket == null)
			return false;
		
		ItemType clickedType = lastHoveredType == null ? null : lastHoveredType.getItemType();
		if (clickedType != null && !clickedType.isEmpty() && isCrafting(pocket, lastHoveredType, holdCraft)) {
			ClientScreens.selectNumber(Component.literal("Request Crafting"), pocket.getColor(), 0L, selectedAmount->{
				Minecraft.getInstance().setScreen(screen);
				if (selectedAmount != 0)
					ClientScreens.processRequest(pocket, clickedType, selectedAmount);
			});
			return true;
		}
		
		AbstractContainerMenu menu = screen.getMenu();
		
		ItemStack carried = menu.getCarried();
		if (holdCraft)
			return true;
		if (clickedType != null && (carried.isEmpty() || clickedType.is(carried))) {
			byte count = switch (button) {
				case InputConstants.MOUSE_BUTTON_LEFT -> (byte) 64;
				default -> (byte) 1;
				case InputConstants.MOUSE_BUTTON_RIGHT -> (byte) 32;
			};
			DeepPocketPacketHandler.sbPocketExtract(clickedType, !Screen.hasShiftDown(), count);
			return true;
		}
		byte count = switch (button) {
			case InputConstants.MOUSE_BUTTON_LEFT -> (byte) 64;
			case InputConstants.MOUSE_BUTTON_MIDDLE -> (byte) ((carried.getCount() + 1) / 2);
			default -> (byte) 1;
		};
		DeepPocketPacketHandler.sbPocketInsert(count);
		carried.shrink(count);
		menu.setCarried(carried);
		return true;
	}
	
	@Override
	public void mouseMoved(double mx, double my) {
		if (holdScroll)
			updateScroll(my);
	}
	
	@Override
	public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
		holdScroll = false;
		changeFocus(false);
		return true;
	}
	
	public @Nullable ItemTypeAmount getLastHoveredType() {
		return lastHoveredType;
	}
	
	private enum SlotType {
		OUTSIDE,
		NORMAL,
		CRAFT,
		CONVERT
	}
}
