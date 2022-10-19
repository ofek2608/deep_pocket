package com.ofek2608.deep_pocket.api.struct;

import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public final class Pocket {
	private final ItemConversions conversions;
	private final UUID pocketId;
	private final UUID owner;
	private PocketInfo pocketInfo;
	private final Map<ItemType,Long> items;
	private final Map<UUID,CraftingPattern> patterns;
	private Snapshot lastSnapshot;

	public Pocket(ItemConversions conversions, UUID pocketId, UUID owner, PocketInfo pocketInfo) {
		this.conversions = conversions;
		this.pocketId = pocketId;
		this.owner = owner;
		this.pocketInfo = pocketInfo;
		this.items = new HashMap<>();
		this.patterns = new HashMap<>();
		this.lastSnapshot = new Snapshot();
	}

	public Pocket(Pocket copy) {
		this.conversions = copy.conversions;
		this.pocketId = copy.pocketId;
		this.owner = copy.owner;
		this.pocketInfo = copy.pocketInfo;
		this.items = new HashMap<>(copy.items);
		this.patterns = new HashMap<>(copy.patterns);
		this.lastSnapshot = new Snapshot();
	}

	public Pocket(MinecraftServer server, ItemConversions conversions, boolean allowPublicPocket, CompoundTag saved) {
		this.conversions = conversions;
		this.pocketId = saved.getUUID("pocketId");
		this.owner = saved.getUUID("owner");
		this.pocketInfo = new PocketInfo(saved.getCompound("info"));
		this.items = new HashMap<>();
		for (Tag itemCount : saved.getList("itemCounts", 10)) {
			ItemType type = ItemType.load(((CompoundTag) itemCount).getCompound("item"));
			long count = ((CompoundTag)itemCount).getLong("count");
			if (count == 0)
				continue;
			this.items.put(type, count < 0 ? -1 : count);
		}
		conversions.convertMap(items);
		this.patterns = new HashMap<>();
		for (Tag savedPattern : saved.getList("patterns", 10)) {
			try {
				WorldCraftingPattern pattern = new WorldCraftingPattern((CompoundTag) savedPattern, server);
				this.patterns.put(pattern.getPatternId(), pattern);
			} catch (Exception ignored) {}
		}
		this.lastSnapshot = new Snapshot();
		if (!allowPublicPocket && pocketInfo.securityMode == PocketSecurityMode.PUBLIC)
			pocketInfo.securityMode = PocketSecurityMode.TEAM;
	}

	public CompoundTag save() {
		CompoundTag saved = new CompoundTag();
		saved.putUUID("pocketId", pocketId);
		saved.putUUID("owner", owner);
		saved.put("info", pocketInfo.save());
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
	public PocketInfo getInfo() { return new PocketInfo(pocketInfo); }
	public String getName() { return pocketInfo.name; }
	public ItemType getIcon() { return pocketInfo.icon; }
	public int getColor() { return pocketInfo.color; }
	public PocketSecurityMode getSecurityMode() { return pocketInfo.securityMode; }

	public void setInfo(PocketInfo pocketInfo) {
		this.pocketInfo = new PocketInfo(pocketInfo);
		this.lastSnapshot.changedInfo = true;
	}


	public boolean canAccess(Player player) {
		return pocketInfo.securityMode.canAccess(player, getOwner());
	}

	public @UnmodifiableView Map<ItemType,Long> getItems() {
		return Collections.unmodifiableMap(items);
	}

	public long getItemCount(ItemType type) {
		return type.isEmpty() ? 0 : items.getOrDefault(type, 0L);
	}

	public void setItemCount(ItemType type, long count) {
		if (type.isEmpty())
			return;
		boolean changed;
		if (count == 0)
			changed = items.remove(type) != null;
		else
			changed = !Objects.equals(items.put(type, count < 0 ? -1 : count), count);
		if (changed)
			this.lastSnapshot.changedItems.add(type);
	}

	private void insertItem0(ItemType type, long count) {
		if (count < 0) {
			setItemCount(type, -1);
			return;
		}
		long currentCount = getItemCount(type);
		setItemCount(type, currentCount < 0 ? -1 : count + currentCount);
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
		counts = new HashMap<>(counts);
		conversions.convertMap(counts);
		if (isInvalidKnowledge(knowledge, counts))
			return 0;
		long maxExtract = getMaxExtract0(counts);
		if (0 <= maxExtract && maxExtract < overallCount)
			overallCount = maxExtract;
		for (var entry : counts.entrySet()) {
			long existing = getItemCount(entry.getKey());
			if (existing < 0)
				continue;
			long needed = DeepPocketUtils.advancedMul(entry.getValue(), overallCount);
			setItemCount(entry.getKey(), existing - needed);
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
		this.lastSnapshot.clearedItems = true;
		this.lastSnapshot.changedItems.clear();
	}

	public @Nullable CraftingPattern getPattern(UUID patternId) {
		return patterns.get(patternId);
	}

	@ApiStatus.Internal
	public void addPattern(CraftingPattern pattern) {
		patterns.put(pattern.getPatternId(), pattern);
		this.lastSnapshot.changedPatterns.add(pattern.getPatternId());
	}

	public UUID addPattern(ItemAmount[] input, ItemTypeAmount[] output, ServerLevel level, BlockPos pos) {
		UUID patternId;
		do {
			patternId = UUID.randomUUID();
		} while (this.patterns.containsKey(patternId));
		addPattern(new WorldCraftingPattern(patternId, input, output, level, pos));
		return patternId;
	}

	public void removePattern(UUID patternId) {
		if (this.patterns.remove(patternId) != null)
			this.lastSnapshot.changedPatterns.add(patternId);
	}

	public @UnmodifiableView Collection<CraftingPattern> getPatterns() {
		return Collections.unmodifiableCollection(this.patterns.values());
	}


	public Pocket copy() {
		return new Pocket(this);
	}

	public Snapshot createSnapshot() {
		return this.lastSnapshot = this.lastSnapshot.next = new Snapshot();
	}


	public final class Snapshot {
		private boolean changedInfo;
		private boolean clearedItems;
		private final Set<ItemType> changedItems = new HashSet<>();
		private final Set<UUID> changedPatterns = new HashSet<>();
		private Snapshot next;

		private Snapshot() {}

		public Pocket getPocket() {
			return Pocket.this;
		}

		public boolean didChangedInfo() {
			simplify();
			return changedInfo || next != null && next.changedInfo;
		}

		public boolean didClearedItems() {
			simplify();
			return clearedItems || next != null && next.clearedItems;
		}

		public Map<ItemType,Long> getChangedItems() {
			simplify();
			Map<ItemType,Long> result = new HashMap<>();
			if (next == null || !next.clearedItems)
				for (ItemType type : changedItems)
					result.put(type, Pocket.this.getItemCount(type));
			if (next != null)
				for (ItemType type : next.changedItems)
					result.put(type, Pocket.this.getItemCount(type));
			return result;
		}

		public CraftingPattern[] getAddedPatterns() {
			return changedPatterns.stream()
							.map(Pocket.this::getPattern)
							.filter(Objects::nonNull)
							.toArray(CraftingPattern[]::new);
		}

		public UUID[] getRemovedPatterns() {
			return changedPatterns.stream()
							.filter(patternId->Pocket.this.getPattern(patternId) == null)
							.toArray(UUID[]::new);
		}




		private void simplify() {
			if (next == null)
				return;
			while (next.next != null) {
				changedInfo = changedInfo || next.changedInfo;
				if (next.clearedItems) {
					clearedItems = true;
					changedItems.clear();
				}
				changedItems.addAll(next.changedItems);
				next = next.next;
			}
		}
	}
}
