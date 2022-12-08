package com.ofek2608.deep_pocket.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.ElementTypeStack;
import com.ofek2608.deep_pocket.registry.MenuWithPocket;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class PatternWidget extends SimpleContainerWidget {
	public static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/widget/pattern.png");
	private final PatternPartWidget inputWidget, outputWidget;
	private final PatternResultWidget resultWidget;
	private final ImageButton clearWidget;
	
	public PatternWidget(AbstractContainerScreen<? extends MenuWithPocket> screen) {
		children.add(inputWidget = new PatternPartWidget(screen));
		children.add(outputWidget = new PatternPartWidget(screen));
		children.add(resultWidget = new PatternResultWidget(screen, inputWidget, outputWidget));
		children.add(clearWidget = new ImageButton(
				0, 0, 16, 16, 224, 0,
				TEXTURE, button -> clearPattern()
		));
	}
	
	@Override
	protected void updatePositions() {
		inputWidget.offX = offX + 22;
		inputWidget.offY = offY;
		outputWidget.offX = offX + 82;
		outputWidget.offY = offY;
		resultWidget.offX = offX + 132;
		resultWidget.offY = offY + 16;
		clearWidget.x = offX + 4;
		clearWidget.y = offY + 16;
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
		super.render(poseStack, mx, my, partialTick);
	}
	
	@Override
	public void renderTooltip(Screen screen, PoseStack poseStack, int mx, int my) {
		inputWidget.renderTooltip(screen, poseStack, mx, my);
		outputWidget.renderTooltip(screen, poseStack, mx, my);
		resultWidget.renderTooltip(screen, poseStack, mx, my);
		if (clearWidget.isMouseOver(mx, my))
			screen.renderTooltip(poseStack, Component.literal("Clear"), mx, my);
	}
	
	public void clearPattern() {
		inputWidget.clear();
		outputWidget.clear();
	}
	
	public void setPattern(ElementType[] inputItems, ItemStack outputItems) {
		clearPattern();
		int inputLen = Math.min(inputItems.length, 9);
		for (int i = 0; i < inputLen; i++)
			inputWidget.items[i] = ElementTypeStack.of(inputItems[i]);
		outputWidget.items[0] = ElementTypeStack.of(ElementType.item(outputItems), outputItems.getCount());
	}
	
	public Rect2i getJEITargetArea(int targetIndex) {
		int x = offX + (targetIndex % 3) * 16 + (targetIndex < 9 ? 22 : 82);
		int y = offY + ((targetIndex / 3) % 3) * 16;
		return new Rect2i(x, y, 16, 16);
	}
	
	public void acceptJEIGhostIngredient(int targetIndex, ItemStack ghostIngredient) {
		if (0 <= targetIndex && targetIndex < 9) {
			inputWidget.items[targetIndex] = ElementTypeStack.of(ElementType.item(ghostIngredient), ghostIngredient.getCount());
		} else if (9 <= targetIndex && targetIndex < 18) {
			outputWidget.items[targetIndex - 9] = ElementTypeStack.of(ElementType.item(ghostIngredient), ghostIngredient.getCount());
		}
	}
}
