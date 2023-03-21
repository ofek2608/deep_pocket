package com.ofek2608.deep_pocket.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.struct.client.ClientPocket;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class PocketSearchWidget extends SearchWidget {
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/widget/pocket_search.png");
	public final PocketWidget pocketWidget;
	
	public PocketSearchWidget(AbstractContainerScreen<?> screen, Supplier<ClientPocket> pocketSupplier) {
		children.add(pocketWidget = new PocketWidget(screen, pocketSupplier));
	}
	
	public void setHeight(int height) {
		pocketWidget.height = height - 14;
	}
	
	public int getHeight() {
		return pocketWidget.height + 14;
	}
	
	@Override
	protected void updatePositions() {
		super.updatePositions();
		pocketWidget.offX = offX;
		pocketWidget.offY = offY + 14;
	}
	
	@Override
	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);
		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		Screen.blit(
				poseStack,
				offX, offY + 10,
				0, 0,
				164, 4,
				256, 256
		);
		
		super.render(poseStack, mx, my, partialTick);
	}
	
	@Override
	public void renderTooltip(Screen screen, PoseStack poseStack, int mx, int my) {
		super.renderTooltip(screen, poseStack, mx, my);
		pocketWidget.renderTooltip(screen, poseStack, mx, my);
	}
	
	@Override
	public boolean mouseScrolled(double mx, double my, double delta) {
		return pocketWidget.mouseScrolled(mx, my, delta);
	}
	
	@Override
	public void mouseMoved(double mx, double my) {
		pocketWidget.mouseMoved(mx, my);
	}
	
	
	@Override
	public boolean mouseReleased(double mx, double my, int button) {
		return pocketWidget.mouseReleased(mx, my, button);
	}
}
