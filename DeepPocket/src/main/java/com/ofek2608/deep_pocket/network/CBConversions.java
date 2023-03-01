package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.ElementConversionsOld;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class CBConversions {
	private final ElementConversionsOld conversions;

	CBConversions(ElementConversionsOld conversions) {
		this.conversions = conversions;
	}

	CBConversions(FriendlyByteBuf buf) {
		this(ElementConversionsOld.decode(buf));
	}

	void encode(FriendlyByteBuf buf) {
		ElementConversionsOld.encode(buf, conversions);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> DeepPocketClientApi.get().setConversions(conversions));
		ctxSupplier.get().setPacketHandled(true);
	}
}
