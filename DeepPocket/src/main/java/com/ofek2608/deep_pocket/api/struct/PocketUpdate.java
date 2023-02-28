package com.ofek2608.deep_pocket.api.struct;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PocketUpdate {
	public final List<ElementCount> updatedElementCount = new ArrayList<>();
	public final List<UUID> removedPatterns = new ArrayList<>();
	public final List<AddedPattern> addedPatterns = new ArrayList<>();
	public final List<DefaultPattern> changedDefaultPatterns = new ArrayList<>();
	public final List<CraftingProcessUpdate> craftingProcessUpdates = new ArrayList<>();
	public final List<CraftingProcessSetup> craftingProcessSetups = new ArrayList<>();
	
	public record ElementCount(int elementId, long elementCount) {}
	public record AddedPattern(UUID patternId, CraftingPattern pattern) {}
	public record DefaultPattern(int elementId, UUID patternId) {}
	public record CraftingProcessUpdate(int processId, CraftingEntryUpdate[] entries) {}
	public record CraftingProcessSetup(int processId, CraftingEntrySetup[] entries) {}
	public record CraftingEntryUpdate(long requiredAmount, long inStorage, long currentlyCrafting) {}
	public record CraftingEntrySetup(int elementId, UUID patternId, CraftingEntryUpdate update) {}
	
	public static void encode(FriendlyByteBuf buf, PocketUpdate update) {
		buf.writeVarInt(update.updatedElementCount.size());
		for (ElementCount elementCount : update.updatedElementCount) {
			buf.writeVarInt(elementCount.elementId);
			buf.writeVarLong(elementCount.elementCount + 1);
		}
		
		buf.writeVarInt(update.removedPatterns.size());
		for (UUID patternId : update.removedPatterns) {
			buf.writeUUID(patternId);
		}
		
		buf.writeVarInt(update.addedPatterns.size());
		for (AddedPattern addedPattern : update.addedPatterns) {
			buf.writeUUID(addedPattern.patternId);
			CraftingPattern.encode(buf, addedPattern.pattern);
		}
		
		buf.writeVarInt(update.changedDefaultPatterns.size());
		for (DefaultPattern defaultPattern : update.changedDefaultPatterns) {
			buf.writeVarInt(defaultPattern.elementId);
			buf.writeUUID(defaultPattern.patternId);
		}
		
		for (CraftingProcessUpdate process : update.craftingProcessUpdates) {
			buf.writeVarInt(process.processId);
			buf.writeVarInt(process.entries.length);
			for (CraftingEntryUpdate entry : process.entries) {
				serializeCraftingEntryUpdate(buf, entry);
			}
		}
		buf.writeVarInt(0);//marks the end
		
		for (CraftingProcessSetup process : update.craftingProcessSetups) {
			buf.writeVarInt(process.processId);
			buf.writeVarInt(process.entries.length);
			for (CraftingEntrySetup entry : process.entries) {
				buf.writeVarInt(entry.elementId);
				buf.writeUUID(entry.patternId);
				serializeCraftingEntryUpdate(buf, entry.update);
			}
		}
		buf.writeVarInt(0);//marks the end
	}
	
	public static PocketUpdate decode(FriendlyByteBuf buf) {
		PocketUpdate update = new PocketUpdate();
		int length, id;
		
		length = buf.readVarInt();
		for (int i = 0; i < length; i++) {
			update.updatedElementCount.add(new ElementCount(buf.readVarInt(), buf.readVarLong() - 1));
		}
		
		length = buf.readVarInt();
		for (int i = 0; i < length; i++) {
			update.removedPatterns.add(buf.readUUID());
		}
		
		length = buf.readVarInt();
		for (int i = 0; i < length; i++) {
			update.addedPatterns.add(new AddedPattern(buf.readUUID(), CraftingPattern.decode(buf)));
		}
		
		length = buf.readVarInt();
		for (int i = 0; i < length; i++) {
			update.changedDefaultPatterns.add(new DefaultPattern(buf.readVarInt(), buf.readUUID()));
		}
		
		while ((id = buf.readVarInt()) != 0) {
			length = buf.readVarInt();
			CraftingEntryUpdate[] entries = new CraftingEntryUpdate[length];
			for (int i = 0; i < length; i++) {
				entries[i] = deserializeCraftingEntryUpdate(buf);
			}
			update.craftingProcessUpdates.add(new CraftingProcessUpdate(id, entries));
		}
		
		while ((id = buf.readVarInt()) != 0) {
			length = buf.readVarInt();
			CraftingEntrySetup[] entries = new CraftingEntrySetup[length];
			for (int i = 0; i < length; i++) {
				entries[i] = new CraftingEntrySetup(
						buf.readVarInt(),
						buf.readUUID(),
						deserializeCraftingEntryUpdate(buf)
				);
			}
			update.craftingProcessSetups.add(new CraftingProcessSetup(id, entries));
		}
		
		return update;
	}
	
	
	
	
	private static void serializeCraftingEntryUpdate(FriendlyByteBuf buf, CraftingEntryUpdate entry) {
		buf.writeVarLong(entry.requiredAmount + 1);
		buf.writeVarLong(entry.inStorage + 1);
		buf.writeVarLong(entry.currentlyCrafting + 1);
	}
	
	private static CraftingEntryUpdate deserializeCraftingEntryUpdate(FriendlyByteBuf buf) {
		return new CraftingEntryUpdate(
				buf.readVarLong() - 1,
				buf.readVarLong() - 1,
				buf.readVarLong() - 1
		);
	}
}
