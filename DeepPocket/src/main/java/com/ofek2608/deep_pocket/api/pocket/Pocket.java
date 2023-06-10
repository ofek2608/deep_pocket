package com.ofek2608.deep_pocket.api.pocket;

import com.ofek2608.deep_pocket.api.types.EntryType;

public interface Pocket {
	PocketProperties getProperties();
	PocketEntry getEntry(EntryType type);
	
	default long getCount(EntryType type) {
		return getEntry(type).getCount();
	}
}
