package com.ofek2608.deep_pocket.api.struct.server;

import com.ofek2608.deep_pocket.api.struct.*;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.*;

public final class ServerPocket extends PocketBase {
	private final Map<Integer,Long> elementCount = new HashMap<>();
	private final Map<CraftingPattern, ServerCraftingPattern> availablePatternsByPattern = new HashMap<>();
	private final Map<UUID, ServerCraftingPattern> availablePatternsById = new HashMap<>();
	private final Map<Integer, UUID> defaultPattern = new HashMap<>();
	private final List<CraftingProcess> craftingProcesses = new ArrayList<>();
	private int nextCraftingProcessId = 1;
	
	private final Set<Integer> updatedElementCount = new HashSet<>();
	private final Set<UUID> updatedAvailablePatterns = new HashSet<>();
	private final Set<Integer> updatedDefaultPatterns = new HashSet<>();
	private int lastSentCraftingProcessId = 0;
	
	public ServerPocket(UUID pocketId, UUID owner, PocketInfo info) {
		super(pocketId, owner, info);
	}
	
	public long getCount(int elementIndex) {
		return elementCount.getOrDefault(elementIndex, 0L);
	}
	
	public void setCount(int elementIndex, long count) {
		updatedElementCount.add(elementIndex);
		if (count == 0) {
			elementCount.remove(elementIndex);
		} else {
			elementCount.put(elementIndex, count < 0 ? -1 : count);
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
	
	@ApiStatus.Internal
	public void tick() {
	
	}
	
	@ApiStatus.Internal
	public void writeUpdate(FriendlyByteBuf buf) {
		writeUpdateCount(buf);
		writeUpdateAvailablePatterns(buf);
		writeUpdateDefaultPatterns(buf);
		writeUpdateActiveCraftingProcesses(buf);
	}
	
	@ApiStatus.Internal
	public void writeSetup(FriendlyByteBuf buf) {
		writeSetupCount(buf);
		writeSetupAvailablePatterns(buf);
		writeSetupDefaultPatterns(buf);
		writeSetupActiveCraftingProcesses(buf);
	}
	
	@ApiStatus.Internal
	public void clearUpdates() {
		updatedElementCount.clear();
		updatedAvailablePatterns.clear();
		updatedDefaultPatterns.clear();
		lastSentCraftingProcessId = nextCraftingProcessId - 1;
	}
	
	private void writeUpdateCount(FriendlyByteBuf buf) {
		buf.writeVarInt(updatedElementCount.size());
		for (Integer elementId : updatedElementCount) {
			buf.writeVarInt(elementId);
			buf.writeVarLong(elementCount.get(elementId) + 1);
		}
	}
	
	private void writeUpdateAvailablePatterns(FriendlyByteBuf buf) {
		List<UUID> removedCraftingPatternsId = new ArrayList<>();
		List<UUID> addedCraftingPatternsId = new ArrayList<>();
		List<CraftingPattern> addedCraftingPatterns = new ArrayList<>();
		for (UUID patternId : updatedAvailablePatterns) {
			ServerCraftingPattern pattern = availablePatternsById.get(patternId);
			if (pattern == null) {
				removedCraftingPatternsId.add(patternId);
			} else {
				addedCraftingPatternsId.add(patternId);
				addedCraftingPatterns.add(pattern.pattern);
			}
		}
		buf.writeVarInt(removedCraftingPatternsId.size());
		for (UUID patternId : removedCraftingPatternsId) {
			buf.writeUUID(patternId);
		}
		buf.writeVarInt(addedCraftingPatternsId.size());
		for (int i = 0; i < addedCraftingPatternsId.size(); i++) {
			buf.writeUUID(addedCraftingPatternsId.get(i));
			CraftingPattern.encode(buf, addedCraftingPatterns.get(i));
		}
	}
	
	private void writeUpdateDefaultPatterns(FriendlyByteBuf buf) {
		buf.writeVarInt(updatedDefaultPatterns.size());
		for (int patternId : updatedDefaultPatterns) {
			buf.writeVarInt(patternId);
			buf.writeUUID(defaultPattern.getOrDefault(patternId, Util.NIL_UUID));
		}
	}
	
	private void writeUpdateActiveCraftingProcesses(FriendlyByteBuf buf) {
		for (CraftingProcess process : craftingProcesses) {
			buf.writeVarInt(process.getId());
			if (process.getId() <= lastSentCraftingProcessId) {
				process.sendUpdate(buf);
			} else {
				CraftingProcess.serialize(buf, process);
			}
		}
		//process id 0 marks the end
		buf.writeVarInt(0);
	}
	
	
	
	
	
	private void writeSetupCount(FriendlyByteBuf buf) {
		buf.writeVarInt(elementCount.size());
		elementCount.forEach((elementId, count) -> {
			buf.writeVarInt(elementId);
			buf.writeVarLong(count + 1);
		});
	}
	
	private void writeSetupAvailablePatterns(FriendlyByteBuf buf) {
		//0 removed patterns
		buf.writeVarInt(0);
		//add all patterns
		buf.writeVarInt(availablePatternsById.size());
		availablePatternsById.forEach((patternId, pattern) -> {
			buf.writeUUID(patternId);
			CraftingPattern.encode(buf, pattern.pattern);
		});
	}
	
	private void writeSetupDefaultPatterns(FriendlyByteBuf buf) {
		buf.writeVarInt(defaultPattern.size());
		defaultPattern.forEach((elementId, patternId) -> {
			buf.writeVarInt(elementId);
			buf.writeUUID(patternId);
		});
	}
	
	private void writeSetupActiveCraftingProcesses(FriendlyByteBuf buf) {
		for (CraftingProcess process : craftingProcesses) {
			buf.writeVarInt(process.getId());
			CraftingProcess.serialize(buf, process);
		}
		//process id 0 marks the end
		buf.writeVarInt(0);
	}
}
