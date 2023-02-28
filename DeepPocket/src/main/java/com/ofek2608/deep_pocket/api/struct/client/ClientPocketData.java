package com.ofek2608.deep_pocket.api.struct.client;

import com.ofek2608.deep_pocket.api.struct.CraftingPattern;
import com.ofek2608.deep_pocket.api.struct.CraftingProcess;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;
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
	
	
	
	@ApiStatus.Internal
	public void readUpdate(FriendlyByteBuf buf) {
		readUpdateCount(buf);
		readUpdateAvailablePatterns(buf);
		readUpdateDefaultPatterns(buf);
		readUpdateActiveCraftingProcesses(buf);
	}
	
	private void readUpdateCount(FriendlyByteBuf buf) {
		int updatedCountLength = buf.readVarInt();
		for (int i = 0; i < updatedCountLength; i++) {
			int elementId = buf.readVarInt();
			long elementCount = buf.readVarLong() - 1;
			if (elementCount < 0) {
				elementCount = -1;
			}
			if (elementCount == 0) {
				counts.remove(elementId);
			} else {
				counts.put(elementId, elementCount);
			}
		}
	}
	
	private void readUpdateAvailablePatterns(FriendlyByteBuf buf) {
		int removedLength = buf.readInt();
		for (int i = 0; i < removedLength; i++) {
			availablePatterns.remove(buf.readUUID());
		}
		int addLength = buf.readVarInt();
		for (int i = 0; i < addLength; i++) {
			availablePatterns.put(buf.readUUID(), CraftingPattern.decode(buf));
		}
	}
	
	private void readUpdateDefaultPatterns(FriendlyByteBuf buf) {
		int addedDefaultPatternsLength = buf.readVarInt();
		for (int i = 0; i < addedDefaultPatternsLength; i++) {
			defaultPattern.put(buf.readVarInt(), buf.readUUID());
		}
	}
	
	private void readUpdateActiveCraftingProcesses(FriendlyByteBuf buf) {
		int nextProcessId = buf.readVarInt();
		
		//Iterate over all the already existing processes, and override finished processes
		int putIndex = 0;
		for (int i = 0; i < activeCraftingProcesses.size(); i++) {
			CraftingProcess process = activeCraftingProcesses.get(i);
			if (nextProcessId == process.getId()) {
				activeCraftingProcesses.set(putIndex++, process);
				process.readUpdate(buf);
				nextProcessId = buf.readVarInt();
			}
		}
		
		//Remove remaining processes after overriding
		//noinspection ListRemoveInLoop
		for (int i = activeCraftingProcesses.size() - 1; i >= putIndex; i--) {
			activeCraftingProcesses.remove(i);
		}
		
		//Add all the new processes
		//process id 0 marks the end
		while (nextProcessId != 0) {
			activeCraftingProcesses.add(CraftingProcess.deserialize(nextProcessId, buf));
			nextProcessId = buf.readVarInt();
		}
	}
}
