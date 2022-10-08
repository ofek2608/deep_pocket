package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.Pocket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

class CBPocketSetItemCount {
	private final UUID pocketId;
	private final Map<ItemType, Double> counts;

	CBPocketSetItemCount(UUID pocketId, Map<ItemType, Double> counts) {
		this.pocketId = pocketId;
		this.counts = counts;
	}

	CBPocketSetItemCount(FriendlyByteBuf buf) {
		this(buf.readUUID(), DPPacketUtils.readItemTypeMap(buf, FriendlyByteBuf::readDouble));
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
		DPPacketUtils.writeItemTypeMap(buf, counts, FriendlyByteBuf::writeDouble);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			DeepPocketClientApi api = DeepPocketClientApi.get();
			Pocket pocket = api.getPocket(pocketId);
			if (pocket != null)
				counts.forEach(pocket::setCount);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
