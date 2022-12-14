package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.*;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.pocket.PocketContent;
import com.ofek2608.deep_pocket.api.pocket.PocketPatterns;
import com.ofek2608.deep_pocket.api.pocket_process.old.PocketProcessManager;
import com.ofek2608.deep_pocket.api.struct.*;

import java.util.UUID;

class DeepPocketHelperImpl implements DeepPocketHelper {
	@Override
	public Pocket createPocket(ElementConversions conversions, UUID pocketId, UUID owner, PocketInfo pocketInfo) {
		return new PocketImpl(this, conversions, pocketId, owner, pocketInfo, createProcessManager());
	}
	
	@Override
	public PocketContent createPocketContent(ElementConversions conversions) {
		return new PocketContentImpl(conversions);
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
	public ProvidedResources createProvidedResources(ElementType[] types) {
		return new ProvidedResourcesImpl(types);
	}

	@Override
	public PocketProcessManager createProcessManager() {
		return new PocketProcessManagerImpl(this);
	}
}
