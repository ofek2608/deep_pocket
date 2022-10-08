package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.registry.pocket_screen.PocketMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class SBClearCraftingGrid {
	private final boolean up;

	SBClearCraftingGrid(boolean up) {
		this.up = up;
	}

	SBClearCraftingGrid(FriendlyByteBuf buf) {
		this(buf.readBoolean());
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeBoolean(up);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			ServerPlayer player = ctxSupplier.get().getSender();
			if (player == null || !(player.containerMenu instanceof PocketMenu menu))
				return;
			if (up)
				menu.clearCraftingTable(player);
			else
				menu.clearCraftingTableToInventory(player);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
