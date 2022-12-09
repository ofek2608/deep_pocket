package com.ofek2608.deep_pocket.client.widget;

import com.mojang.blaze3d.vertex.PoseStack;

public interface MenuItemRenderer {
	void renderItem(PoseStack poseStack, int x, int y, int slotIndex);
}
