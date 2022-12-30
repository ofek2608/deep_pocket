package com.ofek2608.deep_pocket.api.pocket_process;

import com.ofek2608.deep_pocket.api.enums.CrafterStatus;
import com.ofek2608.deep_pocket.api.struct.CraftingPattern;
import com.ofek2608.deep_pocket.api.struct.ElementTypeStack;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.UUID;

public interface ProcessRecipe {
	UUID getPatternId();
	CraftingPattern getPattern();
	
	long getCountNeeded();
	long getCountCrafted();
	long getCountCrafting();
	void setCountNeeded(long count);
	void setCountCrafted(long count);
	void setCountCrafting(long count);
	
	void dumpInventory(List<ElementTypeStack> toClear);
	void requestInventory(List<ElementTypeStack> toFill);
	
	void executeTick();
	boolean isFinished();
	CrafterStatus getStatus();
	
	CompoundTag saveData();
	void loadData(CompoundTag saved);
}
