package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.struct.ItemAmount;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import com.ofek2608.deep_pocket.registry.pocket_screen.PocketMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class SBPatternCreate {
	private final ItemAmount[] input;
	private final ItemTypeAmount[] output;
	private final boolean toCarry;

	SBPatternCreate(ItemAmount[] input, ItemTypeAmount[] output, boolean toCarry) {
		this.input = input;
		this.output = output;
		this.toCarry = toCarry;
	}

	SBPatternCreate(FriendlyByteBuf buf) {
		this(
						DeepPocketUtils.decodeArray(buf, ItemAmount[]::new, ItemAmount::decode),
						DeepPocketUtils.decodeArray(buf, ItemTypeAmount[]::new, ItemTypeAmount::decode),
						buf.readBoolean()
		);
	}

	void encode(FriendlyByteBuf buf) {
		DeepPocketUtils.encodeArray(buf, input, ItemAmount::encode);
		DeepPocketUtils.encodeArray(buf, output, ItemTypeAmount::encode);
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