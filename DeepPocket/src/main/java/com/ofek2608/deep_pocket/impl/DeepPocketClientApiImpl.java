package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import com.ofek2608.deep_pocket.api.Knowledge;
import com.ofek2608.deep_pocket.api.events.DeepPocketConversionsUpdatedEvent;
import com.ofek2608.deep_pocket.api.struct.ElementConversions;
import net.minecraftforge.common.MinecraftForge;

final class DeepPocketClientApiImpl extends DeepPocketApiImpl<DeepPocketClientHelper> implements DeepPocketClientApi {
	private Knowledge knowledge = DeepPocketManager.getHelper().createKnowledge(conversions);
	private boolean permitPublicPocket;

	DeepPocketClientApiImpl(DeepPocketClientHelper helper) {
		super(helper);
	}

	@Override
	public void setConversions(ElementConversions conversions) {
		this.conversions = conversions;
		this.knowledge = DeepPocketManager.getHelper().createKnowledge(conversions);
		MinecraftForge.EVENT_BUS.post(new DeepPocketConversionsUpdatedEvent(this, conversions));
	}

	//Server config
	@Override public boolean isPermitPublicPocket() { return permitPublicPocket; }
	@Override public void setPermitPublicPocket(boolean value) { this.permitPublicPocket = value; }
	
	@Override
	public Knowledge getKnowledge() {
		return knowledge;
	}
}
