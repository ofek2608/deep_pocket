package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import net.minecraft.client.Minecraft;

final class DeepPocketClientHelperImpl extends DeepPocketHelperImpl implements DeepPocketClientHelper {
	private final Minecraft minecraft;

	DeepPocketClientHelperImpl() {
		this.minecraft = Minecraft.getInstance();
	}

	@Override
	public Minecraft getMinecraft() {
		return minecraft;
	}
}
