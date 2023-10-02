package com.ofek2608.deep_pocket.api.implementable;

import com.ofek2608.deep_pocket.api.DPClientAPI;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import net.minecraft.client.player.LocalPlayer;

public interface PocketTabDefinition {
	boolean isVisible(DPClientAPI api, LocalPlayer player, Pocket pocket);
	TabContentWidget createWidget(DPClientAPI api, LocalPlayer player, Pocket pocket);
}
