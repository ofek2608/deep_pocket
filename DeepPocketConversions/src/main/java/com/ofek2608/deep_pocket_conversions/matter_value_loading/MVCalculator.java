package com.ofek2608.deep_pocket_conversions.matter_value_loading;

import com.mojang.logging.LogUtils;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket_conversions.api.*;
import com.ofek2608.deep_pocket_conversions.registry.MatterItem;
import com.ofek2608.deep_pocket_conversions.registry.ModRegistry;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public final class MVCalculator {
	private MVCalculator() {}
	private static final Logger LOGGER = LogUtils.getLogger();



	public static Map<ItemType,Long> calculateMV(Map<String, Long> constants, List<ICompilableRecipe> recipesList, Map<ItemType,String> overrideRecipes, Set<Item> doNotCalculate) {
		// ===Main calculations===
		// Creating context
		MVCalculationContext mvCalculationContext = new MVCalculationContext(constants, recipesList, overrideRecipes, doNotCalculate);
		// Trying to solve all item values
		// Technically I need to pass only types.length
		// But I pass more in hope to get to a stable value
		int loopTimes = 16 * mvCalculationContext.types.length + 1;
		for (int i = 0; i < loopTimes; i++)
			mvCalculationContext.passOverRecipes();
		// Removing unstable value while I can (while last call changed something)
		// If there is a recipe to recheck, the result of the item is in an infinite loop that decrement each time
		// But maybe I haven't got to a stable position, so I won't set a value 0 for it.
		while(mvCalculationContext.removeUnstableValues());
		//Build and return
		return mvCalculationContext.buildValues();
	}

	private static Map<ItemType, Function<IMVCalculationCtx, ValueRule>> getRecipesCompilers(List<ICompilableRecipe> recipesList, Map<ItemType,String> overrideRecipes, Set<Item> doNotCalculate) {
		// Creating the result map
		Map<ItemType, Function<IMVCalculationCtx, ValueRule>> recipesCompilers = new HashMap<>();
		// Group all recipes by the result
		Map<ItemType,List<ICompilableRecipe>> recipesByResult = new HashMap<>();
		for (ICompilableRecipe compilableRecipe : recipesList) {
			ItemType result = compilableRecipe.getResultType();
			if (result.isEmpty() || overrideRecipes.containsKey(result) || doNotCalculate.contains(result.getItem()))
				continue;
			recipesByResult.computeIfAbsent(result,p->new ArrayList<>()).add(compilableRecipe);
		}
		// Adding recipesList to recipesCompilers
		recipesByResult.forEach((type, compilableRecipes) -> recipesCompilers.put(
						type,
						ctx->ValueRule.min(
										compilableRecipes.stream()
														.map(compilableRecipe->compilableRecipe.compile(ctx))
														.filter(Objects::nonNull)
														.toArray(ValueRule[]::new)
						)
		));
		// Adding overrideRecipes to recipesCompilers
		overrideRecipes.forEach(((type, code) -> recipesCompilers.put(type, ctx->parse(ctx, type, code))));
		// Removing matter_1 in case that someone added it
		recipesCompilers.remove(new ItemType(ModRegistry.getMinMatter()));
		// Adding the rest of the matters
		for (int num = MatterItem.MIN_MATTER_NUM + 1; num <= MatterItem.MAX_MATTER_NUM; num++) {
			MatterItem item = ModRegistry.getMatter(num);
			recipesCompilers.put(new ItemType(item), ctx->ValueRule.constant(ProcessingNum.valueOf(item.value)));
		}
		// Return
		return recipesCompilers;
	}

	private static ValueRule parse(IMVCalculationCtx ctx, ItemType type, String code) {
		try {
			return ValueRule.parse(ctx, code);
		} catch (ParsingException e) {
			LOGGER.error("Couldn't parse matter value for " + type + " with '" + code + "'");
			return ValueRule.constant(ProcessingNum.UNDEFINED);
		}
	}

	private static Map<ItemType,Integer> createTypeIndexes(ItemType[] types) {
		Map<ItemType,Integer> typeIndexes = new HashMap<>();
		for (int i = 0; i < types.length; i++)
			typeIndexes.put(types[i], i);
		return typeIndexes;
	}

	private static int[][] calcDependentBy(ValueRule[] valueRules) {
		@SuppressWarnings("unchecked")
		List<Integer>[] dependentBy = (List<Integer>[]) new List<?>[valueRules.length];
		for (int i = 0; i < dependentBy.length; i++)
			dependentBy[i] = new ArrayList<>();
		for (int valueIndex = 0; valueIndex < valueRules.length; valueIndex++) {
			ValueRule value = valueRules[valueIndex];
			if (value == null)
				continue;
			int finalValueIndex = valueIndex;
			value.getDependencies().forEach(dependency->dependentBy[dependency].add(finalValueIndex));
		}
		return Arrays.stream(dependentBy).map(List::stream).map(stream->stream.mapToInt(Integer::intValue).toArray()).toArray(int[][]::new);
	}

	private static final class MVCalculationContext implements IMVCalculationCtx {
		private final Map<String,Long> constants;
		private final ItemType[] types;
		private final List<ItemType> typesList;
		private final Map<ItemType,Integer> typeIndex;
		private final boolean[] hasValue;
		private final long[] values;
		private final ValueRule[] valueRules;
		private final int[][] dependentBy;
		private final boolean[] needToRecalculate;

		private MVCalculationContext(Map<String, Long> constants, List<ICompilableRecipe> compilableRecipes, Map<ItemType,String> overrideRecipes, Set<Item> doNotCalculate) {
			var recipesCompilers = new ArrayList<>(getRecipesCompilers(compilableRecipes, overrideRecipes, doNotCalculate).entrySet());
			this.constants = constants;
			this.types = Stream.concat(
							Stream.of(new ItemType(ModRegistry.getMinMatter())),
							recipesCompilers.stream().map(Map.Entry::getKey)
			).toArray(ItemType[]::new);
			this.typesList = List.of(this.types);
			this.typeIndex = createTypeIndexes(this.types);
			this.hasValue = new boolean[this.types.length];
			this.values = new long[this.types.length];
			this.hasValue[0] = true;
			this.values[0] = 1L;
			this.valueRules = new ValueRule[this.types.length];
			for (int i = 1; i < this.types.length; i++)
				this.valueRules[i] = recipesCompilers.get(i - 1).getValue().apply(this);
			this.dependentBy = calcDependentBy(this.valueRules);
			this.needToRecalculate = new boolean[this.types.length];
			Arrays.fill(this.needToRecalculate, true);
		}

		@Override
		public boolean hasConst(String name) {
			return constants.containsKey(name);
		}

		@Override
		public long getConst(String name) {
			return constants.get(name);
		}

		@Override
		public @UnmodifiableView List<ItemType> getTypes() {
			return typesList;
		}

		@Override
		public int getTypeIndex(ItemType type) {
			return typeIndex.getOrDefault(type, -1);
		}

		@Override
		public boolean hasValue(int index) {
			return hasValue[index];
		}

		@Override
		public long getValue(int index) {
			return values[index];
		}

		private void triggerRequiredRecipes(int index) {
			for (int dependentIndex : dependentBy[index])
				needToRecalculate[dependentIndex] = true;
		}

		private void limitValue(int index, long value) {
			if (index == 0)
				return; //just in case
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
			if (index == 0)
				return; //just in case
			hasValue[index] = false;
			triggerRequiredRecipes(index);
		}

		private OptionalLong calculateValue(ValueRule valueRule) {
			BigInteger value = valueRule.getValue(this).asBigInt();
			if (value == null) {
				return OptionalLong.empty();
			}
			try {
				long longValue = value.longValueExact();
				return OptionalLong.of(longValue == 0 ? 1 : longValue);
			} catch (Exception e) {
				return OptionalLong.of(-1);
			}
		}

		private void passOverRecipes() {
			for (int i = 0; i < valueRules.length; i++) {
				if (!needToRecalculate[i])
					continue;
				needToRecalculate[i] = false;
				ValueRule valueRule = valueRules[i];
				if (valueRule == null)
					continue;
				OptionalLong value = calculateValue(valueRule);
				if (value.isPresent())
					limitValue(i, value.getAsLong());
			}
		}

		private boolean removeUnstableValues() {
			boolean changed = false;
			for (int i = 0; i < needToRecalculate.length; i++) {
				if (!needToRecalculate[i])
					continue;
				needToRecalculate[i] = false;
				ValueRule valueRule = valueRules[i];
				if (valueRule == null)
					continue;
				if (!hasValue[i])
					continue;
				OptionalLong newValue = calculateValue(valueRule);
				if (newValue.isPresent() && newValue.getAsLong() == values[i])
					continue;
				removeValue(i);
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
