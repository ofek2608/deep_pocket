package com.ofek2608.deep_pocket.impl;

import com.ofek2608.collections.CaptureMap;
import com.ofek2608.collections.CaptureReference;
import com.ofek2608.deep_pocket.api.*;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessManager;
import com.ofek2608.deep_pocket.api.struct.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

import static com.ofek2608.deep_pocket.utils.AdvancedLongMath.*;

final class PocketImpl implements Pocket {
	@Nonnull private final ItemConversions conversions;
	@Nonnull private ElementConversions conversions0;
	@Nonnull private final UUID pocketId;
	@Nonnull private final UUID owner;
	@Nonnull private final CaptureReference<PocketInfo> pocketInfo;
	@Nonnull private final PocketItems items;
	@Nonnull private final PocketContentOld contentOld;
	@Nonnull private final PocketContent content;
	@Nonnull private final PocketPatterns patterns;
	@Nonnull private final PocketDefaultPatterns defaultPatterns;
	@Nonnull private final PocketProcessManager processes;

	PocketImpl(DeepPocketHelper helper, ItemConversions conversions, ElementConversions conversions0, UUID pocketId, UUID owner, PocketInfo pocketInfo, PocketProcessManager processes) {
		this.conversions = conversions;
		this.conversions0 = conversions0;
		this.pocketId = pocketId;
		this.owner = owner;
		this.pocketInfo = new CaptureReference<>(pocketInfo);
		this.items = new PocketItems();
		this.contentOld = new PocketContentOld();
		this.content = helper.createPocketContent();
		this.patterns = new PocketPatterns();
		this.defaultPatterns = new PocketDefaultPatterns();
		this.processes = processes;
	}

	private PocketImpl(PocketImpl copy) {
		this.conversions = copy.conversions;
		this.conversions0 = copy.conversions0;
		this.pocketId = copy.pocketId;
		this.owner = copy.owner;
		this.pocketInfo = new CaptureReference<>(copy.pocketInfo);
		this.items = new PocketItems(copy.items);
		this.contentOld = new PocketContentOld(copy.contentOld);
		this.content = copy.content.copy();
		this.patterns = new PocketPatterns(copy.patterns);
		this.defaultPatterns = new PocketDefaultPatterns(copy.defaultPatterns);
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
	public void setConversions(ElementConversions conversions) {
		this.conversions0 = conversions;
		conversions.convertMap(contentOld);
	}
	
	@Override
	public boolean canAccess(Player player) {
		return pocketInfo.get().securityMode.canAccess(player, getOwner());
	}

	@Override
	public Map<ElementType, Long> getContentOld() {
		return contentOld;
	}
	
	@Override
	public PocketContent getContent() {
		return content;
	}
	
	@Override
	public void insertElement(ElementType type, long count) {
		if (type.isEmpty() || count == 0)
			return;
		long[] valueCount = conversions0.getValue(type);
		
		if (valueCount == null) {
			contentOld.put(type, advancedSum(contentOld.get(type), count));
			return;
		}
		
		for (int i = 0; i < valueCount.length; i++) {
			ElementType.TConvertible convertible = conversions0.getBaseElement(i);
			contentOld.put(
					convertible,
					advancedSum(contentOld.get(convertible), advancedMul(count, valueCount[i]))
			);
		}
	}
	
	
	public long getMaxExtract0(@Nullable Knowledge0 knowledge, Map<ElementType, Long> counts) {
		conversions0.convertMap(counts);
		long maxExtract = -1;
		for (var entry : counts.entrySet()) {
			if (knowledge != null && !knowledge.contains(entry.getKey()))
				return 0L;
			long current = advancedDiv(contentOld.get(entry.getKey()), entry.getValue());
			maxExtract = advancedMin(maxExtract, current);
		}
		return maxExtract;
	}
	
	@Override
	public long getMaxExtract(@Nullable Knowledge0 knowledge, Map<ElementType, Long> counts) {
		return getMaxExtract0(knowledge, new HashMap<>(counts));
	}
	
	@Override
	public long getMaxExtract(@Nullable Knowledge0 knowledge, ElementType ... types) {
		Map<ElementType,Long> typesMap = new HashMap<>();
		for (ElementType type : types)
			typesMap.put(type, typesMap.getOrDefault(type, 0L) + 1);
		return getMaxExtract0(knowledge, typesMap);
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
	public long getMaxExtractOld(@Nullable Knowledge knowledge, Map<ItemType,Long> counts) {
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
			long needed = advancedMul(entry.getValue(), overallCount);
			items.put(entry.getKey(), existing - needed);
		}
		return overallCount;
	}

	@Override
	public long extractItem(@Nullable Knowledge knowledge, ItemType type, long count) {
		return extract(knowledge, Map.of(type, 1L), count);
	}
	
	@Override
	public long extractItem(@Nullable Knowledge0 knowledge, ElementType type, long count) {
		if (knowledge != null && !knowledge.contains(type))
			return 0;
		return new EntryImpl(type).extract(count);
	}
	
	@Override
	public long getMaxExtractOld(@Nullable Knowledge knowledge, ItemType ... items) {
		Map<ItemType,Long> itemsMap = new HashMap<>();
		for (ItemType item : items)
			itemsMap.put(item, itemsMap.getOrDefault(item, 0L) + 1);
		return getMaxExtractOld(knowledge, itemsMap);
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
	public Map<ItemType, Optional<UUID>> getDefaultPatternsMap() {
		return defaultPatterns;
	}

	@Override
	public Optional<UUID> getDefaultPattern(ItemType type) {
		return defaultPatterns.get(type);
	}

	@Override
	public void setDefaultPattern(ItemType type, Optional<UUID> patternId) {
		defaultPatterns.put(type, patternId);
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
				contentOld.keySet().stream().filter(type->!conversions0.hasValue(type)),
				conversions0.getKeys().stream()
		).map(EntryImpl::new);
		//TODO add craft-able
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
			long[] valueCount = conversions0.getValue(type);
			
			if (valueCount == null)
				return contentOld.get(type);
			
			long maxExtract = -1;
			for (int i = 0; i < valueCount.length; i++) {
				long current = advancedDiv(contentOld.get(conversions0.getBaseElement(i)), valueCount[i]);
				maxExtract = advancedMin(maxExtract, current);
			}
			
			return maxExtract;
		}
		
		@Override
		public void insert(long amount) {
			insertElement(type, amount);
		}
		
		@Override
		public long extract(long amount) {
			amount = advancedMin(amount, getMaxExtract());
			if (amount == 0)
				return 0;
			
			long[] valueCount = conversions0.getValue(type);
			
			if (valueCount == null) {
				contentOld.put(type, advancedSub(contentOld.get(type), amount));
				return amount;
			}
			for (int i = 0; i < valueCount.length; i++) {
				ElementType.TConvertible convertible = conversions0.getBaseElement(i);
				contentOld.put(
						convertible,
						advancedSub(contentOld.get(convertible), advancedMul(amount, valueCount[i]))
				);
			}
			return amount;
		}
		
		@Override
		public boolean canBeCrafted() {
			return false;//TODO
		}
		
		@Override
		public boolean canBeConverted() {
			return conversions0.hasValue(type);
		}
	}

