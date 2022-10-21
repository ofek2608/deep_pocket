package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.*;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessManager;
import com.ofek2608.deep_pocket.api.struct.*;

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
	public Stream<ItemType> getCraftableItems(Pocket pocket) {
		return pocket.getPatternsMap().values().stream()
						.map(CraftingPattern::getOutput)
						.flatMap(Stream::of)
						.filter(typeAmount->!typeAmount.isEmpty())
						.map(ItemTypeAmount::getItemType)
						.unordered()
						.distinct();
	}

	@Override
	public Stream<ItemType> getExtractableItems(Pocket pocket, Knowledge knowledge) {
		return Stream.concat(pocket.getItemsMap().keySet().stream(), knowledge.asSet().stream());
	}

	@Override
	public Stream<ItemType> getExtractableOrCraftableItems(Pocket pocket, Knowledge knowledge) {
		return Stream.concat(getExtractableItems(pocket, knowledge), getCraftableItems(pocket)).unordered().distinct();
	}
}
