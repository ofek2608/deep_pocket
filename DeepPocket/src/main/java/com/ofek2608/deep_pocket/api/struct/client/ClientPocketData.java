package com.ofek2608.deep_pocket.api.struct.client;

import com.ofek2608.deep_pocket.api.struct.CraftingPattern;
import com.ofek2608.deep_pocket.api.struct.CraftingProcess;
import com.ofek2608.deep_pocket.api.struct.PocketUpdate;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public final class ClientPocketData {
	private final Map<Integer, Long> counts = new HashMap<>();
	//TODO change CraftingPattern to use reference of elements
	private final Map<UUID, CraftingPattern> availablePatterns = new HashMap<>();
	private final Map<Integer, UUID> defaultPattern = new HashMap<>();
	private final List<CraftingProcess> activeCraftingProcesses = new ArrayList<>();
	
	public long getCount(int index) {
		return counts.getOrDefault(index, 0L);
	}
	
	public CraftingPattern getAvailablePattern(UUID patternId) {
		return availablePatterns.get(patternId);
	}
	
	public @UnmodifiableView Map<UUID,CraftingPattern> getAllAvailablePatterns() {
		return Collections.unmodifiableMap(availablePatterns);
	}
	
	public UUID getDefaultPattern(int elementId) {
		return defaultPattern.get(elementId);
	}
	
	
	
	public void applyUpdate(PocketUpdate update) {
		// Element Counts
		for (PocketUpdate.ElementCount upd : update.updatedElementCount) {
			if (upd.elementCount() == 0) {
				counts.remove(upd.elementId());
			} else {
				counts.put(upd.elementId(), upd.elementCount() < 0 ? -1 : upd.elementCount());
			}
		}
		// Available Patterns
		for (UUID removedPattern : update.removedPatterns) {
			availablePatterns.remove(removedPattern);
		}
		for (PocketUpdate.AddedPattern addedPattern : update.addedPatterns) {
			availablePatterns.put(addedPattern.patternId(), addedPattern.pattern());
		}
		// Default Patterns
		for (PocketUpdate.DefaultPattern changedDefaultPattern : update.changedDefaultPatterns) {
			defaultPattern.put(changedDefaultPattern.elementId(), changedDefaultPattern.patternId());
		}
		// Crafting Processes
		//Update existing processes
		int putIndex = 0;
		int takeIndex = 0;
		for (PocketUpdate.CraftingProcessUpdate craftingProcessUpdate : update.craftingProcessUpdates) {
			CraftingProcess process;
			do {
				process = activeCraftingProcesses.get(takeIndex++);
			} while (process.getId() != craftingProcessUpdate.processId());
			activeCraftingProcesses.set(putIndex++, process);
			process.applyUpdate(craftingProcessUpdate);
		}
		
		//Remove remaining processes after overriding
		//noinspection ListRemoveInLoop
		for (int i = activeCraftingProcesses.size() - 1; i >= putIndex; i--) {
			activeCraftingProcesses.remove(i);
		}
		
		//Add all the new processes
		for (PocketUpdate.CraftingProcessSetup craftingProcessSetup : update.craftingProcessSetups) {
			activeCraftingProcesses.add(new CraftingProcess(craftingProcessSetup));
		}
	}
}
