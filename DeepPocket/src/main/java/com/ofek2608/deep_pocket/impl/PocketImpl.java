package com.ofek2608.deep_pocket.impl;

import com.mojang.logging.LogUtils;
import com.ofek2608.deep_pocket.api.utils.LNUtils;
import com.ofek2608.deep_pocket.api.pocket.ModifiablePocket;
import com.ofek2608.deep_pocket.api.pocket.ModifiablePocketEntry;
import com.ofek2608.deep_pocket.api.pocket.ModifiablePocketProperties;
import com.ofek2608.deep_pocket.api.types.EntryType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public final class PocketImpl implements ModifiablePocket {
	private static final Logger LOGGER = LogUtils.getLogger();
	public final PocketPropertiesImpl properties;
	public final Map<EntryType, TypeData> typeData;
	
	public PocketImpl(PocketPropertiesImpl properties) {
		this.properties = properties;
		this.typeData = new HashMap<>();
	}
	
	public static PocketImpl load(CompoundTag saved) {
		//properties
		PocketPropertiesImpl properties = PocketPropertiesImpl.load(saved.getCompound("properties"));
		
		PocketImpl pocket = new PocketImpl(properties);
		
		//typeData
		ListTag savedTypeData = saved.getList("typeData", Tag.TAG_COMPOUND);
		for (int i = 0; i < savedTypeData.size(); i++) {
			try {
				CompoundTag savedTypeDataI = savedTypeData.getCompound(i);
				TypeData typeDataI = TypeData.load(savedTypeDataI);
				pocket.typeData.put(typeDataI.type, typeDataI);
			} catch (Exception e) {
				LOGGER.warn("Couldn't load pocket's typeData", e);
			}
		}
		
		return pocket;
	}
	
	public static CompoundTag save(PocketImpl pocket) {
		CompoundTag saved = new CompoundTag();
		
		//properties
		saved.put("properties", PocketPropertiesImpl.save(pocket.properties));
		
		//typeData
		ListTag savedTypeData = new ListTag();
		for (TypeData typeDataI : pocket.typeData.values()) {
			savedTypeData.add(TypeData.save(typeDataI));
		}
		saved.put("typeData", savedTypeData);
		
		return saved;
	}
	
	@Override
	public ModifiablePocketProperties getProperties() {
		return properties;
	}
	
	@Override
	public ModifiablePocketEntry getEntry(EntryType type) {
		return new PocketEntryImpl(type);
	}
	
	public TypeData getTypeData(EntryType type) {
		return typeData.computeIfAbsent(type, TypeData::new);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	private final class PocketEntryImpl implements ModifiablePocketEntry {
		private final EntryType type;
		
		public PocketEntryImpl(EntryType type) {
			this.type = type;
		}
		
		@Override
		public ModifiablePocket getPocket() {
			return PocketImpl.this;
		}
		
		@Override
		public EntryType getType() {
			return type;
		}
		
		@Override
		public long getCount() {
			return getTypeData(type).count;
		}
		
		@Override
		public boolean setCount(EntryType type, long count) {
			count = LNUtils.normalize(count);
			TypeData typeData = getTypeData(type);
			if (typeData.count == count) {
				return false;
			}
			typeData.count = count;
			typeData.changedCount = true;
			return true;
		}
	}
}
