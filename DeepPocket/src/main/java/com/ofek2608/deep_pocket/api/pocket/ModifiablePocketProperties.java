package com.ofek2608.deep_pocket.api.pocket;

import com.ofek2608.deep_pocket.api.enums.PocketAccess;
import com.ofek2608.deep_pocket.api.types.EntryType;

public interface ModifiablePocketProperties extends PocketProperties {
	void setName(String name);
	void setAccess(PocketAccess access);
	void setIcon(EntryType type);
	void setColor(int color);
}
