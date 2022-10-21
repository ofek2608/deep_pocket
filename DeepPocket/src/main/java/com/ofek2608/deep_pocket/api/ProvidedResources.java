package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.struct.ItemType;

public interface ProvidedResources {
	int getTypeCount();
	public ItemType[] getTypes();
	ItemType getType(int index);
	long getProvided(int index);
	void provide(int index, long amount);
	long take(int index, long amount);


	long getMaxRequestFromParents(int index);
	long requestFromParent(int index, long amount);
	long requestFromParent(long[] amounts, long maxMultiplier);
	void returnToParent(int index, long amount);
	void returnAllToParent();
	ProvidedResources subProvidedResources(int[] indexes);
	ProvidedResources subProvidedResources();
}
