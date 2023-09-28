package com.ofek2608.deep_pocket.api.implementable;

import com.ofek2608.deep_pocket.api.types.EntryStack;
import net.minecraft.client.gui.GuiGraphics;

public interface ClientEntryCategory {
	void render(GuiGraphics graphics, EntryStack entryStack, int x, int y);
	
	//TODO hover text
}
