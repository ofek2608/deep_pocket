package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class CBRemoveItemValue {
	private final ItemType[] types;

	CBRemoveItemValue(ItemType ... types) {
		this.types = types;
	}

	CBRemoveItemValue(FriendlyByteBuf buf) {
		this(DPPacketUtils.readItemTypeArray(buf));
	}

	void encode(FriendlyByteBuf buf) {
		DPPacketUtils.writeItemTypeArray(buf, types);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			DeepPocketClientApi api = DeepPocketClientApi.get();
			for (ItemType type : types)
				api.setItemValue(type, null);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
