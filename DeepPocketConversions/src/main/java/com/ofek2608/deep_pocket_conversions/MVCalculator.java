package com.ofek2608.deep_pocket_conversions;

import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket_conversions.api.SimpleRecipe;
import net.minecraft.world.item.ItemStack;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class MVCalculator {
	private MVCalculator() {}

	public static Map<ItemType,Long> calculateMV(Map<ItemType,Long> map, Collection<SimpleRecipe> recipesCollection, Set<ItemType> doNotCalculate) {
		// Filter recipes, and move to array
		SimpleRecipe[] simpleRecipes = recipesCollection.stream().filter(recipe->{
			ItemType type = recipe.output.item;
			return !(map.containsKey(type) || doNotCalculate.contains(type)) && recipe.output.count > 0 && !recipe.output.item.isEmpty();
		}).toArray(SimpleRecipe[]::new);
		// Get all possible item types
		ItemType[] types = Stream.concat(
						map.keySet().stream(),
						Stream.of(simpleRecipes).map(SimpleRecipe::getOutput).map(SimpleRecipe.ItemCount::getItem)
		).toArray(ItemType[]::new);
		// Create item type indexes
		Map<ItemType,Integer> typeIndexes = createTypeIndexes(types);
		// Parsing recipes
		RecipeParsingContext recipeParsingContext = new RecipeParsingContext(types, typeIndexes);
		ParsedRecipe[] parsedRecipes = Stream.of(simpleRecipes).map(simple->new ParsedRecipe(recipeParsingContext, simple)).toArray(ParsedRecipe[]::new);

		// ===Main calculations===
		// Creating context
		MVCalculationContext mvCalculationContext = new MVCalculationContext(types, parsedRecipes);
		// setting initial values
		for (var entry : map.entrySet())
			mvCalculationContext.limitValue(typeIndexes.get(entry.getKey()), entry.getValue());
		// Trying to solve all item values
		// Technically I need to pass only types.length
		// But I pass more in hope to get to a stable value
		int loopTimes = 16 * types.length + 1;
		for (int i = 0; i < loopTimes; i++)
			mvCalculationContext.passOverRecipes();
		// Removing unstable value while I can (while last call changed something)
		// If there is a recipe to recheck, the result of the item is in an infinite loop that decrement each time
		// But maybe I haven't got to a stable position, so I won't set a value 0 for it.
		while(mvCalculationContext.removeUnstableValues());
		//Build and return
		return mvCalculationContext.buildValues();
	}

	private static Map<ItemType,Integer> createTypeIndexes(ItemType[] types) {
		Map<ItemType,Integer> typeIndexes = new HashMap<>();
		for (int i = 0; i < types.length; i++)
			typeIndexes.put(types[i], i);
		return typeIndexes;
	}

	private static int[][] calcRequiredBy(int typeCount, ParsedRecipe[] parsedRecipes) {
		@SuppressWarnings("unchecked")
		List<Integer>[] requiredBy = (List<Integer>[]) new List<?>[typeCount];
		for (int i = 0; i < requiredBy.length; i++)
			requiredBy[i] = new ArrayList<>();
		for (int recipeIndex = 0; recipeIndex < parsedRecipes.length; recipeIndex++)
			for (ParsedIngredient parsedIngredient : parsedRecipes[recipeIndex].input)
				for (int typeIndex : parsedIngredient.matchedTypes)
					requiredBy[typeIndex].add(recipeIndex);
		return Arrays.stream(requiredBy).map(List::stream).map(stream->stream.mapToInt(Integer::intValue).toArray()).toArray(int[][]::new);
	}







	private static final class RecipeParsingContext {
		private final ItemStack[] stacks;
		private final Map<ItemType,Integer> typeIndexes;

		private RecipeParsingContext(ItemType[] types, Map<ItemType,Integer> typeIndexes) {
			this.stacks = Stream.of(types).map(ItemType::create).toArray(ItemStack[]::new);
			this.typeIndexes = typeIndexes;
		}
	}


	private static final class ParsedRecipe {
		private final ParsedResult output;
		private final ParsedIngredient[] input;

		private ParsedRecipe(RecipeParsingContext ctx, SimpleRecipe simple) {
			this.output = new ParsedResult(ctx, simple.output);
			this.input = simple.input.stream().filter(ing->!ing.ingredient.isEmpty()).map(ingredient->new ParsedIngredient(ctx, ingredient)).toArray(ParsedIngredient[]::new);
		}
	}

	private static final class ParsedResult {
		private final int typeIndex;
		private final long count;

		private ParsedResult(RecipeParsingContext ctx, SimpleRecipe.ItemCount simple) {
			this.typeIndex = ctx.typeIndexes.get(simple.item);
			this.count = simple.count;
		}
	}

	private static final class ParsedIngredient {
		private final int[] matchedTypes;
		private final long count;

		private ParsedIngredient(RecipeParsingContext ctx, SimpleRecipe.IngredientCount simple) {
			this.matchedTypes = IntStream.range(0, ctx.stacks.length).filter(i->simple.ingredient.test(ctx.stacks[i])).toArray();
			this.count = simple.count;
		}
	}

	private static final class MVCalculationContext {
		private final ItemType[] types;
		private final ParsedRecipe[] recipes;
		private final int[][] requiredBy;
		private final long[] values;
		private final boolean[] hasValue;
		private final boolean[] needToRecheckRecipe;

		private MVCalculationContext(ItemType[] types, ParsedRecipe[] recipes) {
			this.types = types;
			this.recipes = recipes;
			this.requiredBy = calcRequiredBy(types.length, recipes);
			this.values = new long[types.length];
			this.hasValue = new boolean[types.length];
			this.needToRecheckRecipe = new boolean[recipes.length];
		}


		private void triggerRequiredRecipes(int index) {
			for (int recipeIndex : requiredBy[index])
				needToRecheckRecipe[recipeIndex] = true;
		}

		private void limitValue(int index, long value) {
			if (hasValue[index] && (value < 0 || (0 <= values[index] && values[index] <= value)))
				return;
			//So either
			// - I don't have a value
			// - The new value is finite and smaller than the old value
			values[index] = value;
			hasValue[index] = true;
			triggerRequiredRecipes(index);
		}

		private void removeValue(int index) {
			hasValue[index] = false;
			triggerRequiredRecipes(index);
		}

		private OptionalLong getRecipeValue(ParsedRecipe recipe) {
			boolean infinite = false;
			//Initial pass: check if everything has a value, and check if it must be infinite.
			for (ParsedIngredient ingredient : recipe.input) {
				boolean cantFind = true;
				boolean thisInfinite = true;
				for (int matchedType : ingredient.matchedTypes) {
					if (!hasValue[matchedType])
						continue;
					cantFind = false;
					if (values[matchedType] >= 0)
						thisInfinite = false;
				}
				if (cantFind)
					return OptionalLong.empty();
				infinite = infinite || thisInfinite;
			}
			if (infinite)
				return OptionalLong.of(-1);
			//Secondary pass: summing up
			BigInteger total = BigInteger.ZERO;
			for (ParsedIngredient ingredient : recipe.input) {
				long min = Long.MAX_VALUE;
				for (int matchedType : ingredient.matchedTypes) {
					if (!hasValue[matchedType])
						continue;
					long value = values[matchedType];
					if (0 <= value && value < min)
						min = value;
				}
				total = total.add(BigInteger.valueOf(min).multiply(BigInteger.valueOf(ingredient.count)));
			}
			//Dividing
			BigInteger outputValue = total.divide(BigInteger.valueOf(recipe.output.count));
			try {
				return OptionalLong.of(outputValue.longValue());
			} catch (Exception e) {
				return OptionalLong.of(-1);//infinite
			}
		}

		private void passOverRecipes() {
			for (int i = 0; i < recipes.length; i++) {
				if (!needToRecheckRecipe[i])
					continue;
				needToRecheckRecipe[i] = false;
				OptionalLong value = getRecipeValue(recipes[i]);
				if (value.isPresent())
					limitValue(recipes[i].output.typeIndex, value.getAsLong());
			}
		}

		private boolean removeUnstableValues() {
			boolean changed = false;
			for (int i = 0; i < recipes.length; i++) {
				if (!needToRecheckRecipe[i])
					continue;
				needToRecheckRecipe[i] = false;
				int resultIndex = recipes[i].output.typeIndex;
				if (!hasValue[resultIndex])
					continue;
				OptionalLong newValue = getRecipeValue(recipes[i]);
				if (newValue.isPresent() && newValue.getAsLong() == values[resultIndex])
					continue;
				removeValue(resultIndex);
				changed = true;
			}
			return changed;
		}

		private Map<ItemType,Long> buildValues() {
			Map<ItemType,Long> result = new HashMap<>();
			for (int i = 0; i < types.length; i++)
				if (hasValue[i])
					result.put(types[i], values[i]);
			return result;
		}
	}
}
