package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.PocketUpdate;
import com.ofek2608.deep_pocket.api.struct.client.ClientPocket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

class CBPocketUpdate {
	private final UUID pocketId;
	private final PocketUpdate update;

	CBPocketUpdate(UUID pocketId, PocketUpdate update) {
		this.pocketId = pocketId;
		this.update = update;
	}

	CBPocketUpdate(FriendlyByteBuf buf) {
		this(buf.readUUID(), PocketUpdate.decode(buf));
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
		PocketUpdate.encode(buf, update);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> {
			ClientPocket pocket = DeepPocketClientApi.get().getPocket(pocketId);
			if (pocket == null) {
				return;
			}
			pocket.getOrCreateData().applyUpdate(update);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
