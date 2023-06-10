package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.enums.PocketAccess;
import com.ofek2608.deep_pocket.api.pocket.ModifiablePocketProperties;
import com.ofek2608.deep_pocket.api.types.EntryType;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public final class PocketPropertiesImpl implements ModifiablePocketProperties {
	public final UUID pocketId;
	public final UUID owner;
	public String name;
	public PocketAccess access;
	public EntryType icon;
	public int color;
	public boolean isChanged;
	
	public PocketPropertiesImpl(UUID pocketId, UUID owner, String name, PocketAccess access, EntryType icon, int color) {
		this.pocketId = pocketId;
		this.owner = owner;
		this.name = name;
		this.access = access;
		this.icon = icon;
		this.color = color & 0xFFFFFF;
	}
	
	public static PocketPropertiesImpl load(CompoundTag data) {
		return new PocketPropertiesImpl(
				data.getUUID("pocketId"),
				data.getUUID("owner"),
				data.getString("name"),
				PocketAccess.valueOf(data.getString("access").toUpperCase()),
				EntryType.load(data.getCompound("icon")),
				data.getInt("color") & 0xFFFFFF
		);
	}
	
	public static CompoundTag save(PocketPropertiesImpl properties) {
		CompoundTag result = new CompoundTag();
		result.putUUID("pocketId", properties.pocketId);
		result.putUUID("owner", properties.owner);
		result.putString("name", properties.name);
		result.putString("access", properties.access.name().toLowerCase());
		result.put("icon", EntryType.save(properties.icon));
		result.putInt("color", properties.color);
		return result;
	}
	
	@Override
	public UUID getPocketId() {
		return pocketId;
	}
	
	@Override
	public UUID getOwner() {
		return owner;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public PocketAccess getAccess() {
		return access;
	}
	
	@Override
	public EntryType getIcon() {
		return icon;
	}
	
	@Override
	public int getColor() {
		return color;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
		this.isChanged = true;
	}
	
	@Override
	public void setAccess(PocketAccess access) {
		this.access = access;
		this.isChanged = true;
	}
	
	@Override
	public void setIcon(EntryType icon) {
		this.icon = icon;
		this.isChanged = true;
	}
	
	@Override
	public void setColor(int color) {
		this.color = color & 0xFFFFFF;
		this.isChanged = true;
	}
}
