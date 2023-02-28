package com.ofek2608.deep_pocket.api.struct.server;

import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.api.struct.*;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.*;

public final class ServerPocket extends PocketBase {
	private ElementConversions conversions;
	private final Map<Integer,Long> elementsCount = new HashMap<>();
	private final Map<CraftingPattern, ServerCraftingPattern> availablePatternsByPattern = new HashMap<>();
	private final Map<UUID, ServerCraftingPattern> availablePatternsById = new HashMap<>();
	private final Map<Integer, UUID> defaultPattern = new HashMap<>();
	private final List<CraftingProcess> craftingProcesses = new ArrayList<>();
	private int nextCraftingProcessId = 1;
	
	private boolean changedInfo = false;
	private final Set<Integer> updatedElementCount = new HashSet<>();
	private final Set<UUID> updatedAvailablePatterns = new HashSet<>();
	private final Set<Integer> updatedDefaultPatterns = new HashSet<>();
	private int lastSentCraftingProcessId = 0;
	
	public ServerPocket(UUID pocketId, UUID owner, PocketInfo info, ElementConversions conversions) {
		super(pocketId, owner, info);
		this.conversions = conversions;
	}
	
	@Override
	public void setInfo(PocketInfo info) {
		super.setInfo(info);
		changedInfo = true;
	}
	
	public boolean didChangeInfo() {
		return changedInfo;
	}
	
	public boolean canAccess(ServerPlayer player) {
		return getSecurityMode().canAccess(player, getOwner());
	}
	
	public ElementConversions getConversions() {
		return conversions;
	}
	
	public void setConversions(ElementConversions conversions) {
		//TODO convert elementCount
		//TODO add elements to updatedElementCount
		this.conversions = conversions;
	}
	
	public long getCount(int elementIndex) {
		return elementsCount.getOrDefault(elementIndex, 0L);
	}
	
	public void setCount(int elementIndex, long count) {
		updatedElementCount.add(elementIndex);
		if (count == 0) {
			elementsCount.remove(elementIndex);
		} else {
			elementsCount.put(elementIndex, count < 0 ? -1 : count);
		}
	}
	
	public @Nullable ServerCraftingPattern getAvailablePattern(CraftingPattern pattern) {
		return availablePatternsByPattern.get(pattern);
	}
	
	public @Nullable ServerCraftingPattern getAvailablePattern(UUID id) {
		return availablePatternsById.get(id);
	}
	
	public ServerCraftingPattern getAvailablePatternOrCreate(CraftingPattern pattern) {
		return availablePatternsByPattern.computeIfAbsent(pattern, p -> {
			UUID id = UUID.randomUUID();
			ServerCraftingPattern craftingPattern = new ServerCraftingPattern(id, p);
			availablePatternsById.put(id, craftingPattern);
			updatedAvailablePatterns.add(id);
			return craftingPattern;
		});
	}
	
	public void setDefaultPattern(int elementId, UUID patternId) {
		defaultPattern.put(elementId, patternId);
		updatedDefaultPatterns.add(elementId);
	}
	
	public @Nullable UUID getDefaultPattern(int elementId) {
		return defaultPattern.get(elementId);
	}
	
	public int getCraftingProcessAmount() {
		return craftingProcesses.size();
	}
	
	public CraftingProcess getCraftingProcess(int index) {
		return craftingProcesses.get(index);
	}
	
	public void addCraftingProcess(CraftingEntry[] entries) {
		craftingProcesses.add(CraftingProcess.create(nextCraftingProcessId++, entries));
	}
	
	public void tick() {
	
	}
	
	public PocketUpdate createUpdate() {
		PocketUpdate update = new PocketUpdate();
		// Element Counts
		for (Integer elementId : updatedElementCount) {
			update.updatedElementCount.add(new PocketUpdate.ElementCount(elementId, elementsCount.get(elementId)));
		}
		// Available Patterns
		for (UUID patternId : updatedAvailablePatterns) {
			ServerCraftingPattern pattern = availablePatternsById.get(patternId);
			if (pattern == null) {
				update.removedPatterns.add(patternId);
			} else {
				update.addedPatterns.add(new PocketUpdate.AddedPattern(patternId, pattern.pattern));
			}
		}
		// Default Patterns
		for (int patternId : updatedDefaultPatterns) {
			update.changedDefaultPatterns.add(new PocketUpdate.DefaultPattern(patternId, defaultPattern.getOrDefault(patternId, Util.NIL_UUID)));
		}
		// Crafting Processes
		for (CraftingProcess process : craftingProcesses) {
			if (process.getId() <= lastSentCraftingProcessId) {
				update.craftingProcessUpdates.add(process.createUpdate());
			} else {
				update.craftingProcessSetups.add(process.createSetup());
			}
		}
		
		return update;
	}
	
