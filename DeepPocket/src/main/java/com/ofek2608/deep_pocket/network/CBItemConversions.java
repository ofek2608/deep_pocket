package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.ItemConversions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class CBItemConversions {
	private final ItemConversions conversions;

	CBItemConversions(ItemConversions conversions) {
		this.conversions = conversions;
	}

	CBItemConversions(FriendlyByteBuf buf) {
		this(ItemConversions.decode(buf));
	}

	void encode(FriendlyByteBuf buf) {
		ItemConversions.encode(buf, conversions);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> DeepPocketClientApi.get().setItemConversions(conversions));
		ctxSupplier.get().setPacketHandled(true);
	}
}
