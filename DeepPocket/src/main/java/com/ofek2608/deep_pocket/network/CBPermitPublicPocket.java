package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class CBPermitPublicPocket {
	private final boolean value;

	CBPermitPublicPocket(boolean value) {
		this.value = value;
	}

	CBPermitPublicPocket(FriendlyByteBuf buf) {
		this(buf.readBoolean());
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeBoolean(value);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> DeepPocketClientApi.get().setPermitPublicPocket(value));
		ctxSupplier.get().setPacketHandled(true);
	}
}
