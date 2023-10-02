package com.ofek2608.deep_pocket.api.implementable;

import com.ofek2608.deep_pocket.api.types.EntryStack;
import com.ofek2608.deep_pocket.api.types.EntryType;
import net.minecraft.client.gui.GuiGraphics;

public interface ClientEntryCategory {
	void render(GuiGraphics graphics, EntryStack entryStack, int x, int y);
	
	default void render(GuiGraphics graphics, EntryType entryStack, int x, int y) {
		render(graphics, new EntryStack(entryStack), x, y);
	}
	
	//TODO hover text
}
