package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.enums.PocketDisplayMode;
import com.ofek2608.deep_pocket.api.enums.SearchMode;
import com.ofek2608.deep_pocket.api.enums.SortingOrder;
import com.ofek2608.deep_pocket.api.struct.*;
import com.ofek2608.deep_pocket.impl.DeepPocketManager;

import java.util.stream.Stream;

public interface DeepPocketClientApi extends DeepPocketApi {
	static DeepPocketClientApi get() { return DeepPocketManager.getClientApi(); }


	@Override DeepPocketClientHelper getHelper();

	void setItemConversions(ItemConversions conversions);
	//server config
	boolean isPermitPublicPocket();
	void setPermitPublicPocket(boolean value);

	Knowledge0 getKnowledge0();
	Knowledge getKnowledge();
}
