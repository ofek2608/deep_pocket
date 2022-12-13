package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class CBKnowledgeRem {
	private final ElementType[] types;

	CBKnowledgeRem(ElementType... types) {
		this.types = types;
	}

	CBKnowledgeRem(FriendlyByteBuf buf) {
		this(DPPacketUtils.decodeElementTypeArray(buf));
	}

	void encode(FriendlyByteBuf buf) {
		DPPacketUtils.encodeElementTypeArray(buf, types);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> DeepPocketClientApi.get().getKnowledge().remove(types));
		ctxSupplier.get().setPacketHandled(true);
	}
}
