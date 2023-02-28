package com.ofek2608.deep_pocket.api.struct;

import net.minecraft.nbt.CompoundTag;

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
	
	public CraftingEntry(PocketUpdate.CraftingEntrySetup setup) {
		this(setup.elementId(), setup.patternId());
		applyUpdate(setup.update());
	}
	
	public PocketUpdate.CraftingEntryUpdate createUpdate() {
		return new PocketUpdate.CraftingEntryUpdate(
				this.requiredAmount,
				this.inStorage,
				this.currentlyCrafting
		);
	}
	
	public PocketUpdate.CraftingEntrySetup createSetup() {
		return new PocketUpdate.CraftingEntrySetup(
				this.elementId,
				this.patternId,
				createUpdate()
		);
	}
	
	public void applyUpdate(PocketUpdate.CraftingEntryUpdate update) {
		setRequiredAmount(update.requiredAmount());
		setInStorage(update.inStorage());
		setCurrentlyCrafting(update.currentlyCrafting());
	}
	
	public static CompoundTag save(CraftingEntry entry) {
		CompoundTag saved = new CompoundTag();
		saved.putInt("elementId", entry.elementId);
		saved.putUUID("patternId", entry.patternId);
		saved.putLong("requiredAmount", entry.requiredAmount);
		saved.putLong("inStorage", entry.inStorage);
		saved.putLong("currentlyCrafting", entry.currentlyCrafting);
		return saved;
	}
	
	public static CraftingEntry load(CompoundTag saved) {
		CraftingEntry entry = new CraftingEntry(saved.getInt("elementId"), saved.getUUID("patternId"));
		entry.setRequiredAmount(saved.getLong("requiredAmount"));
		entry.setInStorage(saved.getLong("inStorage"));
		entry.setCurrentlyCrafting(saved.getLong("currentlyCrafting"));
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
	
	public long getCurrentlyCrafting() {
		return currentlyCrafting;
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
	
	
	
	public CraftingEntry copy() {
		return new CraftingEntry(this);
	}
}
