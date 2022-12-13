package com.ofek2608.deep_pocket.api.pocket_process;

import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public interface PocketProcessManager {
	@UnmodifiableView List<PocketProcessUnit> getUnits();
	PocketProcessUnit addUnit(ItemType[] types);

	long supplyItem(ItemType item, long amount);
	void executeCrafters(Pocket pocket);

	PocketProcessManager recreate();
}
