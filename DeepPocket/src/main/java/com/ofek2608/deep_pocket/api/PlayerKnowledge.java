package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.struct.ItemType;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.stream.Stream;

public interface PlayerKnowledge {
	CompoundTag save();
	public boolean contains(ItemType type);
	public Stream<ItemType> getPocketItems(Pocket pocket);
	public void add(ItemType ... types);
	public void remove(ItemType ... types);
	public void clear();
	public @UnmodifiableView Set<ItemType> asSet();
	public Snapshot createSnapshot();
	public PlayerKnowledge copy();





	public interface Snapshot {
		public PlayerKnowledge getKnowledge();
		public ItemType[] getRemoved();
		public ItemType[] getAdded();
	}
}
