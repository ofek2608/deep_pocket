package com.ofek2608.deep_pocket.api.pocket_process;

import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.ProvidedResources;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public interface PocketProcessUnit {
	PocketProcessManager getParent();
	int getId();
	int getTypeCount();
	ItemType[] getTypes();
	ItemType getType(int index);
	int getTypeIndex(ItemType type);
	ProvidedResources getResources();

	long getLeftToProvide(int index);
	void setLeftToProvide(int index, long leftToProvide);

	@UnmodifiableView List<PocketProcessRecipe> getRecipes();
	PocketProcessRecipe addRecipe(ItemType result, ItemType[] ingredients);

	void stop();
	void forceStop();

	long supplyItem(ItemType item, long amount);
	boolean executeCrafters(Pocket pocket);
}
