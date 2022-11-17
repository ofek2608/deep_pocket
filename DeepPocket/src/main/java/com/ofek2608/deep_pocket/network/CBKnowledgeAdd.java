package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.Knowledge;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class CBKnowledgeAdd {
	private final boolean clear;
	private final ItemType[] types;

	CBKnowledgeAdd(boolean clear, ItemType ... types) {
		this.clear = clear;
		this.types = types;
	}

	CBKnowledgeAdd(FriendlyByteBuf buf) {
		this(
						buf.readBoolean(),
						DPPacketUtils.decodeItemTypeArray(buf)
		);
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeBoolean(clear);
		DPPacketUtils.encodeItemTypeArray(buf, types);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> {
			Knowledge knowledge = DeepPocketClientApi.get().getKnowledge();
			if (clear) {
				knowledge.asSet().clear();
			}
			knowledge.add(types);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
