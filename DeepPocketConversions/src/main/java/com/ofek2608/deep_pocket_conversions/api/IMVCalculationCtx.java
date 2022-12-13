package com.ofek2608.deep_pocket_conversions.api;

import com.ofek2608.deep_pocket.api.struct.ItemType;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public interface IMVCalculationCtx {
	boolean hasConst(String name);
	long getConst(String name);
	@UnmodifiableView List<ItemType> getTypes();
	int getTypeIndex(ItemType type);
	boolean hasValue(int index);
	long getValue(int index);
}
