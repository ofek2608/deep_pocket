package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.PocketContent;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

class CBPocketContentUpdate {
	private final UUID pocketId;
	private final int newSize;
	private final int[] changedTypeIndexes;
	private final ElementType[] changedType;
	private final long[] changedTypeCount;
	private final int[] changedCountIndexes;
	private final long[] changedCount;
	
	CBPocketContentUpdate(UUID pocketId, int newSize, int[] changedTypeIndexes, ElementType[] changedType, long[] changedTypeCount, int[] changedCountIndexes, long[] changedCount) {
		this.pocketId = pocketId;
		this.newSize = newSize;
		this.changedTypeIndexes = changedTypeIndexes;
		this.changedType = changedType;
		this.changedTypeCount = changedTypeCount;
		this.changedCountIndexes = changedCountIndexes;
		this.changedCount = changedCount;
	}
	
	CBPocketContentUpdate(FriendlyByteBuf buf) {
		this.pocketId = buf.readUUID();
		this.newSize = buf.readVarInt();
		
		int changedTypeLength = buf.readVarInt();
		this.changedTypeIndexes = new int[changedTypeLength];
		this.changedType = new ElementType[changedTypeLength];
		this.changedTypeCount = new long[changedTypeLength];
		for (int i = 0; i < changedTypeLength; i++) {
			this.changedTypeIndexes[i] = buf.readVarInt();
			this.changedType[i] = ElementType.decode(buf);
			this.changedTypeCount[i] = buf.readVarLong() - 1;
		}
		int changedCountLength = buf.readVarInt();
		this.changedCountIndexes = new int[changedCountLength];
		this.changedCount = new long[changedCountLength];
		for (int i = 0; i < changedCountLength; i++) {
			this.changedCountIndexes[i] = buf.readVarInt();
			this.changedCount[i] = buf.readVarLong() - 1;
		}
	}
	
	void encode(FriendlyByteBuf buf) {
		buf.writeUUID(pocketId);
		buf.writeVarInt(newSize);
		
		buf.writeVarInt(changedTypeIndexes.length);
		for (int i = 0; i < changedTypeIndexes.length; i++) {
			buf.writeVarInt(changedTypeIndexes[i]);
			ElementType.encode(buf, changedType[i]);
			buf.writeVarLong(changedTypeCount[i] + 1);
		}
		
		buf.writeVarInt(changedCountIndexes.length);
		for (int i = 0; i < changedCountIndexes.length; i++) {
			buf.writeVarInt(changedCountIndexes[i]);
			buf.writeVarLong(changedCount[i] + 1);
		}
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			DeepPocketClientApi api = DeepPocketClientApi.get();
			Pocket pocket = api.getPocket(pocketId);
			if (pocket == null)
				return;
			PocketContent content = pocket.getContent();
			content.limitSize(newSize);
			for (int i = 0; i < changedTypeIndexes.length; i++) {
				content.setType(changedTypeIndexes[i], changedType[i]);
				content.setCount(changedTypeIndexes[i], changedTypeCount[i]);
			}
			for (int i = 0; i < changedCountIndexes.length; i++)
				content.setCount(changedCountIndexes[i], changedCount[i]);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
