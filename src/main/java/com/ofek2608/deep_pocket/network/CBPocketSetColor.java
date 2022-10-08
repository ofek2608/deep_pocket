package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.Pocket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

class CBPocketSetColor {
	private final UUID pocketId;
	private final int color;

	CBPocketSetColor(UUID pocketId, int color) {
		this.pocketId = pocketId;
		this.color = color & 0xFFFFFF;
	}

	CBPocketSetColor(FriendlyByteBuf buf) {
		this(buf.readUUID(), buf.readInt());
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
		buf.writeInt(color);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			DeepPocketClientApi api = DeepPocketClientApi.get();
			Pocket pocket = api.getPocket(pocketId);
			if (pocket != null)
				pocket.setColor(color);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
