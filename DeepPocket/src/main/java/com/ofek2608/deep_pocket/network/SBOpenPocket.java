package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.integration.DeepPocketCurios;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

class SBOpenPocket {
	SBOpenPocket() {}

	SBOpenPocket(FriendlyByteBuf buf) {
		this();
	}

	@SuppressWarnings("EmptyMethod")
	void encode(FriendlyByteBuf buf) {}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			DeepPocketServerApi api = DeepPocketServerApi.get();
			if (api == null)
				return;
			ServerPlayer player = ctxSupplier.get().getSender();
			if (player == null)
				return;
			UUID pocketId = DeepPocketCurios.getPocket(player);
			if (pocketId == null)
				return;
			api.openPocket(player, pocketId);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
