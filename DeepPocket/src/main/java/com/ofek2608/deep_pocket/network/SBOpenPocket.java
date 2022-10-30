package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.integration.DeepPocketCurios;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

class SBOpenPocket {
	private final int type;

	SBOpenPocket(int type) {
		this.type = type;
	}

	SBOpenPocket(FriendlyByteBuf buf) {
		this(buf.readVarInt());
	}

	@SuppressWarnings("EmptyMethod")
	void encode(FriendlyByteBuf buf) {
		buf.writeVarInt(type);
	}

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
			switch (type) {
				case 0 -> api.openPocket(player, pocketId);
				case 1 -> api.openProcesses(player, pocketId);
			}
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
