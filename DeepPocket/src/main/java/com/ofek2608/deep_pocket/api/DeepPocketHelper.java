package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.pocket_process.old.PocketProcessManager;
import com.ofek2608.deep_pocket.api.struct.ElementConversions;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.impl.DeepPocketManager;

public interface DeepPocketHelper {
	static DeepPocketHelper get() { return DeepPocketManager.getHelper(); }

	//Constructions
	Knowledge createKnowledge(ElementConversions conversions);
	ProvidedResources createProvidedResources(ElementType[] types);
	PocketProcessManager createProcessManager();
}