package com.ofek2608.deep_pocket.impl;

import com.ofek2608.collections.CaptureMap;
import com.ofek2608.deep_pocket.api.pocket.PocketPatterns;
import com.ofek2608.deep_pocket.api.struct.CraftingPattern;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.LevelBlockPos;
import org.jetbrains.annotations.UnmodifiableView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class PocketPatternsImpl implements PocketPatterns {
	@Nonnull private final CaptureMap<UUID,CraftingPattern> patterns;
	@Nonnull private final Map<CraftingPattern,UUID> patternsId;
	@Nonnull private final Map<UUID,List<LevelBlockPos>> patternsLocations;
	@Nonnull private final DefaultPatterns defaultPatterns;
	
	PocketPatternsImpl() {
		this.patterns = new CaptureMap<>();
		this.patternsId = new HashMap<>();
		this.patternsLocations = new HashMap<>();
		this.defaultPatterns = new DefaultPatterns();
	}
	
	private PocketPatternsImpl(PocketPatternsImpl copy) {
		this.patterns = new CaptureMap<>(copy.patterns);
		this.patternsId = new HashMap<>(copy.patternsId);
		this.patternsLocations = new HashMap<>();
		copy.patternsLocations.forEach((patternId,positions)->this.patternsLocations.put(patternId, new ArrayList<>(positions)));
		this.defaultPatterns = new DefaultPatterns(copy.defaultPatterns);
	}
	
	@Override
	public UUID add(CraftingPattern pattern, LevelBlockPos pos) {
		UUID patternId = patternsId.computeIfAbsent(pattern, p->{
			UUID id = UUID.randomUUID();
			patterns.put(id, p);
			patternsLocations.put(id, new ArrayList<>());
			return id;
		});
		List<LevelBlockPos> patternPositions = patternsLocations.get(patternId);
		if (!patternPositions.contains(pos))
			patternPositions.add(pos);
		return patternId;
	}
	
	@Override
	public boolean remove(UUID patternId, LevelBlockPos pos) {
		List<LevelBlockPos> patternPositions = patternsLocations.get(patternId);
		if (patternPositions == null || !patternPositions.remove(pos))
			return false;
		if (patternPositions.size() > 0)
			return true;
		patternsId.remove(patterns.remove(patternId));
		patternsLocations.remove(patternId);
		return true;
	}
	
	@Override
	public @Nullable CraftingPattern get(@Nullable UUID patternId) {
		return patternId == null ? null : patterns.get(patternId);
	}
	
	@Override
	public @Nullable UUID getId(@Nullable CraftingPattern pattern) {
		return pattern == null ? null : patternsId.get(pattern);
	}
	
	@Override
	public @UnmodifiableView List<LevelBlockPos> getPositions(@Nullable UUID patternId) {
		return patternsLocations.getOrDefault(patternId, Collections.emptyList());
	}
	
	@Override
	public void clear() {
		patterns.clear();
		patternsId.clear();
		patternsLocations.clear();
	}
	
	@Override
	public @UnmodifiableView Set<UUID> getAllPatterns() {
		return Collections.unmodifiableSet(patterns.keySet());
	}
	
	@Override
	public Map<ElementType, Optional<UUID>> getDefaultsMap() {
		return defaultPatterns;
	}
	
	@Override
	public Optional<UUID> getDefault(ElementType type) {
		return defaultPatterns.get(type);
	}
	
	@Override
	public void setDefault(ElementType type, Optional<UUID> patternId) {
		defaultPatterns.put(type, patternId);
	}
	
	@Override
	public Snapshot createSnapshot() {
		return new SnapshotImpl();
	}
	
	@Override
	public PocketPatterns copy() {
		return new PocketPatternsImpl(this);
	}
	
	@Override
	public void put(UUID patternId, CraftingPattern pattern) {
		patterns.put(patternId, pattern);
		patternsId.put(pattern, patternId);
		List<LevelBlockPos> locations = new ArrayList<>();
		locations.add(LevelBlockPos.ZERO);
		patternsLocations.put(patternId, locations);
	}
	
	private final class SnapshotImpl implements Snapshot {
		private final CaptureMap<UUID,CraftingPattern>.Snapshot internal = patterns.createSnapshot();
		private final CaptureMap<ElementType,Optional<UUID>>.Snapshot internalDefault = defaultPatterns.createSnapshot();
		@Override
		public @UnmodifiableView Map<UUID, CraftingPattern> getAdded() {
			return internal.getAddedAsMap();
		}
		
		@Override
		public @UnmodifiableView Set<UUID> getRemoved() {
			return internal.getRemovedKeys();
		}
		
		@Override
		public @UnmodifiableView Map<ElementType, Optional<UUID>> getAddedDefaults() {
			return internalDefault.getAddedAsMap();
		}
		
		@Override
		public ElementType[] getRemovedDefaults() {
			return internalDefault.getRemovedKeys().toArray(new ElementType[0]);
		}
	}
	
	private static final class DefaultPatterns extends CaptureMap<ElementType,Optional<UUID>> {
		public DefaultPatterns() { }
		public DefaultPatterns(Map<? extends ElementType, ? extends Optional<UUID>> m) { super(m); }
		
		@Override
		public Optional<UUID> validate(ElementType key, Optional<UUID> val) {
			Objects.requireNonNull(key);
			Objects.requireNonNull(val);
			if (key.isEmpty())
				throw new IllegalArgumentException();
			return val;
		}
	}
}
