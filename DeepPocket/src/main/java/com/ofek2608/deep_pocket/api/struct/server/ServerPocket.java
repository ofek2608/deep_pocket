package com.ofek2608.deep_pocket.api.struct.server;

import com.ofek2608.deep_pocket.api.Knowledge;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.api.struct.*;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.IntUnaryOperator;

import static com.ofek2608.deep_pocket.utils.AdvancedLongMath.*;

public final class ServerPocket extends PocketBase {
	private final ElementConversions conversions;
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
	
	public ElementConversions getConversions() {
		return conversions;
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
		//TODO
	}
	
	//=====================
	// Content interaction
	//=====================
	
	public long getElementCount(int elementId) {
		return elementsCount.getOrDefault(elementId, 0L);
	}
	
	public void setElementCount(int elementId, long count) {
		updatedElementCount.add(elementId);
		if (count == 0) {
			elementsCount.remove(elementId);
		} else {
			elementsCount.put(elementId, count < 0 ? -1 : count);
		}
	}
	
	public void clearElement(int elementId) {
		setElementCount(elementId, 0);
	}
	
	public void insertBaseElements(long[] counts, long multiplier) {
		for (int i = 0; i < counts.length; i++) {
			if (counts[i] != 0)
				continue;
			int elementId = conversions.getBaseElement(i);
			setElementCount(
					elementId,
					advancedSum(getElementCount(elementId), advancedMul(multiplier, counts[i]))
			);
		}
	}
	
	public void insertElement(int elementId, long count) {
		if (elementId == 0)
			return;
		long[] valueCount = conversions.getValue(elementId);
		if (valueCount == null) {
			setElementCount(elementId, advancedSum(getElementCount(elementId), count));
		} else {
			insertBaseElements(valueCount, count);
		}
	}
	
	public void insertElement(ElementIdStack stack) {
		insertElement(stack.elementId(), stack.count());
	}
	
	public long extractBaseElement(long[] counts, long maxMultiplier) {
		maxMultiplier = advancedMin(maxMultiplier, getMaxExtract(counts));
		if (maxMultiplier == 0)
			return 0;
		for (int i = 0; i < counts.length; i++) {
			int elementId = conversions.getBaseElement(i);
			setElementCount(elementId, advancedSub(getElementCount(elementId), advancedMul(counts[i], maxMultiplier)));
		}
		return maxMultiplier;
	}
	
	public boolean extractBaseElement(long[] baseElements) {
		return extractBaseElement(baseElements, 1) == 1;
	}
	
	public long extractElement(int elementId, long count) {
		if (elementId == 0 || count == 0)
			return 0;
		
		long[] valueCount = conversions.getValue(elementId);
		if (valueCount != null)
			return extractBaseElement(valueCount, count);
		
		long currentCount = getElementCount(elementId);
		count = advancedMin(count, currentCount);
		setElementCount(elementId, advancedSub(currentCount, count));
		return count;
	}
	
	public long extractElement(ElementIdStack stack) {
		return extractElement(stack.elementId(), stack.count());
	}
	
	public boolean extractElement(int elementId) {
		return extractElement(elementId, 1) == 1;
	}
	
	public long getMaxExtract(long[] baseElements) {
		long maxExtract = -1;
		for (int i = 0; i < baseElements.length; i++) {
			if (baseElements[i] == 0)
				continue;
			long current = advancedDiv(getElementCount(conversions.getBaseElement(i)), baseElements[i]);
			maxExtract = advancedMin(maxExtract, current);
		}
		return maxExtract;
	}
	
	private long getMaxExtract0(@Nullable Knowledge knowledge, Map<Integer, Long> counts) {
		long[] baseElementsRequirement = conversions.convertMapToArray(counts);
		long maxExtract = getMaxExtract(baseElementsRequirement);
		for (var entry : counts.entrySet()) {
			if (knowledge != null && !knowledge.contains(entry.getKey()))
				return 0L;
			long current = advancedDiv(getElementCount(entry.getKey()), entry.getValue());
			maxExtract = advancedMin(maxExtract, current);
		}
		return maxExtract;
	}
	
	public long getMaxExtract(@Nullable Knowledge knowledge, Map<Integer,Long> counts) {
		return getMaxExtract0(knowledge, new HashMap<>(counts));
	}
	
