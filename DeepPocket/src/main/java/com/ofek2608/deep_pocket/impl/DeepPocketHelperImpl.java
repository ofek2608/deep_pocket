package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.*;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessManager;
import com.ofek2608.deep_pocket.api.struct.ItemConversions;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.PocketInfo;

import java.util.UUID;
import java.util.stream.Stream;

class DeepPocketHelperImpl implements DeepPocketHelper {
	@Override
	public Pocket createPocket(ItemConversions conversions, UUID pocketId, UUID owner, PocketInfo pocketInfo) {
		return new PocketImpl(conversions, pocketId, owner, pocketInfo, createProcessManager());
	}

	@Override
	public Knowledge createKnowledge(ItemConversions conversions) {
		return new KnowledgeImpl(conversions);
	}

	@Override
	public ProvidedResources createProvidedResources(ItemType[] types) {
		return new ProvidedResourcesImpl(types);
	}

	@Override
	public PocketProcessManager createProcessManager() {
		return new PocketProcessManagerImpl(this);
	}

	@Override
	public Stream<ItemType> getExtractableItems(Pocket pocket, Knowledge knowledge) {
		return Stream.concat(pocket.getItemsMap().keySet().stream(), knowledge.asSet().stream());
	}
}
