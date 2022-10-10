package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.Pocket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

class CBPocketClearItems {
	private final UUID pocketId;

	CBPocketClearItems(UUID pocketId) {
		this.pocketId = pocketId;
	}

	CBPocketClearItems(FriendlyByteBuf buf) {
		this(buf.readUUID());
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			DeepPocketClientApi api = DeepPocketClientApi.get();
			Pocket pocket = api.getPocket(pocketId);
			if (pocket != null)
				pocket.clearItems();
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
