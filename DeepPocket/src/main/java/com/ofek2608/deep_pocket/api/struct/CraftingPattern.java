package com.ofek2608.deep_pocket.api.struct;

import com.ofek2608.deep_pocket.DeepPocketUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public sealed class CraftingPattern permits WorldCraftingPattern {
	private final UUID patternId;
	private final Item[] input;
	private final Item[] output;

	public CraftingPattern(UUID patternId, Item[] input, Item[] output) {
		this.patternId = patternId;
		this.input = input;
		this.output = output;
	}

	public CraftingPattern(CompoundTag saved) {
		this.patternId = saved.getUUID("patternId");
		this.input = loadItemArray(saved.getList("input", 8));
		this.output = loadItemArray(saved.getList("output", 8));
	}

	public CompoundTag save() {
		CompoundTag saved = new CompoundTag();
		saved.putUUID("patternId", patternId);
		saved.put("input", saveItemArray(input));
		saved.put("output", saveItemArray(output));
		return saved;
	}

	private static Item[] loadItemArray(ListTag lst) {
		return lst.stream()
						.map(Object::toString)
						.map(ResourceLocation::new)
						.map(ForgeRegistries.ITEMS::getValue)
						.filter(Objects::nonNull)
						.toArray(Item[]::new);
	}

	private static ListTag saveItemArray(Item[] items) {
		ListTag saved = new ListTag();
		Stream.of(items)
						.map(ForgeRegistries.ITEMS::getKey)
						.filter(Objects::nonNull)
						.map(Object::toString)
						.map(StringTag::valueOf)
						.forEach(saved::add);
		return saved;
	}




	public static void encode(FriendlyByteBuf buf, CraftingPattern pattern) {
		buf.writeUUID(pattern.patternId);
		buf.writeVarInt(pattern.input.length);
		for (Item item : pattern.input)
			DeepPocketUtils.encodeItem(buf, item);
		buf.writeVarInt(pattern.output.length);
		for (Item item : pattern.output)
			DeepPocketUtils.encodeItem(buf, item);
	}

	public static CraftingPattern decode(FriendlyByteBuf buf) {
		UUID patternId = buf.readUUID();
		Item[] input = new Item[buf.readVarInt()];
		for (int i = 0; i < input.length; i++)
			input[i] = DeepPocketUtils.decodeItem(buf);
		Item[] output = new Item[buf.readVarInt()];
		for (int i = 0; i < output.length; i++)
			output[i] = DeepPocketUtils.decodeItem(buf);
		return new CraftingPattern(patternId, input, output);
	}

	public UUID getPatternId() {
		return patternId;
	}

	public Item[] getInput() {
		return Arrays.copyOf(input, input.length);
	}

	public Item[] getOutput() {
		return Arrays.copyOf(output, output.length);
	}
}
