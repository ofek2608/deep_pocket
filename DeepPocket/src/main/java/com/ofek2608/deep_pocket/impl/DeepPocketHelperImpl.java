package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.DeepPocketHelper;
import com.ofek2608.deep_pocket.api.PlayerKnowledge;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.struct.ItemConversions;
import com.ofek2608.deep_pocket.api.struct.PocketInfo;

import java.util.UUID;

class DeepPocketHelperImpl implements DeepPocketHelper {
	@Override
	public Pocket createPocket(ItemConversions conversions, UUID pocketId, UUID owner, PocketInfo pocketInfo) {
		return new PocketImpl(conversions, pocketId, owner, pocketInfo);
	}

	@Override
	public PlayerKnowledge createKnowledge(ItemConversions conversions) {
		return new PlayerKnowledgeImpl(conversions);
	}
}
