package com.ofek2608.deep_pocket.api.enums;

public enum SearchMode {
	NORMAL("Normal", false, false),
	SYNC_JEI("Sync JEI", true, true),
	SYNC_FROM_JEI("Sync from JEI", true, false),
	SYNC_TO_JEI("Sync to JEI", false, true);

	public final String displayName;
	public final boolean syncFrom, syncTo;

	SearchMode(String displayName, boolean syncFrom, boolean syncTo) {
		this.displayName = displayName;
		this.syncFrom = syncFrom;
		this.syncTo = syncTo;
	}
}
