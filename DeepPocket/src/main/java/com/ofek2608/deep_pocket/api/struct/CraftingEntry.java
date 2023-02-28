package com.ofek2608.deep_pocket.api.struct;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public final class CraftingEntry {
	private final int elementId;
	private final UUID patternId;
	private long requiredAmount;
	private long inStorage;
	private long currentlyCrafting;
	
	public CraftingEntry(int elementId, UUID patternId) {
		this.elementId = elementId;
		this.patternId = patternId;
	}
	
	public CraftingEntry(CraftingEntry copy) {
		this(
				copy.elementId,
				copy.patternId
		);
		this.requiredAmount = copy.requiredAmount;
		this.inStorage = copy.inStorage;
		this.currentlyCrafting = copy.currentlyCrafting;
		
	}
	
	public static void serialize(FriendlyByteBuf buf, CraftingEntry entry) {
		buf.writeVarInt(entry.elementId);
		buf.writeUUID(entry.patternId);
		entry.sendUpdate(buf);
	}
	
	public static CraftingEntry deserialize(FriendlyByteBuf buf) {
		CraftingEntry entry = new CraftingEntry(
				buf.readVarInt(),
				buf.readUUID()
		);
		entry.readUpdate(buf);
		return entry;
	}
	
	public int getElementId() {
		return elementId;
	}
	
	public UUID getPatternId() {
		return patternId;
	}
	
	public long getRequiredAmount() {
		return requiredAmount;
	}
	
	public long getInStorage() {
		return inStorage;
	}
	
	public void setRequiredAmount(long requiredAmount) {
		this.requiredAmount = requiredAmount < 0 ? -1 : requiredAmount;
	}
	
	public void setInStorage(long inStorage) {
		this.inStorage = inStorage < 0 ? -1 : inStorage;
	}
	
	public void setCurrentlyCrafting(long currentlyCrafting) {
		this.currentlyCrafting = currentlyCrafting < 0 ? -1 : currentlyCrafting;
	}
	
	
	public void sendUpdate(FriendlyByteBuf buf) {
		buf.writeVarLong(this.requiredAmount + 1);
		buf.writeVarLong(this.inStorage + 1);
		buf.writeVarLong(this.currentlyCrafting + 1);
	}
	
	public void readUpdate(FriendlyByteBuf buf) {
		setRequiredAmount(buf.readVarLong() - 1);
		setInStorage(buf.readVarLong() - 1);
		setCurrentlyCrafting(buf.readVarLong() - 1);
	}
	
	public CraftingEntry copy() {
		return new CraftingEntry(this);
	}
}
