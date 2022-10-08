package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

class CBPocketSetSecurity {
	private final UUID pocketId;
	private final PocketSecurityMode mode;

	CBPocketSetSecurity(UUID pocketId, PocketSecurityMode mode) {
		this.pocketId = pocketId;
		this.mode = mode;
	}

	CBPocketSetSecurity(FriendlyByteBuf buf) {
		this(buf.readUUID(), buf.readEnum(PocketSecurityMode.class));
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
		buf.writeEnum(mode);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			DeepPocketClientApi api = DeepPocketClientApi.get();
			Pocket pocket = api.getPocket(pocketId);
			if (pocket != null)
				pocket.setSecurityMode(mode);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
