package com.ofek2608.deep_pocket.impl;

import com.ofek2608.collections.CaptureMap;
import com.ofek2608.collections.CaptureReference;
import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.Knowledge;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessManager;
import com.ofek2608.deep_pocket.api.ProvidedResources;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.api.struct.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

final class PocketImpl implements Pocket {
	private final ItemConversions conversions;
	private final UUID pocketId;
	private final UUID owner;
	private final CaptureReference<PocketInfo> pocketInfo;
	private final PocketItems items;
	private final PocketPatterns patterns;
	private final PocketProcessManager processes;

	PocketImpl(ItemConversions conversions, UUID pocketId, UUID owner, PocketInfo pocketInfo, PocketProcessManager processes) {
		this.conversions = conversions;
		this.pocketId = pocketId;
		this.owner = owner;
		this.pocketInfo = new CaptureReference<>(pocketInfo);
		this.items = new PocketItems();
		this.patterns = new PocketPatterns();
		this.processes = processes;
	}

	private PocketImpl(PocketImpl copy) {
		this.conversions = copy.conversions;
		this.pocketId = copy.pocketId;
		this.owner = copy.owner;
		this.pocketInfo = new CaptureReference<>(copy.pocketInfo);
		this.items = new PocketItems(copy.items);
		this.patterns = new PocketPatterns(copy.patterns);
		this.processes = copy.processes.recreate();
	}

	@Override public UUID getPocketId() { return pocketId; }
	@Override public UUID getOwner() { return owner; }
	@Override public PocketInfo getInfo() { return new PocketInfo(pocketInfo.get()); }
	@Override public void setInfo(PocketInfo pocketInfo) { this.pocketInfo.set(new PocketInfo(pocketInfo)); }
	@Override public String getName() { return pocketInfo.get().name; }
	@Override public ItemType getIcon() { return pocketInfo.get().icon; }
	@Override public int getColor() { return pocketInfo.get().color; }
	@Override public PocketSecurityMode getSecurityMode() { return pocketInfo.get().securityMode; }




	@Override
	public boolean canAccess(Player player) {
		return pocketInfo.get().securityMode.canAccess(player, getOwner());
	}

	@Override
	public Map<ItemType,Long> getItemsMap() {
		return items;
	}

	@Override
	public long getItemCount(ItemType type) {
		return items.get(type);
	}

	private void insertItem0(ItemType type, long count) {
		long leftOver = processes.supplyItem(type, count);
		if (leftOver != 0)
			items.computeIfPresent(type, (t,current)->current < 0 || leftOver < 0 ? -1 : current + leftOver);
	}

	@Override
	public void insertItem(ItemType type, long count) {
		if (type.isEmpty() || count == 0)
			return;
		long[] value = conversions.getValue(type, count);
		if (value == null) {
			insertItem0(type, count);
			return;
		}
		for (int i = 0; i < value.length; i++)
			if (value[i] != 0)
				insertItem0(conversions.getBaseItem(i), value[i]);
	}

	@Override
	public void insertAll(ProvidedResources resources) {
		int typeCount = resources.getTypeCount();
		for (int i = 0; i < typeCount; i++)
			insertItem(resources.getType(i), resources.take(i, -1));
	}

	private long getMaxExtract0(Map<ItemType,Long> counts) {
		long min = -1;
		for (var entry : counts.entrySet()) {
			long v = entry.getValue();
			if (v == 0)
				continue;
			long existing = getItemCount(entry.getKey());
			if (existing < 0)
				continue;
			if (v < 0)
				return 0;//can't extract if you need infinity for each one
			if (existing == 0)
				return 0;
			long canMake = existing / v;
			if (canMake == 0)
				return 0;
			if (min < 0 || canMake < min)
				min = canMake;
		}
		return min;
	}

	private boolean isInvalidKnowledge(@Nullable Knowledge knowledge, Map<ItemType,Long> counts) {
		if (knowledge == null)
			return false;
		for (ItemType item : counts.keySet())
			if (!knowledge.contains(item))
				return true;
		return false;
	}

	@Override
	public long getMaxExtract(@Nullable Knowledge knowledge, Map<ItemType,Long> counts) {
		counts = new HashMap<>(counts);
		conversions.convertMap(counts);
		if (isInvalidKnowledge(knowledge, counts))
			return 0;
		return getMaxExtract0(counts);
	}

