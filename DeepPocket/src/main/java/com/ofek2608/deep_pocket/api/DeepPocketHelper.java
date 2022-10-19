package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.struct.ItemConversions;
import com.ofek2608.deep_pocket.api.struct.Pocket;
import com.ofek2608.deep_pocket.api.struct.PocketInfo;

import java.util.UUID;

public interface DeepPocketHelper {
	Pocket createPocket(ItemConversions conversions, UUID pocketId, UUID owner, PocketInfo pocketInfo);
}
