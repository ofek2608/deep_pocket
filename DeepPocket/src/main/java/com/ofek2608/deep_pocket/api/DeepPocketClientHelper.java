package com.ofek2608.deep_pocket.api;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.api.struct.ItemAmount;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import com.ofek2608.deep_pocket.impl.DeepPocketManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

public interface DeepPocketClientHelper extends DeepPocketHelper {
	static DeepPocketClientHelper get() { return DeepPocketManager.getClientHelper(); }

	Minecraft getMinecraft();

	void renderItem(PoseStack poseStack, int x, int y, ItemStack itemStack, ItemRenderer itemRenderer, Font font);
	void renderAmount(PoseStack poseStack, int x, int y, long amount, ItemRenderer itemRenderer, Font font);
	void renderItemAmount(PoseStack poseStack, int x, int y, ItemStack itemStack, long amount, ItemRenderer itemRenderer, Font font);
	void renderItemAmount(PoseStack poseStack, int x, int y, ItemAmount itemAmount, ItemRenderer itemRenderer, Font font);
	void renderItemAmount(PoseStack poseStack, int x, int y, ItemTypeAmount itemAmount, ItemRenderer itemRenderer, Font font);
}