	@Override
	public long extract(@Nullable Knowledge knowledge, Map<ItemType,Long> counts, long overallCount) {
		if (overallCount == 0)
			return 0;
		counts = new HashMap<>(counts);
		conversions.convertMap(counts);
		if (isInvalidKnowledge(knowledge, counts))
			return 0;
		long maxExtract = getMaxExtract0(counts);
		if (maxExtract == 0)
			return 0;
		if (0 < maxExtract && maxExtract < overallCount)
			overallCount = maxExtract;
		for (var entry : counts.entrySet()) {
			long existing = getItemCount(entry.getKey());
			if (existing < 0)
				continue;
			long needed = DeepPocketUtils.advancedMul(entry.getValue(), overallCount);
			items.put(entry.getKey(), existing - needed);
		}
		return overallCount;
	}

	@Override
	public long extractItem(@Nullable Knowledge knowledge, ItemType type, long count) {
		return extract(knowledge, Map.of(type, 1L), count);
	}

	@Override
	public long getMaxExtract(@Nullable Knowledge knowledge, ItemType ... items) {
		Map<ItemType,Long> itemsMap = new HashMap<>();
		for (ItemType item : items)
			itemsMap.put(item, itemsMap.getOrDefault(item, 0L) + 1);
		return getMaxExtract(knowledge, itemsMap);
	}

	@Override
	public Map<UUID, CraftingPattern> getPatternsMap() {
		return patterns;
	}

	@Override
	public @Nullable CraftingPattern getPattern(UUID patternId) {
		return patterns.get(patternId);
	}

	@Override
	public UUID addPattern(ItemTypeAmount[] input, ItemTypeAmount[] output, ServerLevel level, BlockPos pos) {
		UUID patternId;
		do {
			patternId = UUID.randomUUID();
		} while (patterns.containsKey(patternId));
		patterns.put(patternId, new WorldCraftingPattern(patternId, input, output, level, pos));
		return patternId;
	}

	@Override
	public void removePattern(UUID patternId) {
		patterns.remove(patternId);
	}

	@Override
	public PocketProcessManager getProcesses() {
		return processes;
	}

	@Override
	public Snapshot createSnapshot() {
		return new SnapshotImpl();
	}

	@Override
	public PocketImpl copy() {
		return new PocketImpl(this);
	}


	private final class SnapshotImpl implements Snapshot {
		private final CaptureReference<PocketInfo>.Snapshot pocketInfoSnapshot = pocketInfo.createSnapshot();
		private final CaptureMap<ItemType,Long>.Snapshot itemsSnapshot = items.createSnapshot();
		private final CaptureMap<UUID,CraftingPattern>.Snapshot patternsSnapshot = patterns.createSnapshot();

		@Override
		public PocketImpl getPocket() {
			return PocketImpl.this;
		}

		@Override
		public boolean didChangedInfo() {
			return pocketInfoSnapshot.isChanged();
		}

		@Override
		public @UnmodifiableView Map<ItemType,Long> getChangedItems() {
			return itemsSnapshot.getChangedAsMap();
		}

		@Override
		public CraftingPattern[] getAddedPatterns() {
			return patternsSnapshot.getAddedValues().toArray(CraftingPattern[]::new);
		}

		@Override
		public UUID[] getRemovedPatterns() {
			return patternsSnapshot.getRemovedKeys().toArray(UUID[]::new);
		}
	}







	private final class PocketItems extends CaptureMap<ItemType,Long> {
		public PocketItems() { }
		public PocketItems(Map<? extends ItemType, ? extends Long> m) { super(m); }

		@Override
		public Long validate(ItemType key, Long val) {
			Objects.requireNonNull(key);
			Objects.requireNonNull(val);
			if (key.isEmpty() || conversions.hasValue(key))
				throw new IllegalArgumentException();
			return val < 0 ? -1 : val;
		}

		@Override
		public Long defaultValue(Object key) {
			return 0L;
		}
	}

	private static final class PocketPatterns extends CaptureMap<UUID,CraftingPattern> {
		public PocketPatterns() { }
		public PocketPatterns(Map<? extends UUID, ? extends CraftingPattern> m) { super(m); }

		@Override
		public CraftingPattern validate(UUID key, CraftingPattern val) throws IllegalArgumentException {
			Objects.requireNonNull(key);
			Objects.requireNonNull(val);
			if (!val.getPatternId().equals(key))
				throw new IllegalArgumentException();
			return val;
		}
	}
}
