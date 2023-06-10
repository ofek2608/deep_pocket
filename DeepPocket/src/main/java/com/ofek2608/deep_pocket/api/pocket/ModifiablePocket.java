package com.ofek2608.deep_pocket.api.pocket;

import com.ofek2608.deep_pocket.api.types.EntryType;

public interface ModifiablePocket extends Pocket {
	ModifiablePocketProperties getProperties();
	ModifiablePocketEntry getEntry(EntryType type);
}
