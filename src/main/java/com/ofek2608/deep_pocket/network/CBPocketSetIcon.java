package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.Pocket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

class CBPocketSetIcon {
	private final UUID pocketId;
	private final ItemType icon;

	CBPocketSetIcon(UUID pocketId, ItemType icon) {
		this.pocketId = pocketId;
		this.icon = icon;
	}

	CBPocketSetIcon(FriendlyByteBuf buf) {
		this(buf.readUUID(), ItemType.decode(buf));
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
		ItemType.encode(buf, icon);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			DeepPocketClientApi api = DeepPocketClientApi.get();
			Pocket pocket = api.getPocket(pocketId);
			if (pocket != null)
				pocket.setIcon(icon);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
