package com.ofek2608.deep_pocket.api.pocket;

import com.ofek2608.deep_pocket.api.types.EntryType;

public interface ModifiablePocketEntry extends PocketEntry {
	ModifiablePocket getPocket();
	boolean setCount(EntryType type, long count);
}
