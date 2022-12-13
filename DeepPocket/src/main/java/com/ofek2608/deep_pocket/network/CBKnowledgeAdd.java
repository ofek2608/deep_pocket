package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.Knowledge0;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class CBKnowledgeAdd {
	private final boolean clear;
	private final ElementType[] types;

	CBKnowledgeAdd(boolean clear, ElementType ... types) {
		this.clear = clear;
		this.types = types;
	}

	CBKnowledgeAdd(FriendlyByteBuf buf) {
		this(
						buf.readBoolean(),
						DPPacketUtils.decodeElementTypeArray(buf)
		);
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeBoolean(clear);
		DPPacketUtils.encodeElementTypeArray(buf, types);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> {
			Knowledge0 knowledge = DeepPocketClientApi.get().getKnowledge();
			if (clear)
				knowledge.asSet().clear();
			knowledge.add(types);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
