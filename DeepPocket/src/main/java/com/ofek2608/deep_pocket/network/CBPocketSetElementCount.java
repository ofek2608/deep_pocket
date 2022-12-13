package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.Pocket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

class CBPocketSetElementCount {
	private final UUID pocketId;
	private final Map<ElementType, Long> counts;

	CBPocketSetElementCount(UUID pocketId, Map<ElementType, Long> counts) {
		this.pocketId = pocketId;
		this.counts = counts;
	}

	CBPocketSetElementCount(FriendlyByteBuf buf) {
		this(buf.readUUID(), DPPacketUtils.decodeElementTypeMap(buf, FriendlyByteBuf::readLong));
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
		DPPacketUtils.encodeElementTypeMap(buf, counts, FriendlyByteBuf::writeLong);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			DeepPocketClientApi api = DeepPocketClientApi.get();
			Pocket pocket = api.getPocket(pocketId);
			if (pocket != null)
				counts.forEach(pocket.getContentOld()::put);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
