package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

final class DPPacketUtils {
	private DPPacketUtils() {}
	
	static ElementType[] decodeElementTypeArray(FriendlyByteBuf buf) {
		return DeepPocketUtils.decodeArray(buf, ElementType[]::new, ElementType::decode);
	}
	
	static void encodeElementTypeArray(FriendlyByteBuf buf, ElementType[] types) {
		DeepPocketUtils.encodeArray(buf, types, ElementType::encode);
	}
	
	static <T> Map<ElementType,T> decodeElementTypeMap(FriendlyByteBuf buf, Function<FriendlyByteBuf,T> reader) {
		Map<ElementType,T> map = new HashMap<>();
		ElementType[] types = DPPacketUtils.decodeElementTypeArray(buf);
		for (ElementType type : types)
			map.put(type, reader.apply(buf));
		return map;
	}
	
	static <T> void encodeElementTypeMap(FriendlyByteBuf buf, Map<ElementType,T> map, BiConsumer<FriendlyByteBuf,T> writer) {
		ElementType[] types = map.keySet().toArray(new ElementType[0]);
		DPPacketUtils.encodeElementTypeArray(buf, types);
		for (ElementType type : types)
			writer.accept(buf, map.get(type));
	}
	
	static Map<ElementType, Optional<UUID>> decodeElementTypeUUIDMap(FriendlyByteBuf buf) {
		return DPPacketUtils.decodeElementTypeMap(buf, b->b.readBoolean() ? Optional.of(b.readUUID()) : Optional.empty());
	}
	
	static void encodeElementTypeUUIDMap(FriendlyByteBuf buf, Map<ElementType,Optional<UUID>> map) {
		DPPacketUtils.encodeElementTypeMap(buf, map, (b,uuid)->{
			b.writeBoolean(uuid.isPresent());
			uuid.ifPresent(b::writeUUID);
		});
	}
}
