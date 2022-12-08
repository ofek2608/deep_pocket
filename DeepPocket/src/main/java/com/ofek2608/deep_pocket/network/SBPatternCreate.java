package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.struct.ElementTypeStack;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import com.ofek2608.deep_pocket.registry.pocket_screen.PocketMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class SBPatternCreate {
	private final ElementTypeStack[] input;
	private final ElementTypeStack[] output;
	private final boolean toCarry;

	SBPatternCreate(ElementTypeStack[] input, ElementTypeStack[] output, boolean toCarry) {
		this.input = input;
		this.output = output;
		this.toCarry = toCarry;
	}

	SBPatternCreate(FriendlyByteBuf buf) {
		this(
						DeepPocketUtils.decodeArray(buf, ElementTypeStack[]::new, ElementTypeStack::decode),
						DeepPocketUtils.decodeArray(buf, ElementTypeStack[]::new, ElementTypeStack::decode),
						buf.readBoolean()
		);
	}

	void encode(FriendlyByteBuf buf) {
		DeepPocketUtils.encodeArray(buf, input, ElementTypeStack::encode);
		DeepPocketUtils.encodeArray(buf, output, ElementTypeStack::encode);
		buf.writeBoolean(toCarry);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			ServerPlayer player = ctxSupplier.get().getSender();
			if (player == null || !(player.containerMenu instanceof PocketMenu menu))
				return;
			menu.createPattern(player, input, output, toCarry);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
