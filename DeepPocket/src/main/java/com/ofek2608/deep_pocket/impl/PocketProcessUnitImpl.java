package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.*;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessManager;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessRecipe;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessUnit;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.stream.Stream;

final class PocketProcessUnitImpl implements PocketProcessUnit {
	private final PocketProcessManager parent;
	private final ProvidedResources resources;
	private final Map<ItemType,Integer> typeIndexes;
	private final long[] leftToProvide;
	private final List<PocketProcessRecipe> recipes;

	PocketProcessUnitImpl(DeepPocketHelper helper, PocketProcessManager parent, ItemType[] types) {
		types = types.clone();
		this.parent = parent;
		int len = types.length;
		this.resources = helper.createProvidedResources(types);
		this.typeIndexes = new HashMap<>();
		for (int i = 0; i < len; i++)
			this.typeIndexes.put(types[i], i);
		this.leftToProvide = new long[len];
		this.recipes = new ArrayList<>();
	}

	@Override
	public PocketProcessManager getParent() {
		return parent;
	}

	@Override
	public int getTypeCount() {
		return resources.getTypeCount();
	}

	@Override
	public ItemType[] getTypes() {
		return resources.getTypes();
	}

	@Override
	public ItemType getType(int index) {
		return resources.getType(index);
	}

	@Override
	public int getTypeIndex(ItemType type) {
		return typeIndexes.get(type);
	}

	@Override
	public ProvidedResources getResources() {
		return resources;
	}

	@Override
	public long getLeftToProvide(int index) {
		return leftToProvide[index];
	}

	@Override
	public void setLeftToProvide(int index, long leftToProvide) {
		this.leftToProvide[index] = leftToProvide < 0 ? -1 : leftToProvide;
	}

	@Override
	public @UnmodifiableView List<PocketProcessRecipe> getRecipes() {
		return Collections.unmodifiableList(this.recipes);
	}

	@Override
	public PocketProcessRecipe addRecipe(ItemType result, ItemType[] ingredients) {
		int[] indexes = Stream.of(ingredients).map(typeIndexes::get).mapToInt(i->{
			if (i == null)
				throw new IllegalArgumentException("ingredients");
			return i;
		}).toArray();
		PocketProcessRecipe recipe = new PocketProcessRecipeImpl(this, result, resources.subProvidedResources(indexes));
		recipes.add(recipe);
		return recipe;
	}

	@Override
	public long supplyItem(ItemType item, long amount) {
		if (amount == 0)
			return 0;
		int index = typeIndexes.getOrDefault(item, -1);
		if (index < 0)
			return amount < 0 ? -1 : amount;
		if (amount < 0) {
			resources.provide(index, leftToProvide[index]);
			leftToProvide[index] = 0;
			return -1;
		}
		if (leftToProvide[index] < 0) {
			resources.provide(index, amount);
			if (resources.getProvided(index) < 0)
				leftToProvide[index] = 0;
			return 0;
		}
		if (amount <= leftToProvide[index]) {
			resources.provide(index, amount);
			leftToProvide[index] -= amount;
			return 0;
		}
		long leftOver = amount - leftToProvide[index];
		resources.provide(index, leftToProvide[index]);
		leftToProvide[index] = 0;
		return leftOver;
	}

	@Override
	public boolean executeCrafters(Pocket pocket) {
		recipes.removeIf(recipe->recipe.executeCrafters(pocket));
		return recipes.size() == 0;
	}
}
