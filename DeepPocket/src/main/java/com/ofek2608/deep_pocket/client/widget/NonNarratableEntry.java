package com.ofek2608.deep_pocket.client.widget;

import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;

/**
 * Yep, im lazy to narrate everything
 */
public interface NonNarratableEntry extends NarratableEntry {
	@Override default NarrationPriority narrationPriority() { return NarrationPriority.NONE; }
	@Override default void updateNarration(NarrationElementOutput output) { }
}
