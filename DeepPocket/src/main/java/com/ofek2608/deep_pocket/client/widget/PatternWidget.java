package com.ofek2608.deep_pocket.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import com.ofek2608.deep_pocket.registry.MenuWithPocket;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PatternWidget implements WidgetWithTooltip, ContainerEventHandler, NonNarratableEntry {
	public static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/widget/pattern.png");
	private int offX, offY;
	private boolean dragging;
	private GuiEventListener focused;
	private final PatternPartWidget inputWidget, outputWidget;
	private final PatternResultWidget resultWidget;
	private final ImageButton clearWidget;
	private final List<GuiEventListener> children;
	
	public PatternWidget(AbstractContainerScreen<? extends MenuWithPocket> screen) {
		inputWidget = new PatternPartWidget(screen);
		outputWidget = new PatternPartWidget(screen);
		resultWidget = new PatternResultWidget(screen, inputWidget, outputWidget);
		clearWidget = new ImageButton(
				0, 0, 16, 16, 224, 0,
				TEXTURE, button -> clearPattern()
		);
		this.children = List.of(inputWidget, outputWidget, resultWidget, clearWidget);
	}
	
	public void setPos(int x, int y) {
		offX = x;
		offY = y;
		inputWidget.offX = x + 22;
		inputWidget.offY = y;
		outputWidget.offX = x + 82;
		outputWidget.offY = y;
		resultWidget.offX = x + 132;
		resultWidget.offY = y + 16;
		clearWidget.x = x + 4;
		clearWidget.y = y + 16;
	}
	
	@Override
	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, PatternWidget.TEXTURE);
		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		Screen.blit(
				poseStack,
				offX, offY,
				0, 0,
				152, 48,
				256, 256
		);
		for (GuiEventListener child : children)
			if (child instanceof Widget widget)
				widget.render(poseStack, mx, my, partialTick);
	}
	
	@Override
	public void renderTooltip(Screen screen, PoseStack poseStack, int mx, int my) {
		for (GuiEventListener child : children)
			if (child instanceof WidgetWithTooltip tooltip)
				tooltip.renderTooltip(screen, poseStack, mx, my);
	}
	
	@Override
	public List<? extends GuiEventListener> children() {
		return children;
	}
	
	@Override
	public boolean isDragging() {
		return dragging;
	}
	
	@Override
	public void setDragging(boolean dragging) {
		this.dragging = dragging;
	}
	
	@Nullable
	@Override
	public GuiEventListener getFocused() {
		return focused;
	}
	
	@Override
	public void setFocused(@Nullable GuiEventListener focused) {
		this.focused = focused;
	}
	
	public void clearPattern() {
		inputWidget.clear();
		outputWidget.clear();
	}
	
	public void setPattern(ItemType[] inputItems, ItemStack outputItems) {
		clearPattern();
		int inputLen = Math.min(inputItems.length, 9);
		for (int i = 0; i < inputLen; i++)
			inputWidget.items[i] = new ItemTypeAmount(inputItems[i], 1);
		outputWidget.items[0] = new ItemTypeAmount(new ItemType(outputItems), outputItems.getCount());
	}
	
	public Rect2i getJEITargetArea(int targetIndex) {
		int x = offX + (targetIndex % 3) * 16 + (targetIndex < 9 ? 22 : 82);
		int y = offY + ((targetIndex / 3) % 3) * 16;
		return new Rect2i(x, y, 16, 16);
	}
	
	public void acceptJEIGhostIngredient(int targetIndex, ItemStack ghostIngredient) {
		if (0 <= targetIndex && targetIndex < 9) {
			inputWidget.items[targetIndex] = new ItemTypeAmount(new ItemType(ghostIngredient), ghostIngredient.getCount());
		} else if (9 <= targetIndex && targetIndex < 18) {
			outputWidget.items[targetIndex - 9] = new ItemTypeAmount(new ItemType(ghostIngredient), ghostIngredient.getCount());
		}
	}
}
