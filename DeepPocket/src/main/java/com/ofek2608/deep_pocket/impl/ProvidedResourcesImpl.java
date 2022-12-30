package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.ProvidedResources;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.ElementTypeStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import static com.ofek2608.deep_pocket.utils.AdvancedLongMath.advancedSum;

final class ProvidedResourcesImpl implements ProvidedResources {
	private final @Nullable ProvidedResourcesImpl parent;
	private final ElementType[] types;
	private final long[] provided;
	private final int[] indexes;

	ProvidedResourcesImpl(ElementType[] types) {
		this(null, types.clone(), new int[types.length]);
		for (ElementType type : types)
			if (type.isEmpty())
				throw new IllegalArgumentException("types");
	}

	private ProvidedResourcesImpl(@Nullable ProvidedResourcesImpl parent, ElementType[] types, int[] indexes) {
		this.parent = parent;
		this.types = types;
		this.provided = new long[types.length];
		this.indexes = indexes;
	}

	@Override
	public ElementType[] getTypes() {
		return types.clone();
	}

	@Override
	public int getTypeCount() {
		return types.length;
	}

	@Override
	public ElementType getType(int index) {
		return index < 0 || types.length <= index ? ElementType.empty() : types[index];
	}

	@Override
	public long getProvided(int index) {
		return index < 0 || provided.length <= index ? 0 : provided[index];
	}

	@Override
	public void provide(int index, long count) {
		if (index < 0 || types.length <= index)
			return;
		provided[index] = advancedSum(provided[index], count);
	}

	@Override
	public long take(int index, long count) {
		if (index < 0 || types.length <= index)
			return 0;
		long current = provided[index];
		if (current < 0)
			return count < 0 ? -1 : count;
		if (0 <= count && count <= current) {
			provided[index] = current - count;
			return count;
		}
		provided[index] = 0;
		return current;
	}

	@Override
	public long getMaxRequestFromParents(int index) {
		if (index < 0 || types.length <= index)
			return 0;
		long count = 0;
		ProvidedResourcesImpl parent = this.parent;
		while (parent != null) {
			index = this.indexes[index];
			count = advancedSum(count, parent.getProvided(index));
			parent = parent.parent;
		}
		return count;
	}

	@Override
	public long requestFromParent(int index, long count) {
		if (index < 0 || types.length <= index)
			return 0;
		long totalTaken = 0;
		ProvidedResourcesImpl parent = this.parent;
		while (parent != null) {
			index = this.indexes[index];
			long taken = parent.take(index, count);
			totalTaken = advancedSum(totalTaken, taken);
			count = taken < 0 ? 0 : count < 0 ? -1 : count - taken;
			parent = parent.parent;
		}
		return totalTaken;
	}

	private long getMaxRequestMultiplier(long[] counts, long maxMultiplier) {
		if (maxMultiplier < 0)
			maxMultiplier = -1;
		for (int i = 0; i < types.length; i++) {
			long maxRequest = getMaxRequestFromParents(i);
			if (maxRequest < 0)
				continue;
			if (counts[i] < 0)
				return 0;
			if (counts[i] == 0)
				continue;
			long limit = maxRequest / counts[i];
			if (maxMultiplier < 0 || limit < maxMultiplier)
				maxMultiplier = maxRequest;
		}
		return maxMultiplier;
	}

	@Override
	public long requestFromParent(long[] counts, long maxMultiplier) {
		maxMultiplier = getMaxRequestMultiplier(counts, maxMultiplier);
		if (maxMultiplier == 0)
			return 0;

		for (int i = 0; i < types.length; i++) {
			if (counts[i] == 0)
				continue;
			long count = counts[i] < 0 || maxMultiplier < 0 ? -1 : counts[i] * maxMultiplier;
			requestFromParent(i, count);
			provided[i] = advancedSum(provided[i], count);
		}
		return maxMultiplier;
	}

	@Override
	public void returnToParent(int index, long count) {
		if (parent == null || index < 0 || types.length <= index)
			return;
		long current = provided[index];
		if (current < 0) {
			parent.provide(index, count);
			return;
		}
		if (count < 0 || current <= count)
			count = current;
		parent.provide(index, count);
		provided[index] = current - count;
	}

	@Override
	public void returnAllToParent() {
		for (int i = 0; i < types.length; i++)
			returnToParent(i, -1L);
	}

	@Override
	public ProvidedResources subProvidedResources(int[] indexes) {
		indexes = indexes.clone();

		int len = indexes.length;
		ElementType[] types = new ElementType[len];
		for (int i = 0; i < len; i++)
			types[i] = this.types[indexes[i]];

		return new ProvidedResourcesImpl(this, types, indexes);
	}

	@Override
	public ProvidedResources subProvidedResources() {
		int len = types.length;
		int[] indexes = new int[len];
		for (int i = 0; i < len; i++)
			indexes[i] = i;

		return new ProvidedResourcesImpl(this, types, indexes);
	}

	@Override
	public void load(ListTag saved) {
		for (Tag savedElement : saved) {
			if (!(savedElement instanceof CompoundTag savedElementCompound))
				continue;
			ElementTypeStack stack = ElementTypeStack.load(savedElementCompound);
			ElementType type = stack.getType();
			for (int i = 0; i < types.length; i++) {
				if (types[i].equals(type)) {
					provide(i, stack.getCount());
					break;
				}
			}
		}
	}

	@Override
	public ListTag save() {
		ListTag saved = new ListTag();
		for (int i = 0; i < types.length; i++)
			saved.add(ElementTypeStack.save(ElementTypeStack.of(types[i], provided[i])));
		return saved;
	}
}
