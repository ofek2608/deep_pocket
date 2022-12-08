package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.registry.pocket_screen.PocketMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class SBRequestRecipe {
	private final ElementType[] elements;

	SBRequestRecipe(ElementType[] elements) {
		this.elements = elements;
	}

	SBRequestRecipe(FriendlyByteBuf buf) {
		this(DPPacketUtils.decodeElementTypeArray(buf));
	}

	void encode(FriendlyByteBuf buf) {
		DPPacketUtils.encodeElementTypeArray(buf, elements);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> {
			ServerPlayer player = ctxSupplier.get().getSender();
			if (player == null || !(player.containerMenu instanceof PocketMenu menu))
				return;
			menu.requestRecipeServerBound(player, elements);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
