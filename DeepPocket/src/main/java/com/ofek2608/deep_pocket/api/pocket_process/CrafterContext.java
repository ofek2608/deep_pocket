package com.ofek2608.deep_pocket.api.pocket_process;

import com.ofek2608.deep_pocket.api.enums.CrafterStatus;
import com.ofek2608.deep_pocket.api.struct.CraftingPattern;
import com.ofek2608.deep_pocket.api.struct.ElementTypeStack;
import com.ofek2608.deep_pocket.api.struct.LevelBlockPos;

import java.util.List;
import java.util.UUID;

public interface CrafterContext {
	ProcessRecipe getParent();
	UUID getPatternId();
	CraftingPattern getPattern();
	LevelBlockPos getPosition();
	CrafterStatus getStatus();
	void setStatus(CrafterStatus status);
	
	void startCrafting();
	void cancelCrafting();
	void notifyFinishedCraft();
	boolean isCrafting();
	List<ElementTypeStack> getInventory();
	
	
	void instantCraftOne();
	void instantCraftAll();
	
}
