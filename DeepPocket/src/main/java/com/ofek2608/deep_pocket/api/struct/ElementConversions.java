package com.ofek2608.deep_pocket.api.struct;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.*;
import java.util.stream.IntStream;

import static com.ofek2608.deep_pocket.utils.AdvancedLongMath.advancedMul;
import static com.ofek2608.deep_pocket.utils.AdvancedLongMath.advancedSum;

@Immutable
public final class ElementConversions {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final ElementConversions EMPTY = new ElementConversions(new int[0], Collections.emptyMap());
	private final int[] baseElements;
	private final Map<Integer,long[]> values;

	private ElementConversions(int[] baseElements, Map<Integer,long[]> values) {
		this.baseElements = baseElements;
		this.values = values;
	}


	public static void encode(FriendlyByteBuf buf, ElementConversions conversions) {
		// Write base elements keys
		buf.writeVarInt(conversions.baseElements.length);
		for (int baseElement : conversions.baseElements)
			buf.writeVarInt(baseElement);
		// Write converted elements
		buf.writeVarInt(conversions.values.size());
		for (var entry : conversions.values.entrySet()) {
			buf.writeVarInt(entry.getKey());
			for (long baseElementCost : entry.getValue())
				buf.writeVarLong(baseElementCost);
		}
	}

	public static ElementConversions decode(FriendlyByteBuf buf) {
		// Read base elements keys
		int baseElementCount = buf.readVarInt();
		int[] baseElements = new int[baseElementCount];
		Set<Integer> baseElementsSet = new HashSet<>();
		for (int i = 0; i < baseElementCount; i++) {
			int baseElement = buf.readVarInt();
			baseElements[i] = baseElement;
			baseElementsSet.add(baseElement);
		}
		// Read converted elements
		int convertibleCount = buf.readVarInt();
		Map<Integer,long[]> values = new HashMap<>();
		for (int i = 0; i < convertibleCount; i++) {
			int elementIndex = buf.readVarInt();
			if (baseElementsSet.contains(elementIndex)) {
				throw new IllegalArgumentException("ElementConversions-TConvertible has a value.");
			}
			long[] value = new long[baseElementCount];
			for (int j = 0; j < baseElementCount; j++)
				value[j] = buf.readVarLong();
			values.put(elementIndex, value);
		}
		return new ElementConversions(baseElements, values);
	}

	public int getBaseElementCount() {
		return baseElements.length;
	}

	public int getBaseElement(int baseElementIndex) {
		return baseElements[baseElementIndex];
	}

	public int[] getBaseElements() {
		return baseElements.clone();
	}

	public @UnmodifiableView Set<Integer> getKeys() {
		return Collections.unmodifiableSet(values.keySet());
	}

	public boolean hasValue(int elementId) {
		return values.containsKey(elementId);
	}

	public @Nullable long[] getValue(int elementId) {
		long[] value = values.get(elementId);
		return value == null ? null : value.clone();
	}

	public @Nullable long[] getValue(int elementId, long count) {
		long[] value = values.get(elementId);
		if (value == null)
			return null;
		value = value.clone();
		for (int i = 0; i < value.length; i++)
			value[i] = advancedMul(value[i], count);
		return value;
	}

	public void convertMap(Map<Integer,Long> counts) {
		for (var entry : new ArrayList<>(counts.entrySet())) {
			long[] value = getValue(entry.getKey(), entry.getValue());
			if (value == null)
				continue;
			counts.remove(entry.getKey());
			for (int i = 0; i < value.length; i++) {
				long v = value[i];
				if (v == 0)
					continue;
				int baseElement = baseElements[i];
				long newCount = advancedSum(v, counts.getOrDefault(baseElement, 0L));
				if (newCount != 0) {
					counts.put(baseElement, newCount);
				}
			}
		}
	}
	
	public long[] convertMapToArray(Map<Integer,Long> counts) {
		long[] result = new long[baseElements.length];
		var iterator = counts.entrySet().iterator();
		while (iterator.hasNext()) {
			var entry = iterator.next();
			long[] elementValue = values.get(entry.getKey());
			if (elementValue == null)
				continue;
			long count = entry.getValue();
			iterator.remove();
			for (int i = 0; i < result.length; i++)
				result[i] = advancedSum(result[i], advancedMul(elementValue[i], count));
		}
		return result;
	}
















	public static final class Builder {
		public final Map<Integer,ElementValueBuilder> values = new HashMap<>();

		public ElementValueBuilder element(int elementId) {
			return values.computeIfAbsent(elementId, p->new ElementValueBuilder());
		}

		public ElementConversions build() {
			int[] baseElements = values.values()
					.stream()
					.map(builder->builder.amounts)
					.map(Map::keySet)
					.flatMap(Set::stream)
					.mapToInt(Integer::valueOf)
					.unordered()
					.distinct()
					.toArray();
			
			Map<Integer, long[]> resultValues = new HashMap<>();
			for (var entry : values.entrySet()) {
				int element = entry.getKey();
				resultValues.put(
						element,
						IntStream.of(baseElements)
								.mapToLong(entry.getValue()::getAmount)
								.toArray()
				);
			}
			return new ElementConversions(baseElements, resultValues);
		}
	}

	public static final class ElementValueBuilder {
		public final Map<Integer,Long> amounts = new HashMap<>();
		
		public ElementValueBuilder clear() {
			amounts.clear();
			return this;
		}

		public ElementValueBuilder set(int convertible, long amount) {
			amounts.put(convertible, amount);
			return this;
		}

		public ElementValueBuilder add(int convertible, long amount) {
			return set(convertible, advancedSum(amount, getAmount(convertible)));
		}
		
		public long getAmount(int convertible) {
			return amounts.getOrDefault(convertible, 0L);
		}
	}
}
