package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessManager;
import com.ofek2608.deep_pocket.api.struct.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface Pocket {
	UUID getPocketId();
	UUID getOwner();
	PocketInfo getInfo();
	void setInfo(PocketInfo pocketInfo);
	String getName();
	ItemType getIcon();
	int getColor();
	PocketSecurityMode getSecurityMode();
	void setConversions(ElementConversions conversions);

	boolean canAccess(Player player);
	PocketContent getContent();
	void insertElement(ElementType type, long count);
	long getMaxExtract(@Nullable Knowledge0 knowledge, Map<ElementType,Long> counts);
	long getMaxExtract(@Nullable Knowledge0 knowledge, ElementType ... items);
	long extractItem(@Nullable Knowledge0 knowledge, ElementType type, long count);
	
	@Deprecated(forRemoval = true) Map<ItemType,Long> getItemsMap();
	@Deprecated(forRemoval = true) long getItemCount(ItemType type);
	@Deprecated(forRemoval = true) void insertItem(ItemType type, long count);
	void insertAll(ProvidedResources resources);
	@Deprecated(forRemoval = true) long getMaxExtractOld(@Nullable Knowledge knowledge, Map<ItemType,Long> counts);
	@Deprecated(forRemoval = true) long extract(@Nullable Knowledge knowledge, Map<ItemType,Long> counts, long overallCount);
	@Deprecated(forRemoval = true) long extractItem(@Nullable Knowledge knowledge, ItemType type, long count);
	@Deprecated(forRemoval = true) long getMaxExtractOld(@Nullable Knowledge knowledge, ItemType ... items);
	Map<UUID,CraftingPattern> getPatternsMap();
	@Nullable CraftingPattern getPattern(UUID patternId);
	UUID addPattern(ItemTypeAmount[] input, ItemTypeAmount[] output, ServerLevel level, BlockPos pos);
	void removePattern(UUID patternId);
	Map<ItemType,Optional<UUID>> getDefaultPatternsMap();
	Optional<UUID> getDefaultPattern(ItemType type);
	void setDefaultPattern(ItemType type, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<UUID> patternId);
	PocketProcessManager getProcesses();
	Snapshot createSnapshot();
	Pocket copy();
	
	Stream<Entry> entries();
	
	interface Entry {
		Pocket getPocket();
		ElementType getType();
		long getMaxExtract();
		void insert(long amount);
		long extract(long amount);
		boolean canBeCrafted();
		boolean canBeConverted();
	}

	interface Snapshot {
		Pocket getPocket();
		boolean didChangedInfo();
		public PocketContent.Snapshot getContentSnapshot();
		CraftingPattern[] getAddedPatterns();
		@UnmodifiableView Map<ItemType,Optional<UUID>> getAddedDefaultPatterns();
		ItemType[] getRemovedDefaultPatterns();
		UUID[] getRemovedPatterns();
	}
}
