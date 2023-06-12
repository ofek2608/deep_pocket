package com.ofek2608.deep_pocket.def.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public final class GuiUtils {
	private GuiUtils() {}
	
	public static void playClickSound() {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}
	
	public static void setShaderColor(int color) {
		RenderSystem.setShaderColor(
				(float)((color >> 16) & 0xFF) / 0xFF,
				(float)((color >> 8) & 0xFF) / 0xFF,
				(float)(color & 0xFF) / 0xFF,
				1
		);
	}
	
	public static void setShaderTransparentColor(int color) {
		RenderSystem.setShaderColor(
				(float)((color >> 16) & 0xFF) / 0xFF,
				(float)((color >> 8) & 0xFF) / 0xFF,
				(float)(color & 0xFF) / 0xFF,
				(float)(color >> 24) / 0xFF
		);
	}
}
