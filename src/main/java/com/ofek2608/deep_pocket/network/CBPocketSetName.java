package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.Pocket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

class CBPocketSetName {
	private final UUID pocketId;
	private final String name;

	CBPocketSetName(UUID pocketId, String name) {
		this.pocketId = pocketId;
		this.name = name;
	}

	CBPocketSetName(FriendlyByteBuf buf) {
		this(buf.readUUID(), buf.readUtf(Pocket.MAX_NAME_LENGTH));
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
		buf.writeUtf(name, Pocket.MAX_NAME_LENGTH);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			DeepPocketClientApi api = DeepPocketClientApi.get();
			Pocket pocket = api.getPocket(pocketId);
			if (pocket != null)
				pocket.setName(name);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
