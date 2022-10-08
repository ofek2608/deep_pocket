package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class CBClearItemValues {
	CBClearItemValues() {}

	CBClearItemValues(FriendlyByteBuf buf) {
		this();
	}

	@SuppressWarnings("EmptyMethod")
	void encode(FriendlyByteBuf buf) {}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> DeepPocketClientApi.get().clearItemValues());
		ctxSupplier.get().setPacketHandled(true);
	}
}
