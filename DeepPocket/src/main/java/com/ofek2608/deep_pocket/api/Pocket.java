package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.api.struct.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface Pocket {
	UUID getPocketId();
	UUID getOwner();
	PocketInfo getInfo();
	String getName();
	ItemType getIcon();
	int getColor();
	PocketSecurityMode getSecurityMode();
	void setInfo(PocketInfo pocketInfo);
	boolean canAccess(Player player);
	Map<ItemType,Long> getItemsMap();
	long getItemCount(ItemType type);
	void insertItem(ItemType type, long count);
	long getMaxExtract(@Nullable PlayerKnowledge knowledge, Map<ItemType,Long> counts);
	long extract(@Nullable PlayerKnowledge knowledge, Map<ItemType,Long> counts, long overallCount);
	long extractItem(@Nullable PlayerKnowledge knowledge, ItemType type, long count);
	long getMaxExtract(@Nullable PlayerKnowledge knowledge, ItemType ... items);
	void clearItems();
	Map<UUID,CraftingPattern> getPatternsMap();
	@Nullable CraftingPattern getPattern(UUID patternId);
	UUID addPattern(ItemAmount[] input, ItemTypeAmount[] output, ServerLevel level, BlockPos pos);
	void removePattern(UUID patternId);
	Collection<CraftingPattern> getPatterns();
	Snapshot createSnapshot();
	Pocket copy();

	public interface Snapshot {
		Pocket getPocket();
		boolean didChangedInfo();
		@UnmodifiableView Map<ItemType,Long> getChangedItems();
		CraftingPattern[] getAddedPatterns();
		UUID[] getRemovedPatterns();
	}
}
