package com.ofek2608.deep_pocket.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import com.ofek2608.deep_pocket.api.struct.ItemAmount;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

final class DeepPocketClientHelperImpl extends DeepPocketHelperImpl implements DeepPocketClientHelper {
	private final Minecraft minecraft;

	DeepPocketClientHelperImpl() {
		this.minecraft = Minecraft.getInstance();
	}

	@Override
	public Minecraft getMinecraft() {
		return minecraft;
	}



	@Override
	public void renderItem(PoseStack poseStack, int x, int y, ItemStack itemStack, ItemRenderer itemRenderer, Font font) {
		if (itemStack.isEmpty())
			return;
		itemRenderer.renderAndDecorateItem(itemStack, x, y);
		itemRenderer.renderGuiItemDecorations(font, itemStack, x, y, null);
	}

	@Override
	public void renderAmount(PoseStack poseStack, int x, int y, long amount, ItemRenderer itemRenderer, Font font) {
		if (amount == 1)
			return;
		poseStack.pushPose();
		String displayText = DeepPocketUtils.advancedToString(amount);
		poseStack.translate(0.0D, 0.0D, itemRenderer.blitOffset + 200);
		poseStack.scale(0.5f, 0.5f, 1f);
		font.draw(poseStack, displayText, x * 2 + 32 - font.width(displayText), y * 2 + 24, 0xFFFFFF);
		poseStack.popPose();
	}

	@Override
	public void renderItemAmount(PoseStack poseStack, int x, int y, ItemStack itemStack, long amount, ItemRenderer itemRenderer, Font font) {
		if (itemStack.isEmpty() || amount == 0)
			return;
		renderItem(poseStack, x, y, itemStack, itemRenderer, font);
		renderAmount(poseStack, x, y, amount, itemRenderer, font);
	}

	@Override
	public void renderItemAmount(PoseStack poseStack, int x, int y, ItemAmount itemAmount, ItemRenderer itemRenderer, Font font) {
		renderItemAmount(poseStack, x, y, new ItemStack(itemAmount.getItem()), itemAmount.getAmount(), itemRenderer, font);
	}

	@Override
	public void renderItemAmount(PoseStack poseStack, int x, int y, ItemTypeAmount itemAmount, ItemRenderer itemRenderer, Font font) {
		renderItemAmount(poseStack, x, y, itemAmount.getItemType().create(), itemAmount.getAmount(), itemRenderer, font);
	}
}