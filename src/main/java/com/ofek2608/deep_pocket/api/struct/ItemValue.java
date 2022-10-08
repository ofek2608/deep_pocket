package com.ofek2608.deep_pocket.api.struct;

import javax.annotation.concurrent.Immutable;
import java.util.HashMap;
import java.util.Map;

@Immutable
public final class ItemValue {
	public final Map<ItemType, Double> items;

	public ItemValue(Map<ItemType, Double> items) {
		this.items = Map.copyOf(items);
		for (double count : items.values())
			if (count <= 0)
				throw new IllegalArgumentException("ItemValue: negative count");
	}

	private ItemValue(Builder builder) {
		this.items = Map.copyOf(builder.items);
	}

	public static class Builder {
		private final Map<ItemType, Double> items;

		public Builder() {
			this.items = new HashMap<>();
		}

		public Builder(Builder copy) {
			this.items = new HashMap<>(copy.items);
		}

		public double get(ItemType item) {
			return items.getOrDefault(item, 0.0);
		}

		public Builder set(ItemType item, double value) {
			if (value < 0)
				throw new IllegalArgumentException("value");
			if (value == 0)
				items.remove(item);
			else
				items.put(item, value);
			return this;
		}

		public Builder add(ItemType item, double value) {
			return set(item, get(item) + value);
		}

		public ItemValue build() {
			return new ItemValue(this);
		}
	}
}
