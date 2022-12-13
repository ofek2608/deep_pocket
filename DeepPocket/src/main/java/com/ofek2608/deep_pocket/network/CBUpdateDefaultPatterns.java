package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

class CBUpdateDefaultPatterns {
	private final UUID pocketId;
	private final Map<ElementType, Optional<UUID>> addedDefaults;
	private final ElementType[] removedDefaults;
	
	CBUpdateDefaultPatterns(UUID pocketId, Map<ElementType, Optional<UUID>> addedDefaults, ElementType[] removedDefaults) {
		this.pocketId = pocketId;
		this.addedDefaults = addedDefaults;
		this.removedDefaults = removedDefaults;
	}
	
	CBUpdateDefaultPatterns(FriendlyByteBuf buf) {
		this(
				buf.readUUID(),
				DPPacketUtils.decodeElementTypeUUIDMap(buf),
				DPPacketUtils.decodeElementTypeArray(buf)
		);
	}
	
	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
		DPPacketUtils.encodeElementTypeUUIDMap(buf, addedDefaults);
		DPPacketUtils.encodeElementTypeArray(buf, removedDefaults);
	}
	
	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(() -> {
			DeepPocketClientApi api = DeepPocketClientApi.get();
			Pocket pocket = api.getPocket(pocketId);
			if (pocket == null)
				return;
			for (var entry : addedDefaults.entrySet())
				pocket.getDefaultPatternsMap().put(entry.getKey(), entry.getValue());
			for (ElementType type : removedDefaults)
				pocket.getDefaultPatternsMap().remove(type);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
