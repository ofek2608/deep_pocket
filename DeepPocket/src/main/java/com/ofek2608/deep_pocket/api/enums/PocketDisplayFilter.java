package com.ofek2608.deep_pocket.api.enums;

public enum PocketDisplayFilter {
	ITEMS_AND_FLUIDS("Items & Fluids"),
	ITEMS("Items"),
	FLUIDS("Fluids");
	public final String displayName;
	
	PocketDisplayFilter(String displayName) {
		this.displayName = displayName;
	}
}