	public long getMaxExtract(@Nullable Knowledge knowledge, int ... elementIds) {
		Map<Integer, Long> map = new HashMap<>();
		for (int elementId : elementIds)
			map.put(elementId, advancedSum(map.getOrDefault(elementId, 0L), 1L));
		return getMaxExtract0(knowledge, map);
	}
	
	public long getMaxExtract(@Nullable Knowledge knowledge, ElementIdStack ... stacks) {
		Map<Integer, Long> map = new HashMap<>();
		for (ElementIdStack stack : stacks)
			map.put(stack.elementId(), advancedSum(map.getOrDefault(stack.elementId(), 0L), stack.count()));
		return getMaxExtract0(knowledge, map);
	}
	
	public void clearElement() {
		updatedElementCount.addAll(elementsCount.keySet());
		elementsCount.clear();
	}
	
	
	
	
	
	
	
	
	
	
	//=========================
	// Serialization to Client
	//=========================
	
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
	
	
	
	
	//====================
	// JSON Serialization
	//====================
	
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
			ListTag savedPatternPositions = new ListTag();
			for (LevelBlockPos position : pattern.positions) {
				savedPatternPositions.add(LevelBlockPos.save(position));
			}
			savedPattern.put("positions", savedPatternPositions);
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
	
	public static ServerPocket load(CompoundTag saved, boolean allowPublicPockets, ElementConversions conversions, IntUnaryOperator elementIdGetter) {
		ServerPocket pocket = new ServerPocket(
				saved.getUUID("pocketId"),
				saved.getUUID("owner"),
				PocketInfo.load(saved.getCompound("info")),
				conversions
		);
		// disable public pocket if the settings had changed
		if (!allowPublicPockets && pocket.getSecurityMode() == PocketSecurityMode.PUBLIC) {
			PocketInfo info = pocket.getInfo();
			info.securityMode = PocketSecurityMode.TEAM;
			pocket.setInfo(info);
		}
		// load element count
		CompoundTag savedElements = saved.getCompound("elements");
		savedElements.getAllKeys().forEach(key -> {
			int elementIndex;
			try {
				elementIndex = Integer.parseInt(key);
			} catch (NumberFormatException e) {
				return;
			}
			elementIndex = elementIdGetter.applyAsInt(elementIndex);
			pocket.setElementCount(elementIndex, savedElements.getLong(key));
		});
		// load available patterns
		ListTag savedAvailablePatterns = saved.getList("availablePatterns", Tag.TAG_COMPOUND);
		for (int i = 0; i < savedAvailablePatterns.size(); i++) {
			CompoundTag savedAvailablePattern = savedAvailablePatterns.getCompound(i);
			ServerCraftingPattern craftingPattern = new ServerCraftingPattern(
					savedAvailablePattern.getUUID("patternId"),
					CraftingPattern.load(savedAvailablePattern)
			);
			ListTag savedPatternPositions = saved.getList("positions", Tag.TAG_COMPOUND);
			for (int j = 0; j < savedPatternPositions.size(); j++) {
				craftingPattern.positions.add(LevelBlockPos.load(savedPatternPositions.getCompound(j)));
			}
			pocket.availablePatternsById.put(craftingPattern.id, craftingPattern);
			pocket.availablePatternsByPattern.put(craftingPattern.pattern, craftingPattern);
		}
		// load default patterns
		CompoundTag savedDefaultPatterns = saved.getCompound("defaultPatterns");
		savedDefaultPatterns.getAllKeys().forEach(key -> {
			int elementIndex;
			try {
				elementIndex = Integer.parseInt(key);
			} catch (NumberFormatException e) {
				return;
			}
			elementIndex = elementIdGetter.applyAsInt(elementIndex);
			pocket.setDefaultPattern(elementIndex, savedElements.getUUID(key));
		});
		// load crafting processes
		ListTag savedCraftingProcesses = saved.getList("craftingProcesses", Tag.TAG_LIST);
		for (int i = 0; i < savedCraftingProcesses.size(); i++) {
			pocket.craftingProcesses.add(CraftingProcess.load(
					pocket.nextCraftingProcessId++,
					savedCraftingProcesses.getList(i)
			));
		}
		return pocket;
	}
}
