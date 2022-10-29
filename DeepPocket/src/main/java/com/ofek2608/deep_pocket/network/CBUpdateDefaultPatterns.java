package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

class CBUpdateDefaultPatterns {
	private final UUID pocketId;
	private final Map<ItemType, Optional<UUID>> addedDefaults;
	private final ItemType[] removedDefaults;

	CBUpdateDefaultPatterns(UUID pocketId, Map<ItemType, Optional<UUID>> addedDefaults, ItemType[] removedDefaults) {
		this.pocketId = pocketId;
		this.addedDefaults = addedDefaults;
		this.removedDefaults = removedDefaults;
	}

	CBUpdateDefaultPatterns(FriendlyByteBuf buf) {
		this(
						buf.readUUID(),
						DPPacketUtils.decodeItemTypeUUIDMap(buf),
						DPPacketUtils.decodeItemTypeArray(buf)
		);
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
		DPPacketUtils.encodeItemTypeUUIDMap(buf, addedDefaults);
		DPPacketUtils.encodeItemTypeArray(buf, removedDefaults);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			DeepPocketClientApi api = DeepPocketClientApi.get();
			Pocket pocket = api.getPocket(pocketId);
			if (pocket == null)
				return;
			for (var entry : addedDefaults.entrySet())
				pocket.getDefaultPatternsMap().put(entry.getKey(), entry.getValue());
			for (ItemType type : removedDefaults)
				pocket.getDefaultPatternsMap().remove(type);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
