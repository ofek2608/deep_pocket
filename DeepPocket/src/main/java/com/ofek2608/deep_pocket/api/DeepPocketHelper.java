package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.pocket.PocketContent;
import com.ofek2608.deep_pocket.api.pocket.PocketPatterns;
import com.ofek2608.deep_pocket.api.pocket_process.old.PocketProcessManager;
import com.ofek2608.deep_pocket.api.struct.ElementConversionsOld;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.PocketInfo;
import com.ofek2608.deep_pocket.impl.DeepPocketManager;

import java.util.UUID;

public interface DeepPocketHelper {
	static DeepPocketHelper get() { return DeepPocketManager.getHelper(); }

	//Constructions
	Pocket createPocket(ElementConversionsOld conversions, UUID pocketId, UUID owner, PocketInfo pocketInfo);
	PocketContent createPocketContent(ElementConversionsOld conversions);
	PocketPatterns createPocketPatterns();
	Knowledge createKnowledge(ElementConversionsOld conversions);
	ProvidedResources createProvidedResources(ElementType[] types);
	PocketProcessManager createProcessManager();
}