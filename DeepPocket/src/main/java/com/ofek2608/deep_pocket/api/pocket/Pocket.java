package com.ofek2608.deep_pocket.api.pocket;

import com.ofek2608.deep_pocket.api.Knowledge;
import com.ofek2608.deep_pocket.api.Knowledge0;
import com.ofek2608.deep_pocket.api.ProvidedResources;
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
	@Deprecated(forRemoval = true) long getMaxExtractOld(Map<ItemType,Long> counts);
	@Deprecated(forRemoval = true) long extract(Map<ItemType,Long> counts, long overallCount);
	@Deprecated(forRemoval = true) long extractItem(ItemType type, long count);
	@Deprecated(forRemoval = true) long getMaxExtractOld(ItemType ... items);
	PocketPatterns getPatterns();
	@Nullable CraftingPatternOld getPattern(UUID patternId);
	void removePattern(UUID patternId);
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
		PocketContent.Snapshot getContentSnapshot();
		PocketPatterns.Snapshot getPatternsSnapshot();
	}
}
