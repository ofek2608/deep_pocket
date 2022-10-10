package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class CBClearKnowledge {
	CBClearKnowledge() {}

	CBClearKnowledge(FriendlyByteBuf buf) {
		this();
	}

	@SuppressWarnings("EmptyMethod")
	void encode(FriendlyByteBuf buf) {}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> DeepPocketClientApi.get().getKnowledge().clear());
		ctxSupplier.get().setPacketHandled(true);
	}
}
