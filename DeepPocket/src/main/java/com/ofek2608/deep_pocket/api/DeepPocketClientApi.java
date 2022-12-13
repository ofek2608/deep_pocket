package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.struct.ElementConversions;
import com.ofek2608.deep_pocket.api.struct.ItemConversions;
import com.ofek2608.deep_pocket.impl.DeepPocketManager;

public interface DeepPocketClientApi extends DeepPocketApi {
	static DeepPocketClientApi get() { return DeepPocketManager.getClientApi(); }


	@Override DeepPocketClientHelper getHelper();

	void setItemConversions(ElementConversions conversions);
	//server config
	boolean isPermitPublicPocket();
	void setPermitPublicPocket(boolean value);

	Knowledge0 getKnowledge();
}
