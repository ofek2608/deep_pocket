package com.ofek2608.deep_pocket.api.struct;

import net.minecraft.nbt.ListTag;

import java.util.Arrays;

public final class CraftingProcess {
	private final int id;
	private final CraftingEntry[] entries;
	
	private CraftingProcess(int id, int length) {
		this.id = id;
		this.entries = new CraftingEntry[length];
	}
	
	public CraftingProcess(PocketUpdate.CraftingProcessSetup setup) {
		this(setup.processId(), setup.entries().length);
		for (int i = 0; i < entries.length; i++) {
			entries[i] = new CraftingEntry(setup.entries()[i]);
		}
	}
	
	public PocketUpdate.CraftingProcessUpdate createUpdate() {
		return new PocketUpdate.CraftingProcessUpdate(
				id,
				Arrays.stream(entries)
						.map(CraftingEntry::createUpdate)
						.toArray(PocketUpdate.CraftingEntryUpdate[]::new)
		);
	}
	
	public PocketUpdate.CraftingProcessSetup createSetup() {
		return new PocketUpdate.CraftingProcessSetup(
				id,
				Arrays.stream(entries)
						.map(CraftingEntry::createSetup)
						.toArray(PocketUpdate.CraftingEntrySetup[]::new)
		);
	}
	
	public void applyUpdate(PocketUpdate.CraftingProcessUpdate update) {
		if (entries.length != update.entries().length) {
			throw new IllegalArgumentException("Length of update doesn't match");
		}
		for (int i = 0; i < entries.length; i++) {
			entries[i].applyUpdate(update.entries()[i]);
		}
	}
	
	public static ListTag save(CraftingProcess process) {
		ListTag saved = new ListTag();
		for (CraftingEntry entry : process.entries) {
			saved.add(CraftingEntry.save(entry));
		}
		return saved;
	}
	
	public static CraftingProcess load(int id, ListTag saved) {
		CraftingProcess process = new CraftingProcess(id, saved.size());
		for (int i = 0; i < saved.size(); i++) {
			process.entries[i] = CraftingEntry.load(saved.getCompound(i));
		}
		return process;
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
}
