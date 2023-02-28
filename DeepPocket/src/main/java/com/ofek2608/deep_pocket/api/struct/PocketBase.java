package com.ofek2608.deep_pocket.api.struct;

import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.api.struct.client.ClientPocketData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public class PocketBase {
	@Nonnull private final UUID pocketId;
	@Nonnull private final UUID owner;
	@Nonnull private final PocketInfo info;
	
	public PocketBase(UUID pocketId, UUID owner, PocketInfo info) {
		this.pocketId = pocketId;
		this.owner = owner;
		this.info = info.copy();
	}
	
	public UUID getPocketId() {
		return pocketId;
	}
	
	public UUID getOwner() {
		return owner;
	}
	
	public void setInfo(PocketInfo info) {
		this.info.setFrom(info);
	}
	
	public PocketInfo getInfo() {
		return info.copy();
	}
	
	public String getName() {
		return info.name;
	}
	
	public ElementType getIcon() {
		return info.icon;
	}
	
	public int getColor() {
		return info.color;
	}
	
	public PocketSecurityMode getSecurityMode() {
		return info.securityMode;
	}
}
