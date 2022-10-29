package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

final class DPPacketUtils {
	private DPPacketUtils() {}

	static ItemType[] decodeItemTypeArray(FriendlyByteBuf buf) {
		return DeepPocketUtils.decodeArray(buf, ItemType[]::new, ItemType::decode);
	}

	static void encodeItemTypeArray(FriendlyByteBuf buf, ItemType[] types) {
		DeepPocketUtils.encodeArray(buf, types, ItemType::encode);
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

	static Map<ItemType, Optional<UUID>> decodeItemTypeUUIDMap(FriendlyByteBuf buf) {
		return DPPacketUtils.decodeItemTypeMap(buf, b->b.readBoolean() ? Optional.of(b.readUUID()) : Optional.empty());
	}

	static void encodeItemTypeUUIDMap(FriendlyByteBuf buf, Map<ItemType,Optional<UUID>> map) {
		DPPacketUtils.encodeItemTypeMap(buf, map, (b,uuid)->{
			b.writeBoolean(uuid.isPresent());
			uuid.ifPresent(b::writeUUID);
		});
	}
}
