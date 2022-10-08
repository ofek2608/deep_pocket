package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

class CBPocketDestroy {
	private final UUID pocketId;

	CBPocketDestroy(UUID pocketId) {
		this.pocketId = pocketId;
	}

	CBPocketDestroy(FriendlyByteBuf buf) {
		this(buf.readUUID());
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> DeepPocketClientApi.get().destroyPocket(pocketId));
		ctxSupplier.get().setPacketHandled(true);
	}
}
