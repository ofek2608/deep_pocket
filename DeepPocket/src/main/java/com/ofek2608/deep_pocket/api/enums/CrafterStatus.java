package com.ofek2608.deep_pocket.api.enums;

public enum CrafterStatus {
	WORKING,
	WAITING_FOR_INGREDIENTS,
	INVALID_PATTERN,
	MISSING_CRAFTER,
	FINISHED;
	private static final CrafterStatus[] VALUES = values();
	
	public static CrafterStatus join(CrafterStatus status0, CrafterStatus status1) {
		return VALUES[Math.min(status0.ordinal(), status1.ordinal())];
	}
	
}
