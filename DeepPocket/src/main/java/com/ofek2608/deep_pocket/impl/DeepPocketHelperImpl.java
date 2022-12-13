package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.*;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.pocket.PocketContent;
import com.ofek2608.deep_pocket.api.pocket.PocketPatterns;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessManager;
import com.ofek2608.deep_pocket.api.struct.*;

import java.util.UUID;

class DeepPocketHelperImpl implements DeepPocketHelper {
	@Override
	public Pocket createPocket(ItemConversions conversions, ElementConversions conversions0, UUID pocketId, UUID owner, PocketInfo pocketInfo) {
		return new PocketImpl(this, conversions, conversions0, pocketId, owner, pocketInfo, createProcessManager());
	}
	
	@Override
	public PocketContent createPocketContent() {
		return new PocketContentImpl();
	}
	
	@Override
	public PocketPatterns createPocketPatterns() {
		return new PocketPatternsImpl();
	}
	
	@Override
	public Knowledge createKnowledge(ElementConversions conversions) {
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
}
