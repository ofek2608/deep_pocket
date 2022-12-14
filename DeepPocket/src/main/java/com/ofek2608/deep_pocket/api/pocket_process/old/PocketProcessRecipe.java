package com.ofek2608.deep_pocket.api.pocket_process.old;

import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.ProvidedResources;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;
import java.util.UUID;

public interface PocketProcessRecipe {
	PocketProcessUnit getParent();
	ItemType getResult();
	ProvidedResources getResources();

	long getLeftToCraft();
	void setLeftToCraft(long leftToCraft);
	boolean removeLeftToCraft(long crafted);


	@UnmodifiableView List<PocketProcessCrafter> getCrafters();
	PocketProcessCrafter addCrafter(UUID patternId);
	boolean executeCrafters(Pocket pocket);
}
