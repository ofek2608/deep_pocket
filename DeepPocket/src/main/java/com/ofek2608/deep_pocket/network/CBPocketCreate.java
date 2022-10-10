package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.PocketInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

class CBPocketCreate {
	private final UUID pocketId;
	private final UUID owner;
	private final PocketInfo info;

	CBPocketCreate(UUID pocketId, UUID owner, PocketInfo info) {
		this.pocketId = pocketId;
		this.owner = owner;
		this.info = info;
	}

	CBPocketCreate(FriendlyByteBuf buf) {
		this(buf.readUUID(), buf.readUUID(), PocketInfo.decode(buf));
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
		buf.writeUUID(owner);
		PocketInfo.encode(buf, info);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> DeepPocketClientApi.get().createPocket(pocketId, owner, info));
		ctxSupplier.get().setPacketHandled(true);
	}
}
