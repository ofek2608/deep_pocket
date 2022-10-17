package com.ofek2608.deep_pocket.api.enums;

public enum PocketDisplayMode {
	NORMAL("Normal"),
	CRAFTING("Crafting"),
	CREATE_PATTERN("Create Pattern");
	public final String displayName;

	PocketDisplayMode(String displayName) {
		this.displayName = displayName;
	}
}
