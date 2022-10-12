package com.ofek2608.deep_pocket_conversions;

import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class ModRegistry {
	private ModRegistry() {}
	@SuppressWarnings("EmptyMethod")
	static void loadClass() {}

	@SuppressWarnings("SameParameterValue")
	private static <T> DeferredRegister<T> createRegister(IForgeRegistry<T> forge) {
		DeferredRegister<T> register = DeferredRegister.create(forge, DPCMod.ID);
		register.register(FMLJavaModLoadingContext.get().getModEventBus());
		return register;
	}

	private static final DeferredRegister<Item> ITEMS = createRegister(ForgeRegistries.ITEMS);
	private static final List<RegistryObject<MatterItem>> MATTER = new ArrayList<>();
	static {
		for (int num = MatterItem.MIN_MATTER_NUM; num <= MatterItem.MAX_MATTER_NUM; num++) {
			int finalNum = num;
			MATTER.add(ITEMS.register("matter_" + num, () -> new MatterItem(new Item.Properties().tab(DeepPocketRegistry.TAB), finalNum)));
		}
	}

	public static MatterItem getMatter(int num) { return MATTER.get(num - MatterItem.MIN_MATTER_NUM).get(); }
	public static MatterItem getMinMatter() { return getMatter(MatterItem.MIN_MATTER_NUM); }
	public static MatterItem getMaxMatter() { return getMatter(MatterItem.MAX_MATTER_NUM); }
}
