package com.ofek2608.deep_pocket.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.Pocket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class PocketTabWidget implements WidgetWithTooltip, GuiEventListener, NonNarratableEntry {
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/widget/pocket_tabs.png");
	public static final int TAB_W = 16;
	public static final int TAB_H = 16;
	public int offX;
	public int offY;
	private PocketTab currentTab = PocketTab.ITEMS;
	private PocketTab hoverTab = null;
	private final Supplier<Pocket> pocketSupplier;
	
	public PocketTabWidget(int offX, int offY, Supplier<Pocket> pocketSupplier) {
		this.offX = offX;
		this.offY = offY;
		this.pocketSupplier = pocketSupplier;
	}
	
	@Override
	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		Pocket pocket = pocketSupplier.get();
		if (!currentTab.isActive(pocket))
			currentTab = PocketTab.ITEMS;
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);
		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		
		Screen.blit(
				poseStack,
				offX, offY,
				112, 0,
				4, TAB_H,
				256, 256
		);
		hoverTab = null;
		int movingOffX = this.offX + 4;
		for (PocketTab tab : PocketTab.values()) {
			if (!tab.isActive(pocket))
				continue;
			
			int tabIndex = tab.ordinal();
			boolean hover = movingOffX <= mx && mx < movingOffX + TAB_W && offY <= my && my < offY + TAB_H;
			boolean current = tab == currentTab;
			if (hover)
				hoverTab = tab;
			
			Screen.blit(
					poseStack,
					movingOffX, offY,
					tabIndex * TAB_W + 4, current ? 2 * TAB_H : hover ? TAB_H : 0,
					TAB_W, TAB_H,
					256, 256
			);
			
			movingOffX += TAB_W;
		}
		
		//Background to the left of the tabs
		Screen.blit(
				poseStack,
				offX, offY,
				0, TAB_H * 3,
				4, TAB_H,
				256, 256
		);
		//Background to the right of the tabs
		Screen.blit(
				poseStack,
				movingOffX, offY,
				movingOffX - offX, TAB_H * 3,
				offX + 152 - movingOffX, TAB_H,
				256, 256
		);
		//Background below the tabs
		Screen.blit(
				poseStack,
				offX, offY + TAB_H,
				0, TAB_H * 4,
				152, 16,
				256, 256
		);
		Minecraft.getInstance().font.draw(poseStack, pocket.getName(), offX + 4, offY + TAB_H + 4, 0xFFFFFF);
	}
	
	@Override
	public void renderTooltip(Screen screen, PoseStack poseStack, int mx, int my) {
		if (hoverTab != null)
			screen.renderTooltip(poseStack, hoverTab.text, mx, my);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (hoverTab == null)
			return false;
		currentTab = hoverTab;
		DeepPocketUtils.playClickSound();
		return true;
	}
	
	public enum PocketTab {
		ITEMS("Items"),
		FLUIDS("Fluids"),
		POWER("Power"),
		CONVERSIONS("Conversions"),
		PATTERNS("Patterns"),
		CRAFTING("Crafting"),
		SETTINGS("Settings")
		;
		
		public final Component text;
		
		PocketTab(String text, ChatFormatting ... style) {
			this.text = Component.literal(text).withStyle(style);
		}
		
		boolean isActive(@Nullable Pocket pocket) {
			return true;
		}
	}
}
