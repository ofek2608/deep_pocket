package com.ofek2608.deep_pocket.api.pocket;

import com.ofek2608.deep_pocket.api.enums.PocketAccess;
import com.ofek2608.deep_pocket.api.types.EntryType;

import java.util.UUID;

public interface PocketProperties {
	UUID getPocketId();
	UUID getOwner();
	String getName();
	PocketAccess getAccess();
	EntryType getIcon();
	int getColor();
}
