package com.ofek2608.deep_pocket.client.widget;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.client.ClientPocket;
import com.ofek2608.deep_pocket.client.client_screens.ClientScreens;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class PocketWidget implements WidgetWithTooltip, GuiEventListener, NonNarratableEntry {
	private static final DeepPocketClientHelper HELPER = DeepPocketClientHelper.get();
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/widget/pocket.png");
	private final AbstractContainerScreen<?> screen;
	public int offX;
	public int offY;
	public int height;
	public float targetScroll;
	public float displayedScroll;
	public Supplier<ClientPocket> pocketSupplier;
	
	private int maxScroll;
	private boolean holdCraft;
	private boolean holdScroll;
	private boolean hoverScroll;
	private boolean insideContainer;
	private @Nullable Pocket.Entry hoveredEntry;
	
	
	public PocketWidget(AbstractContainerScreen<?> screen, Supplier<ClientPocket> pocketSupplier) {
		this.screen = screen;
		this.pocketSupplier = pocketSupplier;
	}
	
	@Override
	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		poseStack.pushPose();
		
		hoveredEntry = null;
		hoverScroll = false;
		if (height <= 0)
			return;
		ClientPocket pocket = pocketSupplier.get();
		if (pocket == null)
			return;
		
		List<Pocket.Entry> types = pocket.entries()
				.sorted(HELPER.getSearchComparator())
				.filter(HELPER.getSearchFilter())
				.toList();
		
		mx -= offX;
		my -= offY;
		
		maxScroll = Math.max(((types.size() - 1) / 9 + 1) * 16 - height, 0);
		targetScroll = Math.max(Math.min(targetScroll, maxScroll), 0);
		
		if (Math.abs(displayedScroll - targetScroll) > 5)
			displayedScroll = displayedScroll * 0.8f + targetScroll * 0.2f;
		else if (Math.abs(displayedScroll - targetScroll) > 1f)
			displayedScroll += displayedScroll > targetScroll ? -1 : 1;
		else
			displayedScroll = targetScroll;
		
		holdCraft = Screen.hasControlDown();
		hoverScroll = 152 <= mx && mx < 160 && 0 <= my && my < height;
		insideContainer = 4 <= mx && mx < 148 && 0 <= my && my < height;
		int hoverSlotIndex = -1;
		if (insideContainer) {
			int hoverSlotX = (mx - 4) / 16;
			int hoverSlotY = (int)(my + displayedScroll) / 16;
			hoverSlotIndex = hoverSlotX + 9 * hoverSlotY;
		}
		hoveredEntry = 0 <= hoverSlotIndex && hoverSlotIndex < types.size() ? types.get(hoverSlotIndex) : null;
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);
		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		
		blitOutline(poseStack, offX, offY, height);
		blitScroll(poseStack, offX, offY, displayedScroll, maxScroll, height, hoverScroll);
		
		GuiComponent.enableScissor(offX + 4, offY, offX + 148, offY + height);
		
		int slotsOffX = offX + 4;
		int slotsOffY = (int) (offY - displayedScroll);
		int firstRow = (int) displayedScroll / 16;
		int lastRow = (int)(displayedScroll + height) / 16;
		
		
		blitSlotRange(poseStack, types, slotsOffX, slotsOffY, firstRow, lastRow, hoverSlotIndex);
		renderTypeRange(poseStack, types, slotsOffX, slotsOffY, firstRow - 1, lastRow + 1);
		
		GuiComponent.disableScissor();
	}
	
	@Override
	public void renderTooltip(Screen screen, PoseStack poseStack, int mx, int my) {
		if (hoveredEntry != null)
			HELPER.renderElementTypeTooltip(poseStack, mx, my, hoveredEntry, screen);
	}
	
	private void blitSlotRange(PoseStack poseStack, List<Pocket.Entry> entries, int offX, int offY, int firstRow, int lastRow, int hoverIndex) {
		int slotIndex = firstRow * 9;
		for (int slotY = firstRow; slotY <= lastRow; slotY++) {
			for (int slotX = 0; slotX < 9; slotX++) {
				blitSlot(poseStack, slotX * 16 + offX, slotY * 16 + offY, getSlotType(entries, slotIndex), slotIndex == hoverIndex);
				slotIndex++;
			}
		}
	}
	
	private SlotType getSlotType(List<Pocket.Entry> entries, int index) {
		if (index < 0 || entries.size() <= index)
			return SlotType.OUTSIDE;
		Pocket.Entry entry = entries.get(index);
		if (entry.canBeConverted())
			return SlotType.CONVERT;
		if (entry.canBeCrafted())
			return SlotType.CRAFT;
		return SlotType.NORMAL;
	}
	
	private void renderTypeRange(PoseStack poseStack, List<Pocket.Entry> entries, int offX, int offY, int firstRow, int lastRow) {
		int slotIndex = firstRow * 9;
		for (int slotY = firstRow; slotY <= lastRow; slotY++) {
			for (int slotX = 0; slotX < 9; slotX++) {
				if (0 <= slotIndex && slotIndex < entries.size())
					renderType(poseStack, slotX * 16 + offX, slotY * 16 + offY, entries.get(slotIndex), holdCraft);
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
			PoseStack poseStack,
			int offX, int offY,
			Pocket.Entry entry, boolean holdCraft
	) {
		Minecraft minecraft = Minecraft.getInstance();
		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		Font font = minecraft.font;
		
		HELPER.renderPocketEntry(
				poseStack,
				offX, offY,
				entry,
				isCrafting(entry, holdCraft) ? "craft" : null,
				itemRenderer, font
		);
	}
	
	private static boolean isCrafting(Pocket.Entry entry, boolean holdCraft) {
		return entry.canBeCrafted() && (holdCraft || entry.getMaxExtract() == 0);
	}
	
	
	
	
	private void updateScroll(double my) {
		targetScroll = (float) (maxScroll * (my - offY - 1) / (height - 2));
		displayedScroll = targetScroll;
	}
	
	@Override
	public boolean mouseScrolled(double mx, double my, double delta) {
		targetScroll -= delta * 16;
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
		ClientPocket pocket = pocketSupplier.get();
		if (pocket == null)
			return false;
		
		ElementType clickedType = hoveredEntry == null ? null : hoveredEntry.getType();
		if (hoveredEntry != null && isCrafting(hoveredEntry, holdCraft)) {
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
		if (clickedType instanceof ElementType.TItem clickedItem && (carried.isEmpty() || clickedItem.is(carried))) {
			byte count = switch (button) {
				case InputConstants.MOUSE_BUTTON_LEFT -> (byte) 64;
				default -> (byte) 1;
				case InputConstants.MOUSE_BUTTON_RIGHT -> (byte) 32;
			};
			DeepPocketPacketHandler.sbPocketExtract(clickedItem, !Screen.hasShiftDown(), count);
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
	
	public ItemStack getHoveredItem() {
		return hoveredEntry != null && hoveredEntry.getType() instanceof ElementType.TItem item ? item.create() : ItemStack.EMPTY;
	}
	
	public FluidStack getHoveredFluid() {
		return hoveredEntry != null && hoveredEntry.getType() instanceof ElementType.TFluid fluid ? fluid.create(1000) : FluidStack.EMPTY;
	}
	
	private enum SlotType {
		OUTSIDE,
		NORMAL,
		CRAFT,
		CONVERT
	}
}
