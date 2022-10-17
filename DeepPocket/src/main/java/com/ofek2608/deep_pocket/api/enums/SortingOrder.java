package com.ofek2608.deep_pocket.api.enums;

import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;

@SuppressWarnings("deprecation")
public enum SortingOrder {
	COUNT("Count", Comparator.comparingLong(typeAmount->{
		long count = typeAmount.getAmount();
		return count < 0 ? Long.MAX_VALUE : count - 1;
	})),
	ID("ID", Comparator.comparingInt(ta->Registry.ITEM.getId(ta.getItemType().getItem()))),
	NAME("Name", Comparator.comparing(ta->ta.getItemType().create().getDisplayName().getString())),
	MOD("Mod", Comparator.comparing(ta->{
		ResourceLocation loc = ForgeRegistries.ITEMS.getKey(ta.getItemType().getItem());
		return loc == null ? new ResourceLocation("") : loc;
	}, ResourceLocation::compareNamespaced));
	public final String displayName;
	private final Comparator<ItemTypeAmount> baseComparator;
	public final Comparator<ItemTypeAmount> comparator;

	SortingOrder(String displayName, Comparator<ItemTypeAmount> baseComparator) {
		this.displayName = displayName;
		this.baseComparator = baseComparator;
		this.comparator = baseComparator.thenComparing(SortingOrder::arbitraryCompare);
	}

	public static int arbitraryCompare(ItemTypeAmount ta0, ItemTypeAmount ta1) {
		for (SortingOrder value : values()) {
			int comp = value.baseComparator.compare(ta0, ta1);
			if (comp != 0)
				return comp;
		}
		return Integer.compare(ta0.getItemType().getTag().hashCode(), ta1.getItemType().getTag().hashCode());
	}
}
