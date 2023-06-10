package com.ofek2608.deep_pocket.api.enums;

public enum SortingOrder {
	COUNT("Count") {
	},
	ID("ID") {
	},
	NAME("Name") {
	},
	MOD("Mod") {
	};
	public final String displayName;
	
	SortingOrder(String displayName) {
		this.displayName = displayName;
	}
}