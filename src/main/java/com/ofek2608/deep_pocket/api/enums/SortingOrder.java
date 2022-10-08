package com.ofek2608.deep_pocket.api.enums;

import com.ofek2608.deep_pocket.api.struct.ItemType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.Map;

public enum SortingOrder {
	COUNT("Count", Comparator.comparingDouble(Map.Entry::getValue)),
	ID("ID", Comparator.comparingInt(e->{
		//noinspection deprecation
		return Registry.ITEM.getId(e.getKey().getItem());
	})),
	NAME("Name", Comparator.comparing(e->e.getKey().create().getDisplayName().getString())),
	MOD("Mod", Comparator.comparing(e->{
		ResourceLocation loc = ForgeRegistries.ITEMS.getKey(e.getKey().getItem());
		return loc == null ? new ResourceLocation("") : loc;
	}, ResourceLocation::compareNamespaced));
	public final String displayName;
	private final Comparator<Map.Entry<ItemType,Double>> baseComparator;
	public final Comparator<Map.Entry<ItemType,Double>> comparator;

	SortingOrder(String displayName, Comparator<Map.Entry<ItemType,Double>> baseComparator) {
		this.displayName = displayName;
		this.baseComparator = baseComparator;
		this.comparator = baseComparator.thenComparing(SortingOrder::arbitraryCompare);
	}

	public static int arbitraryCompare(Map.Entry<ItemType,Double> e0, Map.Entry<ItemType,Double> e1) {
		for (SortingOrder value : values()) {
			int comp = value.baseComparator.compare(e0, e1);
			if (comp != 0)
				return comp;
		}
		return Integer.compare(e0.getKey().getTag().hashCode(), e1.getKey().getTag().hashCode());
	}
}
