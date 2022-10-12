package com.ofek2608.deep_pocket_conversions;

import com.mojang.brigadier.StringReader;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;

public final class DPCUtils {
	private DPCUtils() {}

	public static ItemType parseItemType(String name) {
		try {
			@SuppressWarnings("deprecation")
			ItemParser.ItemResult parsed = ItemParser.parseForItem(HolderLookup.forRegistry(Registry.ITEM), new StringReader(name));
			Holder<Item> item = parsed.item();
			if (!item.isBound())
				throw new IllegalArgumentException();
			return new ItemType(item.value(), parsed.nbt());
		} catch (Exception e) {
			return ItemType.EMPTY;
		}
	}
}
