package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.Knowledge;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class CBKnowledgeClear {
	CBKnowledgeClear() {}

	CBKnowledgeClear(FriendlyByteBuf buf) {
		this();
	}

	@SuppressWarnings("EmptyMethod")
	void encode(FriendlyByteBuf buf) {}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> {
			Knowledge knowledge = DeepPocketClientApi.get().getKnowledge();
			knowledge.asSet().clear();
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
