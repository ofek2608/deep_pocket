package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.ItemValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

class CBSetItemValue {
	private final Map<ItemType, ItemValue> values;

	CBSetItemValue(Map<ItemType, ItemValue> values) {
		this.values = values;
	}

	CBSetItemValue(FriendlyByteBuf buf) {
		this(DPPacketUtils.readItemTypeMap(buf, DPPacketUtils::readItemValue));
	}

	void encode(FriendlyByteBuf buf) {
		DPPacketUtils.writeItemTypeMap(buf, values, DPPacketUtils::writeItemValue);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> values.forEach(DeepPocketClientApi.get()::setItemValue));
		ctxSupplier.get().setPacketHandled(true);
	}
}
