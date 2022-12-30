package com.ofek2608.deep_pocket.api.pocket_process.old;

import com.ofek2608.deep_pocket.api.ProvidedResources;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public interface PocketProcessUnit {
	PocketProcessManager getParent();
	int getId();
	int getTypeCount();
	ElementType[] getTypes();
	ElementType getType(int index);
	int getTypeIndex(ElementType type);
	ProvidedResources getResources();

	long getLeftToProvide(int index);
	void setLeftToProvide(int index, long leftToProvide);

	@UnmodifiableView List<PocketProcessRecipe> getRecipes();
	PocketProcessRecipe addRecipe(ItemType result, ItemType[] ingredients);

	void stop();
	void forceStop();

	long supplyItem(ElementType item, long amount);
	boolean executeCrafters(Pocket pocket);
}
