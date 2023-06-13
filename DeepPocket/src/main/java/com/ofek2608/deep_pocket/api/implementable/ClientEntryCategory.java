package com.ofek2608.deep_pocket.api.implementable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.api.types.EntryStack;

public interface ClientEntryCategory {
	void render(EntryStack entryStack, int x, int y);
	void render(EntryStack entryStack, PoseStack poseStack);
	
	//TODO hover text
}
