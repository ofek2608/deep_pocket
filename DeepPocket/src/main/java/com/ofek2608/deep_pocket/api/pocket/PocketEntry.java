package com.ofek2608.deep_pocket.api.pocket;

import com.ofek2608.deep_pocket.api.types.EntryType;

public interface PocketEntry {
	Pocket getPocket();
	EntryType getType();
	long getCount();
}
