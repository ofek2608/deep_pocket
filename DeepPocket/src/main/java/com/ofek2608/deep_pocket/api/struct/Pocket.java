package com.ofek2608.deep_pocket.api.struct;

import com.ofek2608.collections.CaptureMap;
import com.ofek2608.collections.CaptureReference;
import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public final class Pocket {
	private final ItemConversions conversions;
	private final UUID pocketId;
	private final UUID owner;
	private final CaptureReference<PocketInfo> pocketInfo;
	private final PocketItems items;
	private final CaptureMap<UUID,CraftingPattern> patterns;

	public Pocket(ItemConversions conversions, UUID pocketId, UUID owner, PocketInfo pocketInfo) {
		this.conversions = conversions;
		this.pocketId = pocketId;
		this.owner = owner;
		this.pocketInfo = new CaptureReference<>(pocketInfo);
		this.items = new PocketItems();
		this.patterns = new CaptureMap<>();
	}

	public Pocket(Pocket copy) {
		this.conversions = copy.conversions;
		this.pocketId = copy.pocketId;
		this.owner = copy.owner;
		this.pocketInfo = new CaptureReference<>(copy.pocketInfo);
		this.items = new PocketItems(copy.items);
		this.patterns = new CaptureMap<>(copy.patterns);
	}

	public Pocket(MinecraftServer server, ItemConversions conversions, boolean allowPublicPocket, CompoundTag saved) {
		this(conversions, saved.getUUID("pocketId"), saved.getUUID("owner"), new PocketInfo(saved.getCompound("info")));
		for (Tag itemCount : saved.getList("itemCounts", 10)) {
			ItemType type = ItemType.load(((CompoundTag) itemCount).getCompound("item"));
			long count = ((CompoundTag)itemCount).getLong("count");
			if (count == 0)
				continue;
			this.items.put(type, count < 0 ? -1 : count);
		}
		conversions.convertMap(items);
		for (Tag savedPattern : saved.getList("patterns", 10)) {
			try {
				WorldCraftingPattern pattern = new WorldCraftingPattern((CompoundTag) savedPattern, server);
				this.patterns.put(pattern.getPatternId(), pattern);
			} catch (Exception ignored) {}
		}
		if (!allowPublicPocket && pocketInfo.get().securityMode == PocketSecurityMode.PUBLIC)
			pocketInfo.get().securityMode = PocketSecurityMode.TEAM;
	}

	public CompoundTag save() {
		CompoundTag saved = new CompoundTag();
		saved.putUUID("pocketId", pocketId);
		saved.putUUID("owner", owner);
		saved.put("info", pocketInfo.get().save());
		ListTag itemCounts = new ListTag();
		for (var entry : items.entrySet()) {
			CompoundTag itemCount = new CompoundTag();
			itemCount.put("item", entry.getKey().save());
			itemCount.putDouble("count", entry.getValue());
			itemCounts.add(itemCount);
		}
		saved.put("itemCounts", itemCounts);
		return saved;
	}

	public UUID getPocketId() { return pocketId; }
	public UUID getOwner() { return owner; }
	public PocketInfo getInfo() { return new PocketInfo(pocketInfo.get()); }
	public String getName() { return pocketInfo.get().name; }
	public ItemType getIcon() { return pocketInfo.get().icon; }
	public int getColor() { return pocketInfo.get().color; }
	public PocketSecurityMode getSecurityMode() { return pocketInfo.get().securityMode; }

	public void setInfo(PocketInfo pocketInfo) {
		this.pocketInfo.set(new PocketInfo(pocketInfo));
	}


	public boolean canAccess(Player player) {
		return pocketInfo.get().securityMode.canAccess(player, getOwner());
	}

	public Map<ItemType,Long> getItems() {
		return items;
	}

	public long getItemCount(ItemType type) {
		return items.get(type);
	}

	private void insertItem0(ItemType type, long count) {
		items.computeIfPresent(type, (t,current)->current < 0 || count < 0 ? -1 : current + count);
	}

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

	private boolean isInvalidKnowledge(@Nullable PlayerKnowledge knowledge, Map<ItemType,Long> counts) {
		if (knowledge == null)
			return false;
		for (ItemType item : counts.keySet())
			if (!knowledge.contains(item))
				return true;
		return false;
	}

	public long getMaxExtract(@Nullable PlayerKnowledge knowledge, Map<ItemType,Long> counts) {
		counts = new HashMap<>(counts);
		conversions.convertMap(counts);
		if (isInvalidKnowledge(knowledge, counts))
			return 0;
		return getMaxExtract0(counts);
	}

	public long extract(@Nullable PlayerKnowledge knowledge, Map<ItemType,Long> counts, long overallCount) {
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

	public long extractItem(@Nullable PlayerKnowledge knowledge, ItemType type, long count) {
		return extract(knowledge, Map.of(type, 1L), count);
	}

	public long getMaxExtract(@Nullable PlayerKnowledge knowledge, ItemType ... items) {
		Map<ItemType,Long> itemsMap = new HashMap<>();
		for (ItemType item : items)
			itemsMap.put(item, itemsMap.getOrDefault(item, 0L) + 1);
		return getMaxExtract(knowledge, itemsMap);
	}

	public void clearItems() {
		items.clear();
	}

	public @Nullable CraftingPattern getPattern(UUID patternId) {
		return patterns.get(patternId);
	}

	@ApiStatus.Internal
	public void addPattern(CraftingPattern pattern) {
		patterns.put(pattern.getPatternId(), pattern);
	}

	public UUID addPattern(ItemAmount[] input, ItemTypeAmount[] output, ServerLevel level, BlockPos pos) {
		UUID patternId;
		do {
			patternId = UUID.randomUUID();
		} while (patterns.containsKey(patternId));
		addPattern(new WorldCraftingPattern(patternId, input, output, level, pos));
		return patternId;
	}

	public void removePattern(UUID patternId) {
		patterns.remove(patternId);
	}

	public Collection<CraftingPattern> getPatterns() {
		return patterns.values();
	}


	public Pocket copy() {
		return new Pocket(this);
	}

	public Snapshot createSnapshot() {
		return new Snapshot();
	}


	public final class Snapshot {
		private final CaptureReference<PocketInfo>.Snapshot pocketInfoSnapshot = pocketInfo.createSnapshot();
		private final CaptureMap<ItemType,Long>.Snapshot itemsSnapshot = items.createSnapshot();
		private final CaptureMap<UUID,CraftingPattern>.Snapshot patternsSnapshot = patterns.createSnapshot();

		private Snapshot() {}

		public Pocket getPocket() {
			return Pocket.this;
		}

		public boolean didChangedInfo() {
			return pocketInfoSnapshot.isChanged();
		}

		public @UnmodifiableView Map<ItemType,Long> getChangedItems() {
			return itemsSnapshot.getChangedAsMap();
		}

		public CraftingPattern[] getAddedPatterns() {
			return patternsSnapshot.getAddedValues().toArray(CraftingPattern[]::new);
		}

		public UUID[] getRemovedPatterns() {
			return patternsSnapshot.getRemovedKeys().toArray(UUID[]::new);
		}
	}







	private final class PocketItems extends CaptureMap<ItemType,Long> {
		public PocketItems() { }
		public PocketItems(Map<? extends ItemType, ? extends Long> m) { super(m); }

		@Override
		public void validateKey(ItemType key) {
			Objects.requireNonNull(key);
			if (key.isEmpty() || conversions.hasValue(key))
				throw new IllegalArgumentException();
		}

		@Override
		public Long validateValue(Long val) {
			Objects.requireNonNull(val);
			return val < 0 ? -1 : val;
		}

		@Override
		public Long defaultValue(Object key) {
			return 0L;
		}
	}
}
