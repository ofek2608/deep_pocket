package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.registry.pocket_screen.PocketMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class SBPocketInsert {
	private final byte count;

	SBPocketInsert(byte count) {
		this.count = count;
	}

	SBPocketInsert(FriendlyByteBuf buf) {
		this(buf.readByte());
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeByte(count);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			ServerPlayer player = ctxSupplier.get().getSender();
			if (player == null || !(player.containerMenu instanceof PocketMenu menu))
				return;
			menu.insertCarry(player, count);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
