package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.registry.pocket_screen.PocketMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class SBRequestRecipe {
	private final ItemType[] types;

	SBRequestRecipe(ItemType ... types) {
		this.types = types;
	}

	SBRequestRecipe(FriendlyByteBuf buf) {
		this(DPPacketUtils.decodeItemTypeArray(buf));
	}

	void encode(FriendlyByteBuf buf) {
		DPPacketUtils.encodeItemTypeArray(buf, types);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> {
			ServerPlayer player = ctxSupplier.get().getSender();
			if (player == null || !(player.containerMenu instanceof PocketMenu menu))
				return;
			menu.requestRecipeServerBound(player, types);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
