package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.ElementConversions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class CBConversions {
	private final ElementConversions conversions;

	CBConversions(ElementConversions conversions) {
		this.conversions = conversions;
	}

	CBConversions(FriendlyByteBuf buf) {
		this(ElementConversions.decode(buf));
	}

	void encode(FriendlyByteBuf buf) {
		ElementConversions.encode(buf, conversions);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> DeepPocketClientApi.get().setConversions(conversions));
		ctxSupplier.get().setPacketHandled(true);
	}
}
