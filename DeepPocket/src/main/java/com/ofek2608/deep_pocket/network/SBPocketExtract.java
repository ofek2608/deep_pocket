package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.registry.pocket_screen.PocketMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class SBPocketExtract {
	private final ElementType.TItem type;
	private final boolean toCarry;
	private final byte count;

	SBPocketExtract(ElementType.TItem type, boolean toCarry, byte count) {
		this.type = type;
		this.toCarry = toCarry;
		this.count = count;
	}

	SBPocketExtract(FriendlyByteBuf buf) {
		this((ElementType.TItem)ElementType.decode(buf), buf.readBoolean(), buf.readByte());
	}

	void encode(FriendlyByteBuf buf) {
		ElementType.encode(buf, type);
		buf.writeBoolean(toCarry);
		buf.writeByte(count);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			ServerPlayer player = ctxSupplier.get().getSender();
			if (player == null || !(player.containerMenu instanceof PocketMenu menu))
				return;
			menu.extractType(player, type, toCarry, count);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
