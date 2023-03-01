package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.struct.ElementConversions;
import com.ofek2608.deep_pocket.api.struct.ElementConversionsOld;
import com.ofek2608.deep_pocket.api.struct.client.ClientElementIndices;
import com.ofek2608.deep_pocket.api.struct.client.ClientPocket;
import com.ofek2608.deep_pocket.impl.DeepPocketManager;

public interface DeepPocketClientApi extends DeepPocketApi<ClientPocket> {
	static DeepPocketClientApi get() { return DeepPocketManager.getClientApi(); }


	@Override DeepPocketClientHelper getHelper();
	ClientElementIndices getElementIndices();

	void setConversions(ElementConversions conversions);
	//server config
	boolean isPermitPublicPocket();
	void setPermitPublicPocket(boolean value);

	Knowledge getKnowledge();
	
}
