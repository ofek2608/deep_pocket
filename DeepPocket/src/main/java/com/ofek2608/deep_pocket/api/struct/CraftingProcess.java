package com.ofek2608.deep_pocket.api.struct;

import net.minecraft.network.FriendlyByteBuf;

public final class CraftingProcess {
	private final int id;
	private final CraftingEntry[] entries;
	
	private CraftingProcess(int id, int length) {
		this.id = id;
		this.entries = new CraftingEntry[length];
	}
	
	public int getId() {
		return id;
	}
	
	public int getSize() {
		return entries.length;
	}
	
	public CraftingEntry getEntry(int index) {
		return entries[index];
	}
	
	public static CraftingProcess create(int id, CraftingEntry[] entries) {
		int length = entries.length;
		CraftingProcess process = new CraftingProcess(id, length);
		for (int i = 0; i < length; i++) {
			process.entries[i] = entries[i].copy();
		}
		return process;
	}
	
	public static CraftingProcess deserialize(int id, FriendlyByteBuf buf) {
		int length = buf.readVarInt();
		CraftingProcess process = new CraftingProcess(id, length);
		for (int i = 0; i < length; i++) {
			process.entries[i] = CraftingEntry.deserialize(buf);
		}
		return process;
	}
	
	public static void serialize(FriendlyByteBuf buf, CraftingProcess process) {
		buf.writeVarInt(process.entries.length);
		for (CraftingEntry entry : process.entries) {
			CraftingEntry.serialize(buf, entry);
		}
	}
	
	public void readUpdate(FriendlyByteBuf buf) {
		for (CraftingEntry entry : entries) {
			entry.readUpdate(buf);
		}
	}
	
	public void sendUpdate(FriendlyByteBuf buf) {
		for (CraftingEntry entry : entries) {
			entry.sendUpdate(buf);
		}
	}
}
