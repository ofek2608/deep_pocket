package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.struct.ItemType;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;

final class DPPacketUtils {
	private DPPacketUtils() {}

	static <T> T[] decodeArray(FriendlyByteBuf buf, IntFunction<T[]> arrayFactory, Function<FriendlyByteBuf,T> decoder) {
		int length = buf.readVarInt();
		T[] result = arrayFactory.apply(length);
		for (int i = 0; i < length; i++)
			result[i] = decoder.apply(buf);
		return result;
	}

	static <T> void encodeArray(FriendlyByteBuf buf, T[] array, BiConsumer<FriendlyByteBuf,T> encoder) {
		buf.writeVarInt(array.length);
		for (T t : array)
			encoder.accept(buf, t);
	}

	static ItemType[] decodeItemTypeArray(FriendlyByteBuf buf) {
		return decodeArray(buf, ItemType[]::new, ItemType::decode);
	}

	static void encodeItemTypeArray(FriendlyByteBuf buf, ItemType[] types) {
		encodeArray(buf, types, ItemType::encode);
	}

	static <T> Map<ItemType,T> decodeItemTypeMap(FriendlyByteBuf buf, Function<FriendlyByteBuf,T> reader) {
		Map<ItemType,T> map = new HashMap<>();
		ItemType[] types = DPPacketUtils.decodeItemTypeArray(buf);
		for (ItemType type : types)
			map.put(type, reader.apply(buf));
		return map;
	}

	static <T> void encodeItemTypeMap(FriendlyByteBuf buf, Map<ItemType,T> map, BiConsumer<FriendlyByteBuf,T> writer) {
		ItemType[] types = map.keySet().toArray(new ItemType[0]);
		DPPacketUtils.encodeItemTypeArray(buf, types);
		for (ItemType type : types)
			writer.accept(buf, map.get(type));
	}
}
