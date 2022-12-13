package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.pocket.PocketPatterns;
import com.ofek2608.deep_pocket.api.struct.CraftingPattern;
import com.ofek2608.deep_pocket.api.struct.LevelBlockPos;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

class CBUpdatePatterns {
	private final UUID pocketId;
	private final Map<UUID,CraftingPattern> addedPatterns;
	private final UUID[] removedPatterns;

	CBUpdatePatterns(UUID pocketId, Map<UUID, CraftingPattern> addedPatterns, UUID[] removedPatterns) {
		this.pocketId = pocketId;
		this.addedPatterns = addedPatterns;
		this.removedPatterns = removedPatterns;
	}

	CBUpdatePatterns(FriendlyByteBuf buf) {
		this(
						buf.readUUID(),
						DeepPocketUtils.decodeMap(buf, FriendlyByteBuf::readUUID, CraftingPattern::decode),
						DeepPocketUtils.decodeArray(buf, UUID[]::new, FriendlyByteBuf::readUUID)
		);
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
		DeepPocketUtils.encodeMap(buf, addedPatterns, FriendlyByteBuf::writeUUID, CraftingPattern::encode);
		DeepPocketUtils.encodeArray(buf, removedPatterns, FriendlyByteBuf::writeUUID);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			DeepPocketClientApi api = DeepPocketClientApi.get();
			Pocket pocket = api.getPocket(pocketId);
			if (pocket == null)
				return;
			PocketPatterns pocketPatterns = pocket.getPatterns();
			addedPatterns.forEach(pocketPatterns::put);
			for (UUID patternId : removedPatterns)
				pocketPatterns.remove(patternId, LevelBlockPos.ZERO);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
