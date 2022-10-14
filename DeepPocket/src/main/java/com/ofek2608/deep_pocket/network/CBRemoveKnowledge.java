package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class CBRemoveKnowledge {
	private final ItemType[] types;

	CBRemoveKnowledge(ItemType ... types) {
		this.types = types;
	}

	CBRemoveKnowledge(FriendlyByteBuf buf) {
		this(DPPacketUtils.decodeItemTypeArray(buf));
	}

	void encode(FriendlyByteBuf buf) {
		DPPacketUtils.encodeItemTypeArray(buf, types);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> DeepPocketClientApi.get().getKnowledge().remove(types));
		ctxSupplier.get().setPacketHandled(true);
	}
}
