package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.struct.PocketInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

class SBChangePocketSettings {
	private final UUID pocketId;
	private final PocketInfo info;

	SBChangePocketSettings(UUID pocketId, PocketInfo info) {
		this.pocketId = pocketId;
		this.info = info;
	}

	SBChangePocketSettings(FriendlyByteBuf buf) {
		this(buf.readUUID(), PocketInfo.decode(buf));
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
		PocketInfo.encode(buf, info);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			ServerPlayer player = ctxSupplier.get().getSender();
			DeepPocketServerApi api = DeepPocketServerApi.get();
			if (player == null || api == null)
				return;
			api.changePocketSettingsFrom(player, pocketId, info);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
