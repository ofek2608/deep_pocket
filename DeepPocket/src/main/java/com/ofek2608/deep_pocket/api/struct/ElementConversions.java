package com.ofek2608.deep_pocket.api.struct;

import com.mojang.logging.LogUtils;
import com.ofek2608.deep_pocket.DeepPocketUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.*;
import java.util.stream.Stream;

import static com.ofek2608.deep_pocket.DeepPocketUtils.advancedMul;
import static com.ofek2608.deep_pocket.DeepPocketUtils.advancedSum;
import static com.ofek2608.deep_pocket.api.struct.ElementType.TConvertible;

@Immutable
public final class ElementConversions {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final ElementConversions EMPTY = new ElementConversions(new TConvertible[0], Collections.emptyMap());
	private final TConvertible[] baseElements;
	private final Map<ElementType,long[]> values;

	private ElementConversions(TConvertible[] baseElements, Map<ElementType,long[]> values) {
		this.baseElements = baseElements;
		this.values = values;
	}


	public static void encode(FriendlyByteBuf buf, ElementConversions conversions) {
		// Write base elements keys
		buf.writeVarInt(conversions.baseElements.length);
		for (ElementType baseElement : conversions.baseElements)
			buf.writeResourceLocation(baseElement.getKey());
		// Write converted elements
		for (var entry : conversions.values.entrySet()) {
			ElementType.encode(buf, entry.getKey());
			for (long baseElementCost : entry.getValue())
				buf.writeVarLong(baseElementCost);
		}
		// Write ElementType.empty() to mark the end
		ElementType.encode(buf, ElementType.empty());
	}

	public static ElementConversions decode(FriendlyByteBuf buf) {
		// Read base elements keys
		int baseElementCount = buf.readVarInt();
		TConvertible[] baseElements = new TConvertible[baseElementCount];
		for (int i = 0; i < baseElementCount; i++)
			baseElements[i] = ElementType.convertible(buf.readResourceLocation());
		// Read converted elements
		Map<ElementType,long[]> values = new HashMap<>();
		ElementType readElementType;
		while (!(readElementType = ElementType.decode(buf)).isEmpty()) {
			if (readElementType instanceof TConvertible)
				throw new IllegalArgumentException("ElementConversions-TConvertible has a value.");
			long[] value = new long[baseElementCount];
			for (int i = 0; i < baseElementCount; i++)
				value[i] = buf.readVarLong();
			values.put(readElementType, value);
		}
		return new ElementConversions(baseElements, values);
	}

	public int getBaseElementCount() {
		return baseElements.length;
	}

	public TConvertible getBaseElement(int baseElementIndex) {
		return baseElements[baseElementIndex];
	}

	public TConvertible[] getBaseElements() {
		return baseElements.clone();
	}

	public @UnmodifiableView Set<ElementType> getKeys() {
		return Collections.unmodifiableSet(values.keySet());
	}

	public boolean hasValue(ElementType type) {
		return values.containsKey(type);
	}

	public @Nullable long[] getValue(ElementType type) {
		long[] value = values.get(type);
		return value == null ? null : value.clone();
	}

	public @Nullable long[] getValue(ElementType type, long count) {
		long[] value = values.get(type);
		if (value == null)
			return null;
		value = value.clone();
		for (int i = 0; i < value.length; i++)
			value[i] = advancedMul(value[i], count);
		return value;
	}

	public void convertMap(Map<ElementType,Long> counts) {
		for (var entry : new ArrayList<>(counts.entrySet())) {
			if (entry.getKey().isEmpty()) {
				counts.remove(entry.getKey());
				continue;
			}
			long[] value = getValue(entry.getKey(), entry.getValue());
			if (value == null)
				continue;
			counts.remove(entry.getKey());
			for (int i = 0; i < value.length; i++) {
				long v = value[i];
				if (v == 0)
					continue;
				ElementType baseElement = baseElements[i];
				counts.put(baseElement, DeepPocketUtils.advancedSum(v, counts.getOrDefault(baseElement, 0L)));
			}
		}
	}
















	public static final class Builder {
		public final Map<ElementType,ElementValueBuilder> values = new HashMap<>();

		public ElementValueBuilder element(ElementType element) {
			if (element.isEmpty() || element instanceof TConvertible)
				throw new IllegalArgumentException("element can't be TEmpty or TConvertible");
			return values.computeIfAbsent(element, p->new ElementValueBuilder());
		}

		public ElementValueBuilder item(ItemStack stack) { return element(ElementType.item(stack)); }
		public ElementValueBuilder item(Item item) { return element(ElementType.item(item)); }
		public ElementValueBuilder fluid(FluidStack stack) { return element(ElementType.fluid(stack)); }
		public ElementValueBuilder fluid(Fluid fluid) { return element(ElementType.fluid(fluid)); }
		public ElementValueBuilder energy() { return element(ElementType.energy()); }

		public ElementConversions build() {
			TConvertible[] baseElements = values.values()
					.stream()
					.map(builder->builder.amounts)
					.map(Map::keySet)
					.flatMap(Set::stream)
					.unordered()
					.distinct()
					.map(ElementType::convertible)
					.toArray(TConvertible[]::new);
			
			Map<ElementType, long[]> resultValues = new HashMap<>();
			for (var entry : values.entrySet()) {
				ElementType element = entry.getKey();
				if (element.isEmpty() || element instanceof TConvertible) {
					LOGGER.warn("Tried to build a value to " + element);
					continue;
				}
				resultValues.put(
						element,
						Stream.of(baseElements)
								.map(TConvertible::getKey)
								.mapToLong(entry.getValue()::getAmount)
								.toArray()
				);
			}
			return new ElementConversions(baseElements, resultValues);
		}
	}

	public static final class ElementValueBuilder {
		public final Map<ResourceLocation,Long> amounts = new HashMap<>();
		
		public ElementValueBuilder clear() {
			amounts.clear();
			return this;
		}

		public ElementValueBuilder set(ResourceLocation convertible, long amount) {
			amounts.put(convertible, amount);
			return this;
		}

		public ElementValueBuilder add(ResourceLocation convertible, long amount) {
			return set(convertible, advancedSum(amount, getAmount(convertible)));
		}
		
		public long getAmount(ResourceLocation convertible) {
			return amounts.getOrDefault(convertible, 0L);
		}
	}
}
