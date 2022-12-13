package com.ofek2608.deep_pocket.registry;

import com.ofek2608.deep_pocket.api.pocket.Pocket;
import org.jetbrains.annotations.Nullable;

public interface MenuWithPocket {
	public @Nullable Pocket getPocket();
	public void setPocket(@Nullable Pocket pocket);
}