	public PocketUpdate createSetup() {
		PocketUpdate update = new PocketUpdate();
		// Element Counts
		elementsCount.forEach((elementId, count) -> {
			update.updatedElementCount.add(new PocketUpdate.ElementCount(elementId, count));
		});
		// Available Patterns
		availablePatternsById.forEach((patternId, pattern) -> {
			update.addedPatterns.add(new PocketUpdate.AddedPattern(patternId, pattern.pattern));
		});
		// Default Patterns
		defaultPattern.forEach((elementId, patternId) -> {
			update.changedDefaultPatterns.add(new PocketUpdate.DefaultPattern(elementId, patternId));
		});
		// Crafting Processes
		for (CraftingProcess process : craftingProcesses) {
			update.craftingProcessSetups.add(process.createSetup());
		}
		
		return update;
	}
	
	public void clearUpdates() {
		changedInfo = false;
		updatedElementCount.clear();
		updatedAvailablePatterns.clear();
		updatedDefaultPatterns.clear();
		lastSentCraftingProcessId = nextCraftingProcessId - 1;
	}
	
	
	
	public static CompoundTag save(ServerPocket pocket) {
		CompoundTag saved = new CompoundTag();
		saved.putUUID("pocketId", pocket.getPocketId());
		saved.putUUID("owner", pocket.getOwner());
		saved.put("info", PocketInfo.save(pocket.getInfo()));
		
		// save element count
		CompoundTag savedElementsCounts = new CompoundTag();
		pocket.elementsCount.forEach((elementId, elementCount) ->
				savedElementsCounts.putLong("" + elementCount, elementCount)
		);
		saved.put("elements", savedElementsCounts);
		
		// save available patterns
		ListTag savedAvailablePatterns = new ListTag();
		pocket.availablePatternsById.forEach((patternId, pattern) -> {
			CompoundTag savedPattern = CraftingPattern.save(pattern.pattern);
			ListTag savedPatternLocations = new ListTag();
			for (LevelBlockPos position : pattern.positions) {
				savedPatternLocations.add(LevelBlockPos.save(position));
			}
			savedPattern.put("positions", savedPatternLocations);
			savedPattern.putUUID("patternId", patternId);
			savedAvailablePatterns.add(savedPattern);
		});
		saved.put("availablePatterns", savedAvailablePatterns);
		
		// save default patterns
		CompoundTag savedDefaultPatterns = new CompoundTag();
		pocket.defaultPattern.forEach((elementId, patternId) ->
				savedDefaultPatterns.putUUID("" + elementId, patternId)
		);
		saved.put("defaultPatterns", savedDefaultPatterns);
		
		// save crafting processes
		ListTag savedCraftingProcesses = new ListTag();
		for (CraftingProcess craftingProcess : pocket.craftingProcesses) {
			savedCraftingProcesses.add(CraftingProcess.save(craftingProcess));
		}
		saved.put("craftingProcesses", savedCraftingProcesses);
		
		return saved;
	}
	
	public static ServerPocket load(CompoundTag tag, boolean allowPublicPockets, ElementConversions conversions) {
		ServerPocket pocket = new ServerPocket(
				tag.getUUID("pocketId"),
				tag.getUUID("owner"),
				PocketInfo.load(tag.getCompound("info")),
				conversions
		);
		// disable public pocket if the settings had changed
		if (!allowPublicPockets && pocket.getSecurityMode() == PocketSecurityMode.PUBLIC) {
			PocketInfo info = pocket.getInfo();
			info.securityMode = PocketSecurityMode.TEAM;
			pocket.setInfo(info);
		}
		// load element count
		// TODO load
		// load available patterns
		// TODO load
		// load default patterns
		// TODO load
		// load crafting processes
		// TODO load
		return pocket;
	}
}
