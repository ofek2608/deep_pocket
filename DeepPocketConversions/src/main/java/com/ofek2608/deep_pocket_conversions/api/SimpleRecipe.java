package com.ofek2608.deep_pocket_conversions.api;

import com.ofek2608.deep_pocket.api.struct.ItemType;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.List;

@Immutable
public final class SimpleRecipe {
	public final ItemCount output;
	public final List<IngredientCount> input;

	public SimpleRecipe(ItemCount output, IngredientCount ... input) {
		this.output = output;
		this.input = List.of(input);
	}

	public ItemCount getOutput() {
		return output;
	}

	public List<IngredientCount> getInput() {
		return input;
	}

	@Immutable
	public static final class IngredientCount {
		public final @Nonnull Ingredient ingredient;
		public final long count;

		public IngredientCount(Ingredient ingredient, long count) {
			this.ingredient = ingredient;
			this.count = count;
		}

		public Ingredient getIngredient() {
			return ingredient;
		}

		public long getCount() {
			return count;
		}
	}

	@Immutable
	public static final class ItemCount {
		public final @Nonnull ItemType item;
		public final long count;

		public ItemCount(ItemType item, long count) {
			this.item = item;
			this.count = count;
		}

		public ItemType getItem() {
			return item;
		}

		public long getCount() {
			return count;
		}
	}
}
