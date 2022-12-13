package com.ofek2608.deep_pocket.api.pocket;

import com.ofek2608.deep_pocket.api.struct.CraftingPattern;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.LevelBlockPos;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public interface PocketPatterns {
	UUID add(CraftingPattern pattern, LevelBlockPos pos);
	boolean remove(UUID patternId, LevelBlockPos pos);
	@Nullable CraftingPattern get(@Nullable UUID patternId);
	@Nullable UUID getId(@Nullable CraftingPattern pattern);
	@UnmodifiableView List<LevelBlockPos> getPositions(@Nullable UUID patternId);
	void clear();
	@UnmodifiableView Set<UUID> getAllPatterns();
	Map<ElementType, Optional<UUID>> getDefaultsMap();
	Optional<UUID> getDefault(ElementType type);
	void setDefault(ElementType type, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<UUID> patternId);
	Snapshot createSnapshot();
	PocketPatterns copy();
	@ApiStatus.Internal
	void put(UUID patternId, CraftingPattern pattern);
	
	interface Snapshot {
		@UnmodifiableView Map<UUID,CraftingPattern> getAdded();
		@UnmodifiableView Set<UUID> getRemoved();
		@UnmodifiableView Map<ElementType,Optional<UUID>> getAddedDefaults();
		ElementType[] getRemovedDefaults();
	}
}
