package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.registry.pocket_screen.PocketMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class SBBulkCrafting {
	private final long count;

	SBBulkCrafting(long count) {
		this.count = count;
	}

	SBBulkCrafting(FriendlyByteBuf buf) {
		this(buf.readLong());
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeLong(count);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			ServerPlayer player = ctxSupplier.get().getSender();
			if (player == null || !(player.containerMenu instanceof PocketMenu menu))
				return;
			menu.bulkCrafting(player, count);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
