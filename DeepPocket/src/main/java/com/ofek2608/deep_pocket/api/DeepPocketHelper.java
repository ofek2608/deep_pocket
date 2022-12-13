package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessManager;
import com.ofek2608.deep_pocket.api.struct.ElementConversions;
import com.ofek2608.deep_pocket.api.struct.ItemConversions;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.PocketInfo;
import com.ofek2608.deep_pocket.impl.DeepPocketManager;

import java.util.UUID;
import java.util.stream.Stream;

public interface DeepPocketHelper {
	static DeepPocketHelper get() { return DeepPocketManager.getHelper(); }

	//Constructions
	Pocket createPocket(ItemConversions conversions, ElementConversions conversions0, UUID pocketId, UUID owner, PocketInfo pocketInfo);
	public PocketContent createPocketContent();
	Knowledge createKnowledge(ItemConversions conversions);
	Knowledge0 createKnowledge(ElementConversions conversions);
	ProvidedResources createProvidedResources(ItemType[] types);
	PocketProcessManager createProcessManager();

	Stream<ItemType> getCraftableItems(Pocket pocket);
	Stream<ItemType> getExtractableItems(Pocket pocket, Knowledge knowledge);
	Stream<ItemType> getExtractableOrCraftableItems(Pocket pocket, Knowledge knowledge);
}
