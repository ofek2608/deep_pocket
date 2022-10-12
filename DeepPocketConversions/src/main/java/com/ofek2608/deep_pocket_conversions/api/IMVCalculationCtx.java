package com.ofek2608.deep_pocket_conversions.api;

import com.ofek2608.deep_pocket.api.struct.ItemType;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public interface IMVCalculationCtx {
	public boolean hasConst(String name);
	public long getConst(String name);
	@UnmodifiableView List<ItemType> getTypes();
	public int getTypeIndex(ItemType type);
	public boolean hasValue(int index);
	public long getValue(int index);
}
