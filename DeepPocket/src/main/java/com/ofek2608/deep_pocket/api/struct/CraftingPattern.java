package com.ofek2608.deep_pocket.api.struct;

import com.ofek2608.deep_pocket.DeepPocketUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Arrays;
import java.util.UUID;

public sealed class CraftingPattern permits WorldCraftingPattern {
	private final UUID patternId;
	private final ItemAmount[] input;
	private final ItemTypeAmount[] output;

	public CraftingPattern(UUID patternId, ItemAmount[] input, ItemTypeAmount[] output) {
		this.patternId = patternId;
		this.input = input;
		this.output = output;
	}

	public CraftingPattern(CompoundTag saved) {
		this.patternId = saved.getUUID("patternId");
		this.input = DeepPocketUtils.loadArray(saved.getList("input", 8), ItemAmount[]::new, ItemAmount::new);
		this.output = DeepPocketUtils.loadArray(saved.getList("output", 8), ItemTypeAmount[]::new, ItemTypeAmount::new);
	}

	public CompoundTag save() {
		CompoundTag saved = new CompoundTag();
		saved.putUUID("patternId", patternId);
		saved.put("input", DeepPocketUtils.saveArray(input, ItemAmount::save));
		saved.put("output", DeepPocketUtils.saveArray(output, ItemTypeAmount::save));
		return saved;
	}




	public static void encode(FriendlyByteBuf buf, CraftingPattern pattern) {
		buf.writeUUID(pattern.patternId);
		DeepPocketUtils.encodeArray(buf, pattern.input, ItemAmount::encode);
		DeepPocketUtils.encodeArray(buf, pattern.output, ItemTypeAmount::encode);
	}

	public static CraftingPattern decode(FriendlyByteBuf buf) {
		return new CraftingPattern(
						buf.readUUID(),
						DeepPocketUtils.decodeArray(buf, ItemAmount[]::new, ItemAmount::decode),
						DeepPocketUtils.decodeArray(buf, ItemTypeAmount[]::new, ItemTypeAmount::decode)
		);
	}

	public UUID getPatternId() {
		return patternId;
	}

	public ItemAmount[] getInput() {
		return Arrays.copyOf(input, input.length);
	}

	public ItemTypeAmount[] getOutput() {
		return Arrays.copyOf(output, output.length);
	}
}
