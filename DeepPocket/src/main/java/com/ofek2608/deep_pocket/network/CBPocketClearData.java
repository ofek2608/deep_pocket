package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.client.ClientPocket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

class CBPocketClearData {
	private final UUID pocketId;

	CBPocketClearData(UUID pocketId) {
		this.pocketId = pocketId;
	}

	CBPocketClearData(FriendlyByteBuf buf) {
		this(buf.readUUID());
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			ClientPocket pocket = DeepPocketClientApi.get().getPocket(pocketId);
			if (pocket != null) {
				pocket.clearData();
			}
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
