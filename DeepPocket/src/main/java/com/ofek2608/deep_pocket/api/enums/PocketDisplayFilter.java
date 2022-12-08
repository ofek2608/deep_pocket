package com.ofek2608.deep_pocket.api.enums;

public enum PocketDisplayFilter {
	ITEMS_AND_FLUIDS(true, true, "Items & Fluids"),
	ITEMS(true, false, "Items"),
	FLUIDS(false, true, "Fluids");
	
	public final boolean displayItems;
	public final boolean displayFluids;
	public final String displayName;
	
	PocketDisplayFilter(boolean displayItems, boolean displayFluids, String displayName) {
		this.displayItems = displayItems;
		this.displayFluids = displayFluids;
		this.displayName = displayName;
	}
}
