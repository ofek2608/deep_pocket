package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.impl.DeepPocketManager;
import net.minecraft.client.Minecraft;

public interface DeepPocketClientHelper extends DeepPocketHelper {
	static DeepPocketClientHelper get() { return DeepPocketManager.getClientHelper(); }

	Minecraft getMinecraft();
}
