package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.registry.MenuWithPocket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

class CBSetViewedPocket {
	private final UUID pocketId;

	CBSetViewedPocket(UUID pocketId) {
		this.pocketId = pocketId;
	}

	CBSetViewedPocket(FriendlyByteBuf buf) {
		this(buf.readUUID());
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> {
			if (Minecraft.getInstance().player.containerMenu instanceof MenuWithPocket menu)
				menu.setPocket(DeepPocketClientApi.get().getPocket(pocketId));
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
