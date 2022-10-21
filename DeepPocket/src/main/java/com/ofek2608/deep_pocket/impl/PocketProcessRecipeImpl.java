package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.*;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessCrafter;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessRecipe;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessUnit;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

final class PocketProcessRecipeImpl implements PocketProcessRecipe {
	private final PocketProcessUnit parent;
	private final ItemType result;
	private final ProvidedResources resources;
	private long leftToCraft;
	private final List<PocketProcessCrafter> crafters;

	PocketProcessRecipeImpl(PocketProcessUnit parent, ItemType result, ProvidedResources resources) {
		this.parent = parent;
		this.result = result;
		this.resources = resources;
		this.leftToCraft = 0;
		this.crafters = new ArrayList<>();
	}

	@Override
	public PocketProcessUnit getParent() {
		return parent;
	}

	@Override
	public ItemType getResult() {
		return result;
	}

	@Override
	public ProvidedResources getResources() {
		return resources;
	}

	@Override
	public long getLeftToCraft() {
		return leftToCraft;
	}

	@Override
	public void setLeftToCraft(long leftToCraft) {
		this.leftToCraft = leftToCraft;
	}

	@Override
	public void removeLeftToCraft(long crafted) {
		if (crafted < 0)
			this.leftToCraft = 0;
		else if (0 <= this.leftToCraft)
			this.leftToCraft = Math.max(this.leftToCraft - crafted, 0);
	}

	@Contract(pure = true)
	@Override
	public @UnmodifiableView List<PocketProcessCrafter> getCrafters() {
		return Collections.unmodifiableList(crafters);
	}

	@Override
	public void addCrafter(UUID patternId) {
		this.crafters.add(new PocketProcessCrafterImpl(this, resources.subProvidedResources(), patternId));
	}

	@Override
	public boolean executeCrafters(Pocket pocket) {
		this.crafters.removeIf(crafter->crafter.executeCrafter(pocket));
		if (crafters.size() > 0 && leftToCraft != 0)
			return false;
		crafters.forEach(crafter->crafter.getResources().returnAllToParent());
		resources.returnAllToParent();
		return true;
	}
}
