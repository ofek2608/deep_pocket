package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.struct.ItemConversions;
import com.ofek2608.deep_pocket.api.struct.PocketInfo;
import com.ofek2608.deep_pocket.impl.DeepPocketManager;

import java.util.UUID;

public interface DeepPocketHelper {
	static DeepPocketHelper get() { return DeepPocketManager.getHelper(); }

	Pocket createPocket(ItemConversions conversions, UUID pocketId, UUID owner, PocketInfo pocketInfo);
	PlayerKnowledge createKnowledge(ItemConversions conversions);
}