	private final class SnapshotImpl implements Snapshot {
		private final CaptureReference<PocketInfo>.Snapshot pocketInfoSnapshot = pocketInfo.createSnapshot();
		private final CaptureMap<ItemType,Long>.Snapshot itemsSnapshot = items.createSnapshot();
		private final CaptureMap<ElementType,Long>.Snapshot contentSnapshot = contentOld.createSnapshot();
		private final CaptureMap<UUID,CraftingPattern>.Snapshot patternsSnapshot = patterns.createSnapshot();
		private final CaptureMap<ItemType,Optional<UUID>>.Snapshot defaultPatternsSnapshot = defaultPatterns.createSnapshot();

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
		public @UnmodifiableView Map<ElementType, Long> getChangedElements() {
			return contentSnapshot.getChangedAsMap();
		}
		
		@Override
		public CraftingPattern[] getAddedPatterns() {
			return patternsSnapshot.getAddedValues().toArray(CraftingPattern[]::new);
		}

		@Override
		public @UnmodifiableView Map<ItemType, Optional<UUID>> getAddedDefaultPatterns() {
			return defaultPatternsSnapshot.getAddedAsMap();
		}

		@Override
		public ItemType[] getRemovedDefaultPatterns() {
			return defaultPatternsSnapshot.getRemovedKeys().toArray(new ItemType[0]);
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
	
	private final class PocketContentOld extends CaptureMap<ElementType,Long> {
		public PocketContentOld() { }
		public PocketContentOld(Map<? extends ElementType, ? extends Long> m) { super(m); }

		@Override
		public Long validate(ElementType key, Long val) {
			Objects.requireNonNull(key);
			Objects.requireNonNull(val);
			if (key.isEmpty() || conversions0.hasValue(key))
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

	private static final class PocketDefaultPatterns extends CaptureMap<ItemType,Optional<UUID>> {
		public PocketDefaultPatterns() { }
		public PocketDefaultPatterns(Map<? extends ItemType, ? extends Optional<UUID>> m) { super(m); }

		@Override
		public Optional<UUID> validate(ItemType key, Optional<UUID> val) {
			Objects.requireNonNull(key);
			Objects.requireNonNull(val);
			if (key.isEmpty())
				throw new IllegalArgumentException();
			return val;
		}
	}
}
