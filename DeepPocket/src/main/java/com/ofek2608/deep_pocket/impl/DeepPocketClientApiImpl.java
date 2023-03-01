package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import com.ofek2608.deep_pocket.api.Knowledge;
import com.ofek2608.deep_pocket.api.events.DeepPocketConversionsUpdatedEvent;
import com.ofek2608.deep_pocket.api.struct.ElementConversions;
import com.ofek2608.deep_pocket.api.struct.PocketInfo;
import com.ofek2608.deep_pocket.api.struct.client.ClientElementIndices;
import com.ofek2608.deep_pocket.api.struct.client.ClientPocket;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.UUID;

final class DeepPocketClientApiImpl extends DeepPocketApiImpl<DeepPocketClientHelper, ClientPocket> implements DeepPocketClientApi {
	private final ClientElementIndices elementIndices = new ClientElementIndices();
	private Knowledge knowledge = DeepPocketManager.getHelper().createKnowledge(conversions);
	private boolean permitPublicPocket;

	DeepPocketClientApiImpl(DeepPocketClientHelper helper) {
		super(helper);
	}
	
	@Override
	public ClientElementIndices getElementIndices() {
		return elementIndices;
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
	
	public @Nullable ClientPocket createPocket(UUID pocketId, UUID owner, PocketInfo info) {
		if (pockets.containsKey(pocketId))
			return null;
		ClientPocket newPocket = new ClientPocket(pocketId, owner, info);
		pockets.put(pocketId, newPocket);
		return newPocket;
	}
}
