package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.Knowledge;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class CBKnowledgeAdd {
	private final int[] elementIds;

	CBKnowledgeAdd(int ... elementIds) {
		this.elementIds = elementIds;
	}

	CBKnowledgeAdd(FriendlyByteBuf buf) {
		this(buf.readVarIntArray());
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeVarIntArray(elementIds);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> DeepPocketClientApi.get().getKnowledge().add(elementIds));
		ctxSupplier.get().setPacketHandled(true);
	}
}
