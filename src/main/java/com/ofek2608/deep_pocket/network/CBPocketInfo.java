package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.Pocket;
import com.ofek2608.deep_pocket.api.struct.PocketInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

class CBPocketInfo {
	private final UUID pocketId;
	private final PocketInfo info;

	CBPocketInfo(UUID pocketId, PocketInfo info) {
		this.pocketId = pocketId;
		this.info = info;
	}

	CBPocketInfo(FriendlyByteBuf buf) {
		this(buf.readUUID(), PocketInfo.decode(buf));
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
		PocketInfo.encode(buf, info);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			DeepPocketClientApi api = DeepPocketClientApi.get();
			Pocket pocket = api.getPocket(pocketId);
			if (pocket != null)
				pocket.setInfo(info);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
