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
import java.util.UUID;

public interface Pocket {
	UUID getPocketId();
	UUID getOwner();
	PocketInfo getInfo();
	void setInfo(PocketInfo pocketInfo);
	String getName();
	ItemType getIcon();
	int getColor();
	PocketSecurityMode getSecurityMode();

	boolean canAccess(Player player);
	Map<ItemType,Long> getItemsMap();
	long getItemCount(ItemType type);
	void insertItem(ItemType type, long count);
	void insertAll(ProvidedResources resources);
	long getMaxExtract(@Nullable Knowledge knowledge, Map<ItemType,Long> counts);
	long extract(@Nullable Knowledge knowledge, Map<ItemType,Long> counts, long overallCount);
	long extractItem(@Nullable Knowledge knowledge, ItemType type, long count);
	long getMaxExtract(@Nullable Knowledge knowledge, ItemType ... items);
	Map<UUID,CraftingPattern> getPatternsMap();
	@Nullable CraftingPattern getPattern(UUID patternId);
	UUID addPattern(ItemAmount[] input, ItemTypeAmount[] output, ServerLevel level, BlockPos pos);
	void removePattern(UUID patternId);
	PocketProcessManager getProcesses();
	Snapshot createSnapshot();
	Pocket copy();

	interface Snapshot {
		Pocket getPocket();
		boolean didChangedInfo();
		@UnmodifiableView Map<ItemType,Long> getChangedItems();
		CraftingPattern[] getAddedPatterns();
		UUID[] getRemovedPatterns();
	}
}
