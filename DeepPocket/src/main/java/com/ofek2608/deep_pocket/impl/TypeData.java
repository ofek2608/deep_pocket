package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.utils.LNUtils;
import com.ofek2608.deep_pocket.api.types.EntryType;
import net.minecraft.nbt.CompoundTag;

public final class TypeData {
	public final EntryType type;
	public long count;
	public boolean changedCount;
	
	
	public TypeData(EntryType type) {
		this.type = type;
	}
	
	public static TypeData load(CompoundTag saved) {
		TypeData typeData = new TypeData(
				EntryType.load(saved.getCompound("type"))
		);
		
		typeData.count = LNUtils.normalize(saved.getLong("count"));
		
		return typeData;
	}
	
	public static CompoundTag save(TypeData data) {
		CompoundTag saved = new CompoundTag();
		
		saved.put("type", EntryType.save(data.type));
		saved.putLong("count", data.count);
		
		return saved;
	}
}
