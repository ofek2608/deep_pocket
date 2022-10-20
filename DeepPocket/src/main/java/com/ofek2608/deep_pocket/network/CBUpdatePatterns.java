package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.CraftingPattern;
import com.ofek2608.deep_pocket.api.Pocket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

class CBUpdatePatterns {
	private final UUID pocketId;
	private final CraftingPattern[] addedPatterns;
	private final UUID[] removedPatterns;

	CBUpdatePatterns(UUID pocketId, CraftingPattern[] addedPatterns, UUID[] removedPatterns) {
		this.pocketId = pocketId;
		this.addedPatterns = addedPatterns;
		this.removedPatterns = removedPatterns;
	}

	CBUpdatePatterns(FriendlyByteBuf buf) {
		this(
						buf.readUUID(),
						DeepPocketUtils.decodeArray(buf, CraftingPattern[]::new, CraftingPattern::decode),
						DeepPocketUtils.decodeArray(buf, UUID[]::new, FriendlyByteBuf::readUUID)
		);
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
		DeepPocketUtils.encodeArray(buf, addedPatterns, CraftingPattern::encode);
		DeepPocketUtils.encodeArray(buf, removedPatterns, FriendlyByteBuf::writeUUID);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			DeepPocketClientApi api = DeepPocketClientApi.get();
			Pocket pocket = api.getPocket(pocketId);
			if (pocket == null)
				return;
			for (CraftingPattern pattern : addedPatterns)
				pocket.getPatternsMap().put(pattern.getPatternId(), pattern);
			for (UUID patternId : removedPatterns)
				pocket.removePattern(patternId);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
