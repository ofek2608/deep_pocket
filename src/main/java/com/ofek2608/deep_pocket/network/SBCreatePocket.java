package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.struct.PocketInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class SBCreatePocket {
	private final PocketInfo info;

	SBCreatePocket(PocketInfo info) {
		this.info = info;
	}

	SBCreatePocket(FriendlyByteBuf buf) {
		this(PocketInfo.decode(buf));
	}

	void encode(FriendlyByteBuf buf) {
		PocketInfo.encode(buf, info);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			ServerPlayer player = ctxSupplier.get().getSender();
			DeepPocketServerApi api = DeepPocketServerApi.get();
			if (player == null || api == null)
				return;
			api.createPocketFor(player, info);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
