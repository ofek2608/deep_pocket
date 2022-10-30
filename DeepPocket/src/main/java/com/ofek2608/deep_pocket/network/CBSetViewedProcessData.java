package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.struct.ProcessUnitClientData;
import com.ofek2608.deep_pocket.registry.process_screen.ProcessMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class CBSetViewedProcessData {
	private final ProcessUnitClientData data;

	CBSetViewedProcessData(ProcessUnitClientData data) {
		this.data = data;
	}

	CBSetViewedProcessData(FriendlyByteBuf buf) {
		this(ProcessUnitClientData.decode(buf));
	}

	void encode(FriendlyByteBuf buf) {
		ProcessUnitClientData.encode(buf, data);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> {
			if (Minecraft.getInstance().player.containerMenu instanceof ProcessMenu menu)
				menu.clientData = data;
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
