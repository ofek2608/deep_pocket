package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

class CBSetPlayersName {
	private final Map<UUID,String> names;

	CBSetPlayersName(Map<UUID,String> names) {
		this.names = names;
	}

	CBSetPlayersName(FriendlyByteBuf buf) {
		this(new HashMap<>());
		int size = buf.readVarInt();
		for (int i = 0; i < size; i++)
			names.put(buf.readUUID(), buf.readUtf(16));
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeVarInt(names.size());
		for (var entry : names.entrySet()) {
			buf.writeUUID(entry.getKey());
			buf.writeUtf(entry.getValue(), 16);
		}
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> names.forEach(DeepPocketClientApi.get()::cachePlayerName));
		ctxSupplier.get().setPacketHandled(true);
	}
}
