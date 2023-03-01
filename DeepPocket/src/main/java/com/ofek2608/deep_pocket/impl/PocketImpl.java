package com.ofek2608.deep_pocket.impl;

import com.ofek2608.collections.CaptureMap;
import com.ofek2608.collections.CaptureReference;
import com.ofek2608.deep_pocket.api.*;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.pocket.PocketContent;
import com.ofek2608.deep_pocket.api.pocket.PocketPatterns;
import com.ofek2608.deep_pocket.api.pocket_process.old.PocketProcessManager;
import com.ofek2608.deep_pocket.api.struct.*;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.ofek2608.deep_pocket.utils.AdvancedLongMath.*;

final class PocketImpl implements Pocket {
	@Nonnull private final UUID pocketId;
	@Nonnull private final UUID owner;
	@Nonnull private final CaptureReference<PocketInfo> pocketInfo;
	@Nonnull @Deprecated(forRemoval = true) private final PocketItems items;
	@Nonnull private final PocketContent content;
	@Nonnull @Deprecated(forRemoval = true) private final PocketPatternsOld patternsOld;
	@Nonnull private final PocketPatterns patterns;
	@Nonnull private final PocketProcessManager processes;

	PocketImpl(DeepPocketHelper helper, ElementConversionsOld conversions, UUID pocketId, UUID owner, PocketInfo pocketInfo, PocketProcessManager processes) {
		this.pocketId = pocketId;
		this.owner = owner;
		this.pocketInfo = new CaptureReference<>(pocketInfo);
		this.items = new PocketItems();
		this.content = new PocketContentImpl(conversions);
		this.patternsOld = new PocketPatternsOld();
		this.patterns = new PocketPatternsImpl();
		this.processes = processes;
	}

	private PocketImpl(PocketImpl copy) {
		this.pocketId = copy.pocketId;
		this.owner = copy.owner;
		this.pocketInfo = new CaptureReference<>(copy.pocketInfo);
		this.items = new PocketItems(copy.items);
		this.content = copy.content.copy();
		this.patternsOld = new PocketPatternsOld(copy.patternsOld);
		this.patterns = copy.patterns.copy();
		this.processes = copy.processes.recreate();
	}

	@Override public UUID getPocketId() { return pocketId; }
	@Override public UUID getOwner() { return owner; }
	@Override public PocketInfo getInfo() { return new PocketInfo(pocketInfo.get()); }
	@Override public void setInfo(PocketInfo pocketInfo) { this.pocketInfo.set(new PocketInfo(pocketInfo)); }
	@Override public String getName() { return pocketInfo.get().name; }
	@Override public ElementType getIcon() { return pocketInfo.get().icon; }
	@Override public int getColor() { return pocketInfo.get().color; }
	@Override public PocketSecurityMode getSecurityMode() { return pocketInfo.get().securityMode; }
	
	@Override
	public void setConversions(ElementConversionsOld conversions) {
		content.setConversions(conversions);
	}
	
	@Override
	public boolean canAccess(Player player) {
		return pocketInfo.get().securityMode.canAccess(player, getOwner());
	}
	
	@Override
	public PocketContent getContent() {
		return content;
	}
	
	@Override
	public void insertElement(ElementType type, long count) {
		content.insert(type, count);
	}
	
	@Override
	public long getMaxExtract(@Nullable Knowledge knowledge, Map<ElementType, Long> counts) {
		return content.getMaxExtract(knowledge, counts);
	}
	
	@Override
	public long getMaxExtract(@Nullable Knowledge knowledge, ElementType ... types) {
		return content.getMaxExtract(knowledge, types);
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
//		long leftOver = processes.supplyItem(type, count);
//		if (leftOver != 0)
//			items.computeIfPresent(type, (t,current)->current < 0 || leftOver < 0 ? -1 : current + leftOver);
		if (count == 0)
			return;
		items.computeIfPresent(type, (t,current)->current < 0 || count < 0 ? -1 : current + count);
	}

	@Override
	public void insertItem(ItemType type, long count) {
		if (type.isEmpty() || count == 0)
			return;
		insertItem0(type, count);
	}

