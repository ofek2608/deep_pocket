package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

class CBPocketCreate {
	private final UUID pocketId;
	private final UUID owner;
	private final String name;
	private final ItemType icon;
	private final int color;
	private final PocketSecurityMode securityMode;

	CBPocketCreate(UUID pocketId, UUID owner, String name, ItemType icon, int color, PocketSecurityMode securityMode) {
		this.pocketId = pocketId;
		this.owner = owner;
		this.name = name;
		this.icon = icon;
		this.color = color & 0xFFFFFF;
		this.securityMode = securityMode;
	}

	CBPocketCreate(FriendlyByteBuf buf) {
		this(buf.readUUID(), buf.readUUID(), buf.readUtf(Pocket.MAX_NAME_LENGTH), ItemType.decode(buf), buf.readInt(), buf.readEnum(PocketSecurityMode.class));
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
		buf.writeUUID(owner);
		buf.writeUtf(name, Pocket.MAX_NAME_LENGTH);
		ItemType.encode(buf, icon);
		buf.writeInt(color);
		buf.writeEnum(securityMode);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> {
			Pocket pocket = DeepPocketClientApi.get().getOrCreatePocket(pocketId, owner);
			pocket.setName(name);
			pocket.setIcon(icon);
			pocket.setColor(color);
			pocket.setSecurityMode(securityMode);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
