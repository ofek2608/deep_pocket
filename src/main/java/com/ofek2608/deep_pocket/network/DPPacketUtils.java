package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.ItemValue;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

final class DPPacketUtils {
	private DPPacketUtils() {}

	static ItemType[] readItemTypeArray(FriendlyByteBuf buf) {
		int length = buf.readVarInt();
		ItemType[] result = new ItemType[length];
		for (int i = 0; i < length; i++)
			result[i] = ItemType.decode(buf);
		return result;
	}

	static void writeItemTypeArray(FriendlyByteBuf buf, ItemType[] types) {
		buf.writeVarInt(types.length);
		for (ItemType type : types)
			ItemType.encode(buf, type);
	}

	static <T> Map<ItemType,T> readItemTypeMap(FriendlyByteBuf buf, Function<FriendlyByteBuf,T> reader) {
		Map<ItemType,T> map = new HashMap<>();
		ItemType[] types = DPPacketUtils.readItemTypeArray(buf);
		for (ItemType type : types)
			map.put(type, reader.apply(buf));
		return map;
	}

	static <T> void writeItemTypeMap(FriendlyByteBuf buf, Map<ItemType,T> map, BiConsumer<FriendlyByteBuf,T> writer) {
		ItemType[] types = map.keySet().toArray(new ItemType[0]);
		DPPacketUtils.writeItemTypeArray(buf, types);
		for (ItemType type : types)
			writer.accept(buf, map.get(type));
	}

	static ItemValue readItemValue(FriendlyByteBuf buf) {
		return new ItemValue(readItemTypeMap(buf, FriendlyByteBuf::readDouble));
	}

	static void writeItemValue(FriendlyByteBuf buf, ItemValue value) {
		writeItemTypeMap(buf, value.items, FriendlyByteBuf::writeDouble);
	}
}
