package com.ofek2608.deep_pocket.client.widget;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.ElementTypeStack;
import com.ofek2608.deep_pocket.api.struct.client.ClientPocket;
import com.ofek2608.deep_pocket.client.client_screens.ClientScreens;
import com.ofek2608.deep_pocket.registry.MenuWithPocket;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

public class PatternPartWidget implements WidgetWithTooltip, GuiEventListener, NonNarratableEntry {
	private static final DeepPocketClientHelper HELPER = DeepPocketClientHelper.get();
	public int offX, offY;
	public final ElementTypeStack[] items = new ElementTypeStack[9];
	private int hoveredIndex;
	public AbstractContainerScreen<? extends MenuWithPocket> screen;
	
	public PatternPartWidget(AbstractContainerScreen<? extends MenuWithPocket> screen) {
		this.screen = screen;
		clear();
	}
	
	public void clear() {
		Arrays.fill(items, ElementTypeStack.empty());
	}
	
	@Override
	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		hoveredIndex = -1;
		if (offX <= mx && mx < offX + 48 && offY <= my && my < offY + 48)
			hoveredIndex = (mx - offX) / 16 + (my - offY) / 16 * 3;
		
		int slotIndex;
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, PatternWidget.TEXTURE);
		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		
		slotIndex = 0;
		for (int slotY = 0; slotY < 3; slotY++) {
			for (int slotX = 0; slotX < 3; slotX++) {
				Screen.blit(
						poseStack,
						offX + slotX * 16, offY + slotY * 16,
						240, slotIndex++ == hoveredIndex ? 16 : 0,
						16, 16,
						256, 256
				);
			}
		}
		
		Font font = Minecraft.getInstance().font;
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		
		slotIndex = 0;
		for (int slotY = 0; slotY < 3; slotY++)
			for (int slotX = 0; slotX < 3; slotX++)
				HELPER.renderElementTypeStack(poseStack, offX + 16 * slotX, offY + 16 * slotY, items[slotIndex++], itemRenderer, font);
	}
	
	@Override
	public void renderTooltip(Screen screen, PoseStack poseStack, int mx, int my) {
		if (hoveredIndex < 0 || 9 <= hoveredIndex)
			return;
		HELPER.renderElementTypeTooltip(poseStack, mx, my, items[hoveredIndex], screen);
	}
	
	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (hoveredIndex < 0 || 9 <= hoveredIndex)
			return false;
		int clickedSlot = hoveredIndex;
		ItemStack carried = screen.getMenu().getCarried();
		boolean control = Screen.hasControlDown();
		boolean shift = Screen.hasShiftDown();
		
		if (shift || !control) {
			if (shift)
				carried = ItemStack.EMPTY;
			long count = carried.isEmpty() ? 0 : button == InputConstants.MOUSE_BUTTON_LEFT ? carried.getCount() : 1;
			items[clickedSlot] = ElementTypeStack.of(ElementType.item(carried), count);
			return true;
		}
		long currentAmount = items[clickedSlot].getCount();
		if (currentAmount == 0)
			return true;
		ClientPocket pocket = screen.getMenu().getClientPocket();
		ClientScreens.selectNumber(Component.literal("Select Amount"), pocket == null ? 0 : pocket.getColor(), currentAmount, newAmount->{
			items[clickedSlot] = ElementTypeStack.of(items[clickedSlot].getType(), newAmount);
			Minecraft.getInstance().setScreen(screen);
		});
		return true;
	}
}