	@Override
	public void insertAll(ProvidedResources resources) {
		int typeCount = resources.getTypeCount();
		for (int i = 0; i < typeCount; i++)
			insertElement(resources.getType(i), resources.take(i, -1));
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

	@Override
	public long getMaxExtractOld(Map<ItemType,Long> counts) {
		return getMaxExtract0(new HashMap<>(counts));
	}

	@Override
	public long extract(Map<ItemType,Long> counts, long overallCount) {
		if (overallCount == 0)
			return 0;
		counts = new HashMap<>(counts);
		long maxExtract = getMaxExtract0(counts);
		if (maxExtract == 0)
			return 0;
		if (0 < maxExtract && maxExtract < overallCount)
			overallCount = maxExtract;
		for (var entry : counts.entrySet()) {
			long existing = getItemCount(entry.getKey());
			if (existing < 0)
				continue;
			long needed = advancedMul(entry.getValue(), overallCount);
			items.put(entry.getKey(), existing - needed);
		}
		return overallCount;
	}

	@Override
	public long extractItem(ItemType type, long count) {
		return extract(Map.of(type, 1L), count);
	}
	
	@Override
	public long extractItem(@Nullable Knowledge knowledge, ElementType type, long count) {
		if (knowledge != null && !knowledge.contains(type))
			return 0;
		return content.extract(type, count);
	}
	
	@Override
	public long getMaxExtractOld(ItemType ... items) {
		Map<ItemType,Long> itemsMap = new HashMap<>();
		for (ItemType item : items)
			itemsMap.put(item, itemsMap.getOrDefault(item, 0L) + 1);
		return getMaxExtractOld(itemsMap);
	}
	
	@Override
	public PocketPatterns getPatterns() {
		return patterns;
	}
	
	@Override
	public @Nullable CraftingPatternOld getPattern(UUID patternId) {
		return patternsOld.get(patternId);
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
	
	@Override
	public Stream<Entry> entries() {
		return Stream.concat(
				Stream.concat(
						IntStream.range(0, content.getSize())
								.mapToObj(content::getType)
								.filter(type->!(type instanceof ElementType.TConvertible)),
						content.getConversions().getKeys().stream()
				),
				patterns.getAllPatterns()
						.stream()
						.map(patterns::get)
						.filter(Objects::nonNull)
						.map(CraftingPattern::getOutput)
						.flatMap(Stream::of)
						.map(ElementTypeStack::getType)
						.filter(type->!(type instanceof ElementType.TConvertible))
						.filter(type -> content.getCount(type) == 0)
						.distinct()
		).map(EntryImpl::new);
	}
	
	
	private final class EntryImpl implements Entry {
		private final ElementType type;
		
		private EntryImpl(ElementType type) {
			this.type = type;
		}
		
		@Override
		public Pocket getPocket() {
			return PocketImpl.this;
		}
		
		@Override
		public ElementType getType() {
			return type;
		}
		
		@Override
		public long getMaxExtract() {
			return content.getMaxExtract(null, type);
		}
		
		@Override
		public void insert(long amount) {
			insertElement(type, amount);
		}
		
		@Override
		public long extract(long amount) {
			return content.extract(type, amount);
		}
		
		@Override
		public boolean canBeCrafted() {
			return patterns.getAllPatterns()
					.stream()
					.map(patterns::get)
					.filter(Objects::nonNull)
					.anyMatch(pattern -> pattern.hasOutput(type));
		}
		
		@Override
		public boolean canBeConverted() {
			return content.getConversions().hasValue(type);
		}
	}

	private final class SnapshotImpl implements Snapshot {
		private final CaptureReference<PocketInfo>.Snapshot pocketInfoSnapshot = pocketInfo.createSnapshot();
		private final PocketContent.Snapshot contentSnapshot = content.createSnapshot();
		private final PocketPatterns.Snapshot patternsSnapshot = patterns.createSnapshot();

		@Override
		public PocketImpl getPocket() {
			return PocketImpl.this;
		}

		@Override
		public boolean didChangedInfo() {
			return pocketInfoSnapshot.isChanged();
		}
		
		@Override
		public PocketContent.Snapshot getContentSnapshot() {
			return contentSnapshot;
		}
		
		@Override
		public PocketPatterns.Snapshot getPatternsSnapshot() {
			return patternsSnapshot;
		}
	}







	private static final class PocketItems extends CaptureMap<ItemType,Long> {
		public PocketItems() { }
		public PocketItems(Map<? extends ItemType, ? extends Long> m) { super(m); }

		@Override
		public Long validate(ItemType key, Long val) {
			Objects.requireNonNull(key);
			Objects.requireNonNull(val);
			if (key.isEmpty())
				throw new IllegalArgumentException();
			return val < 0 ? -1 : val;
		}

		@Override
		public Long defaultValue(Object key) {
			return 0L;
		}
	}

	private static final class PocketPatternsOld extends CaptureMap<UUID, CraftingPatternOld> {
		public PocketPatternsOld() { }
		public PocketPatternsOld(Map<? extends UUID, ? extends CraftingPatternOld> m) { super(m); }

		@Override
		public CraftingPatternOld validate(UUID key, CraftingPatternOld val) throws IllegalArgumentException {
			Objects.requireNonNull(key);
			Objects.requireNonNull(val);
			if (!val.getPatternId().equals(key))
				throw new IllegalArgumentException();
			return val;
		}
	}
}
