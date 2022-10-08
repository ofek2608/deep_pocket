package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class SBCreatePocket {
	private final String name;
	private final ItemType icon;
	private final int color;
	private final PocketSecurityMode securityMode;

	SBCreatePocket(String name, ItemType icon, int color, PocketSecurityMode securityMode) {
		this.name = name;
		this.icon = icon;
		this.color = color & 0xFFFFFF;
		this.securityMode = securityMode;
	}

	SBCreatePocket(FriendlyByteBuf buf) {
		this(buf.readUtf(Pocket.MAX_NAME_LENGTH), ItemType.decode(buf), buf.readInt(), buf.readEnum(PocketSecurityMode.class));
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeUtf(name, Pocket.MAX_NAME_LENGTH);
		ItemType.encode(buf, icon);
		buf.writeInt(color);
		buf.writeEnum(securityMode);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			ServerPlayer player = ctxSupplier.get().getSender();
			DeepPocketServerApi api = DeepPocketServerApi.get();
			if (player == null || api == null)
				return;
			api.createPocketFor(player, name, icon, color, securityMode);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
