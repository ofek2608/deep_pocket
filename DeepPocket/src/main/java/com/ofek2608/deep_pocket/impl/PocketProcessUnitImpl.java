package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.DeepPocketHelper;
import com.ofek2608.deep_pocket.api.ProvidedResources;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.pocket_process.old.PocketProcessCrafter;
import com.ofek2608.deep_pocket.api.pocket_process.old.PocketProcessManager;
import com.ofek2608.deep_pocket.api.pocket_process.old.PocketProcessRecipe;
import com.ofek2608.deep_pocket.api.pocket_process.old.PocketProcessUnit;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.RecipeRequest;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.stream.Stream;

final class PocketProcessUnitImpl implements PocketProcessUnit {
	private final PocketProcessManager parent;
	private final int id;
	private final ProvidedResources resources;
	private final Map<ElementType,Integer> typeIndexes;
	private final long[] leftToProvide;
	private final List<PocketProcessRecipe> recipes;

	PocketProcessUnitImpl(DeepPocketHelper helper, PocketProcessManager parent, int id, RecipeRequest[] types) {
		types = types.clone();
		this.parent = parent;
		this.id = id;
		int len = types.length;
		this.resources = helper.createProvidedResources(Stream.of(types).map(RecipeRequest::getResult).toArray(ElementType[]::new));//FIXME
		this.typeIndexes = new HashMap<>();
		for (int i = 0; i < len; i++)
			this.typeIndexes.put(types[i].getResult(), i);
		this.leftToProvide = new long[len];
		this.recipes = new ArrayList<>();
	}

	@Override
	public PocketProcessManager getParent() {
		return parent;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public int getTypeCount() {
		return resources.getTypeCount();
	}

	@Override
	public ElementType[] getTypes() {
		return resources.getTypes();
	}

	@Override
	public ElementType getType(int index) {
		return resources.getType(index);
	}

	@Override
	public int getTypeIndex(ElementType type) {
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
	public void stop() {
		for (PocketProcessRecipe recipe : recipes)
			recipe.setLeftToCraft(0L);
	}

	@Override
	public void forceStop() {
		for (PocketProcessRecipe recipe : recipes) {
			for (PocketProcessCrafter crafter : recipe.getCrafters())
				crafter.getResources().returnAllToParent();
			recipe.getResources().returnAllToParent();
		}
		recipes.clear();
	}

	@Override
	public long supplyItem(ElementType item, long amount) {
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
