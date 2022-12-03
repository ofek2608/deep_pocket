package com.ofek2608.deep_pocket.client.widget;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import com.ofek2608.deep_pocket.registry.MenuWithPocket;
import com.ofek2608.deep_pocket.registry.items.crafting_pattern.CraftingPatternItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class PatternResultWidget implements WidgetWithTooltip, GuiEventListener, NonNarratableEntry {
	private static final DeepPocketClientHelper HELPER = DeepPocketClientHelper.get();
	public int offX;
	public int offY;
	public AbstractContainerScreen<? extends MenuWithPocket> screen;
	private final PatternPartWidget input;
	private final PatternPartWidget output;
	
	private boolean hovered;
	
	public PatternResultWidget(AbstractContainerScreen<? extends MenuWithPocket> screen, PatternPartWidget input, PatternPartWidget output) {
		this.screen = screen;
		this.input = input;
		this.output = output;
	}
	
	@Override
	public void render(PoseStack poseStack, int mx, int my, float partialTick) {
		this.hovered = offX <= mx && mx < offX + 16 && offY <= my && my < offY + 16;
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, PatternWidget.TEXTURE);
		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		
		Screen.blit(
				poseStack,
				offX, offY,
				240, hovered ? 16 : 0,
				16, 16,
				256, 256
		);
		
		
		if (!isEmptyPattern()) {
			HELPER.renderItem(poseStack, offX, offY,
					canCreatePattern() ?
							CraftingPatternItem.createItem(input.items, output.items) :
							new ItemStack(DeepPocketRegistry.EMPTY_CRAFTING_PATTERN_ITEM.get()),
					Minecraft.getInstance().getItemRenderer(), Minecraft.getInstance().font
			);
		}
	}
	
	private boolean isEmptyPattern() {
		for (ItemTypeAmount output : output.items)
			if (!output.isEmpty())
				return false;
		return true;
	}
	
	private boolean canCreatePattern() {
		Pocket pocket = screen.getMenu().getPocket();
		return pocket != null && pocket.getMaxExtract(DeepPocketClientApi.get().getKnowledge(), new ItemType(DeepPocketRegistry.EMPTY_CRAFTING_PATTERN_ITEM.get())) != 0;
	}
	
	@Override
	public void renderTooltip(Screen screen, PoseStack poseStack, int mx, int my) {
		if (!hovered || isEmptyPattern())
			return;
		if (canCreatePattern())
			screen.renderTooltip(poseStack, CraftingPatternItem.createItem(input.items, output.items), mx, my);
		else
			screen.renderTooltip(poseStack, Component.literal("Missing Empty Crafting Patterns").withStyle(ChatFormatting.RED), mx, my);
	}
	
	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (!hovered)
			return false;
		if (button == InputConstants.MOUSE_BUTTON_LEFT && !isEmptyPattern() && canCreatePattern())
			DeepPocketPacketHandler.sbPatternCreate(input.items, output.items, !Screen.hasShiftDown());
		return true;
	}
}
