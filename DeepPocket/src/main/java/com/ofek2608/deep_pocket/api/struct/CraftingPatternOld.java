package com.ofek2608.deep_pocket.api.struct;

import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Deprecated(forRemoval = true)
public sealed class CraftingPatternOld permits WorldCraftingPatternOld {
	private final UUID patternId;
	private final ItemTypeAmount[] input;
	private final ItemTypeAmount[] output;

	public CraftingPatternOld(UUID patternId, ItemTypeAmount[] input, ItemTypeAmount[] output) {
		this.patternId = patternId;
		this.input = input;
		this.output = output;
	}

	public CraftingPatternOld(CompoundTag saved) {
		this.patternId = saved.getUUID("patternId");
		this.input = DeepPocketUtils.loadArray(saved.getList("input", 10), ItemTypeAmount[]::new, ItemTypeAmount::new);
		this.output = DeepPocketUtils.loadArray(saved.getList("output", 10), ItemTypeAmount[]::new, ItemTypeAmount::new);
	}

	public CompoundTag save() {
		CompoundTag saved = new CompoundTag();
		saved.putUUID("patternId", patternId);
		saved.put("input", DeepPocketUtils.saveArray(input, ItemTypeAmount::save));
		saved.put("output", DeepPocketUtils.saveArray(output, ItemTypeAmount::save));
		return saved;
	}




	public static void encode(FriendlyByteBuf buf, CraftingPatternOld pattern) {
		buf.writeUUID(pattern.patternId);
		DeepPocketUtils.encodeArray(buf, pattern.input, ItemTypeAmount::encode);
		DeepPocketUtils.encodeArray(buf, pattern.output, ItemTypeAmount::encode);
	}

	public static CraftingPatternOld decode(FriendlyByteBuf buf) {
		return new CraftingPatternOld(
						buf.readUUID(),
						DeepPocketUtils.decodeArray(buf, ItemTypeAmount[]::new, ItemTypeAmount::decode),
						DeepPocketUtils.decodeArray(buf, ItemTypeAmount[]::new, ItemTypeAmount::decode)
		);
	}

	public UUID getPatternId() {
		return patternId;
	}

	public ItemTypeAmount[] getInput() {
		return Arrays.copyOf(input, input.length);
	}

	public ItemTypeAmount[] getOutput() {
		return Arrays.copyOf(output, output.length);
	}

	public @UnmodifiableView Map<ItemType,Long> getInputCountMap() {
		return getCountMap(input);
	}

	public @UnmodifiableView Map<ItemType,Long> getOutputCountMap() {
		return getCountMap(output);
	}

	private static @UnmodifiableView Map<ItemType,Long> getCountMap(ItemTypeAmount[] arr) {
		HashMap<ItemType,Long> result = new HashMap<>();
		for (ItemTypeAmount itemTypeAmount : arr) {
			if (itemTypeAmount.isEmpty())
				continue;
			result.compute(itemTypeAmount.getItemType(), (t,a) -> (a == null ? 0 : a) + itemTypeAmount.getAmount());
		}
		return result;
	}
}
