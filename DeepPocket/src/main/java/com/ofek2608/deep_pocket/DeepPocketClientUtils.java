package com.ofek2608.deep_pocket;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.ofek2608.deep_pocket.api.struct.ItemAmount;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

public final class DeepPocketClientUtils {
	private DeepPocketClientUtils() {}

	public static void renderItem(PoseStack poseStack, int x, int y, ItemStack itemStack, ItemRenderer itemRenderer, Font font) {
		if (itemStack.isEmpty())
			return;
		itemRenderer.renderAndDecorateItem(itemStack, x, y);
		itemRenderer.renderGuiItemDecorations(font, itemStack, x, y, null);
	}

	public static void renderAmount(PoseStack poseStack, int x, int y, long amount, ItemRenderer itemRenderer, Font font) {
		if (amount == 1)
			return;
		poseStack.pushPose();
		String displayText = DeepPocketUtils.advancedToString(amount);
		poseStack.translate(0.0D, 0.0D, itemRenderer.blitOffset + 200);
		poseStack.scale(0.5f, 0.5f, 1f);
		MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		font.drawInBatch(displayText, (float)(x * 2 + 32 - font.width(displayText)), (float)(y * 2 + 24), 0xFFFFFF, true, poseStack.last().pose(), bufferSource, false, 0, 0xF000F0);
		bufferSource.endBatch();
		poseStack.popPose();
	}

	public static void renderItemAmount(PoseStack poseStack, int x, int y, ItemStack itemStack, long amount, ItemRenderer itemRenderer, Font font) {
		if (itemStack.isEmpty() || amount == 0)
			return;
		renderItem(poseStack, x, y, itemStack, itemRenderer, font);
		renderAmount(poseStack, x, y, amount, itemRenderer, font);
	}

	public static void renderItemAmount(PoseStack poseStack, int x, int y, ItemAmount itemAmount, ItemRenderer itemRenderer, Font font) {
		renderItemAmount(poseStack, x, y, new ItemStack(itemAmount.getItem()), itemAmount.getAmount(), itemRenderer, font);
	}

	public static void renderItemAmount(PoseStack poseStack, int x, int y, ItemTypeAmount itemAmount, ItemRenderer itemRenderer, Font font) {
		renderItemAmount(poseStack, x, y, itemAmount.getItemType().create(), itemAmount.getAmount(), itemRenderer, font);
	}
}
