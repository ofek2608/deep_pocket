package com.ofek2608.deep_pocket.client.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;

public interface WidgetWithTooltip extends Widget {
	void renderTooltip(Screen screen, PoseStack poseStack, int mx, int my);
}
