package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

class SBDestroyPocket {
	private final UUID pocketId;

	SBDestroyPocket(UUID pocketId) {
		this.pocketId = pocketId;
	}

	SBDestroyPocket(FriendlyByteBuf buf) {
		this(buf.readUUID());
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			ServerPlayer player = ctxSupplier.get().getSender();
			DeepPocketServerApi api = DeepPocketServerApi.get();
			if (player == null || api == null)
				return;
			api.destroyPocketFor(player, pocketId);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
