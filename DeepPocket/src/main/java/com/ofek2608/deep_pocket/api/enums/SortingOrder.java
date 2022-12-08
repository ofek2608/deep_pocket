package com.ofek2608.deep_pocket.api.enums;

import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.Objects;

@SuppressWarnings("deprecation")
public enum SortingOrder implements Comparator<Pocket.Entry> {
	COUNT("Count", Comparator.comparingLong(typeAmount->{
		long count = typeAmount.getAmount();
		return count < 0 ? Long.MAX_VALUE : count - 1;
	})) {
		@Override
		public int compare(Pocket.Entry e0, Pocket.Entry e1) {
			int compClass = SortingOrder.compareClass(e0, e1);
			if (compClass != 0)
				return compClass;
			
			long amount0 = e0.getMaxExtract();
			long amount1 = e1.getMaxExtract();
			
			int compAmount = Long.compare(
					amount0 < 0 ? Long.MAX_VALUE : amount0 - 1,
					amount1 < 0 ? Long.MAX_VALUE : amount1 - 1
			);
			if (compAmount != 0)
				return compAmount;
			
			return arbitraryCompare(e0, e1);
		}
	},
	ID("ID", Comparator.comparingInt(ta->Registry.ITEM.getId(ta.getItemType().getItem()))) {
		@Override
		public int compare(Pocket.Entry e0, Pocket.Entry e1) {
			int compClass = SortingOrder.compareClass(e0, e1);
			if (compClass != 0)
				return compClass;
			
			ElementType t0 = e0.getType();
			ElementType t1 = e1.getType();
			
			int compId = 0;
			if (t0 instanceof ElementType.TItem item0)
				compId = Integer.compare(Registry.ITEM.getId(item0.getItem()), Registry.ITEM.getId(((ElementType.TItem)t1).getItem()));
			else if (t0 instanceof ElementType.TFluid fluid0)
				compId = Integer.compare(Registry.FLUID.getId(fluid0.getFluid()), Registry.FLUID.getId(((ElementType.TFluid)t1).getFluid()));
			if (compId != 0)
				return compId;
			
			ResourceLocation key0 = e0.getType().getKey();
			ResourceLocation key1 = e1.getType().getKey();
			int compNamespace = key0.compareNamespaced(key1);
			if (compNamespace != 0)
				return compNamespace;
			return arbitraryCompare(e0, e1);
		}
	},
	NAME("Name", Comparator.comparing(ta->ta.getItemType().create().getDisplayName().getString())) {
		@Override
		public int compare(Pocket.Entry e0, Pocket.Entry e1) {
			int compClass = SortingOrder.compareClass(e0, e1);
			if (compClass != 0)
				return compClass;
			
			String name0 = e0.getType().getDisplayName().toString();
			String name1 = e1.getType().getDisplayName().toString();
			int compName = name0.compareTo(name1);
			if (compName != 0)
				return compName;
			return arbitraryCompare(e0, e1);
		}
	},
	MOD("Mod", Comparator.comparing(ta->{
		ResourceLocation loc = ForgeRegistries.ITEMS.getKey(ta.getItemType().getItem());
		return loc == null ? new ResourceLocation("") : loc;
	}, ResourceLocation::compareNamespaced)) {
		@Override
		public int compare(Pocket.Entry e0, Pocket.Entry e1) {
			int compClass = SortingOrder.compareClass(e0, e1);
			if (compClass != 0)
				return compClass;
			
			ResourceLocation key0 = e0.getType().getKey();
			ResourceLocation key1 = e1.getType().getKey();
			int compNamespace = key0.compareNamespaced(key1);
			if (compNamespace != 0)
				return compNamespace;
			return arbitraryCompare(e0, e1);
		}
	};
	public final String displayName;
	private final Comparator<ItemTypeAmount> baseComparator;
	public final Comparator<ItemTypeAmount> comparator;

	SortingOrder(String displayName, Comparator<ItemTypeAmount> baseComparator) {
		this.displayName = displayName;
		this.baseComparator = baseComparator;
		this.comparator = baseComparator.thenComparing(SortingOrder::arbitraryCompare);
	}
	
	private static int compareClass(Pocket.Entry e0, Pocket.Entry e1) {
		ElementType t0 = e0.getType();
		ElementType t1 = e1.getType();
		if (t0 instanceof ElementType.TItem) {
			if (t1 instanceof ElementType.TItem)
				return 0;
			return -1;
		}
		if (t1 instanceof ElementType.TItem)
			return 1;
		if (t0 instanceof ElementType.TFluid) {
			if (t1 instanceof ElementType.TFluid)
				return 0;
			return -1;
		}
		if (t1 instanceof ElementType.TFluid)
			return 1;
		String class0 = t0.getElementClassName();
		String class1 = t1.getElementClassName();
		return class0.compareTo(class1);
	}
	
	public static int arbitraryCompare(ItemTypeAmount ta0, ItemTypeAmount ta1) {
		for (SortingOrder value : values()) {
			int comp = value.baseComparator.compare(ta0, ta1);
			if (comp != 0)
				return comp;
		}
		return Integer.compare(ta0.getItemType().getTag().hashCode(), ta1.getItemType().getTag().hashCode());
	}
	
	public static int arbitraryCompare(Pocket.Entry e0, Pocket.Entry e1) {
		for (SortingOrder value : values()) {
			int comp = value.compare(e0, e1);
			if (comp != 0)
				return comp;
		}
		if (e0.getType() instanceof ElementType.TaggedType t0 && e1.getType() instanceof ElementType.TaggedType t1)
			return Integer.compare(Objects.hashCode(t0.getTag()), Objects.hashCode(t1.getTag()));
		return 0;
	}
}
