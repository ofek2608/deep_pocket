package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.DeepPocketHelper;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessManager;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessUnit;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class PocketProcessManagerImpl implements PocketProcessManager {
	private final DeepPocketHelper helper;
	private final List<PocketProcessUnit> units = new ArrayList<>();

	PocketProcessManagerImpl(DeepPocketHelper helper) {
		this.helper = helper;
	}

	@Override
	public @UnmodifiableView List<PocketProcessUnit> getUnits() {
		return Collections.unmodifiableList(units);
	}

	@Override
	public PocketProcessUnit addUnit(ItemType[] types) {
		PocketProcessUnit unit = new PocketProcessUnitImpl(helper, this, types);
		units.add(unit);
		return unit;
	}

	@Override
	public long supplyItem(ItemType item, long amount) {
		for (PocketProcessUnit unit : units) {
			if (amount == 0)
				return 0;
			amount = unit.supplyItem(item, amount);
		}
		return amount;
	}

	@Override
	public void executeCrafters(Pocket pocket) {
		var finishedUnits = units.stream().filter(unit->unit.executeCrafters(pocket)).toList();
		units.removeAll(finishedUnits);
		for (PocketProcessUnit finishedUnit : finishedUnits)
			pocket.insertAll(finishedUnit.getResources());
	}

	@Override
	public PocketProcessManager recreate() {
		return new PocketProcessManagerImpl(helper);
	}
}
