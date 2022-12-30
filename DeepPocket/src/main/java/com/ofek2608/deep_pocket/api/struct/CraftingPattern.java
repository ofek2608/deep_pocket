package com.ofek2608.deep_pocket.api.struct;

import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Arrays;
import java.util.HashMap;

import static com.ofek2608.deep_pocket.utils.AdvancedLongMath.advancedSum;

public final class CraftingPattern {
	private final ElementTypeStack[] input;
	private final ElementTypeStack[] output;

	public CraftingPattern(ElementTypeStack[] input, ElementTypeStack[] output) {
		this.input = input.clone();
		this.output = output.clone();
	}
	
	public CraftingPattern load(CompoundTag saved) {
		return new CraftingPattern(
				DeepPocketUtils.loadArray(saved.getList("input", 10), ElementTypeStack[]::new, ElementTypeStack::load),
				DeepPocketUtils.loadArray(saved.getList("output", 10), ElementTypeStack[]::new, ElementTypeStack::load)
		);
	}

	public static CompoundTag save(CraftingPattern pattern) {
		CompoundTag saved = new CompoundTag();
		saved.put("input", DeepPocketUtils.saveArray(pattern.input, ElementTypeStack::save));
		saved.put("output", DeepPocketUtils.saveArray(pattern.output, ElementTypeStack::save));
		return saved;
	}




	public static void encode(FriendlyByteBuf buf, CraftingPattern pattern) {
		DeepPocketUtils.encodeArray(buf, pattern.input, ElementTypeStack::encode);
		DeepPocketUtils.encodeArray(buf, pattern.output, ElementTypeStack::encode);
	}

	public static CraftingPattern decode(FriendlyByteBuf buf) {
		return new CraftingPattern(
						DeepPocketUtils.decodeArray(buf, ElementTypeStack[]::new, ElementTypeStack::decode),
						DeepPocketUtils.decodeArray(buf, ElementTypeStack[]::new, ElementTypeStack::decode)
		);
	}

	public ElementTypeStack[] getInput() {
		return input.clone();
	}

	public ElementTypeStack[] getOutput() {
		return output.clone();
	}

	public ElementTypeStack[] getInputCountMap() {//TODO rename
		return getCountMap(input);
	}

	public ElementTypeStack[] getOutputCountMap() {//TODO rename
		return getCountMap(output);
	}

	private static ElementTypeStack[] getCountMap(ElementTypeStack[] arr) {
		HashMap<ElementType,Long> result = new HashMap<>();
		for (ElementTypeStack stack : arr) {
			if (stack.isEmpty())
				continue;
			result.compute(stack.getType(), (t,a) -> (a == null ? 0 : a) + stack.getCount());
		}
		return result.entrySet()
				.stream()
				.map(entry->ElementTypeStack.of(entry.getKey(), entry.getValue()))
				.toArray(ElementTypeStack[]::new);
	}
	
	public boolean hasOutput(ElementType type) {
		for (ElementTypeStack output : output)
			if (output.getType().equals(type))
				return true;
		return false;
	}
	
	public long getOutputCount(ElementType type) {
		long count = 0;
		for (ElementTypeStack output : output)
			if (output.getType().equals(type))
				count = advancedSum(count, output.getCount());
		return count;
	}
	
	@Override
	public boolean equals(Object o) {
		return this == o ||
				o instanceof CraftingPattern that &&
						Arrays.equals(input, that.input) &&
						Arrays.equals(output, that.output);
	}
	
	@Override
	public int hashCode() {
		return 31 * Arrays.hashCode(input) + Arrays.hashCode(output);
	}
}
