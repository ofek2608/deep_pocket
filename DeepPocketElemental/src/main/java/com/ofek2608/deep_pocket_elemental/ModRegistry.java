package com.ofek2608.deep_pocket_elemental;

import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class ModRegistry {
	private ModRegistry() {}
	static void loadClass() {}

	private static <T> DeferredRegister<T> createRegister(IForgeRegistry<T> forge) {
		DeferredRegister<T> register = DeferredRegister.create(forge, DPEMod.ID);
		register.register(FMLJavaModLoadingContext.get().getModEventBus());
		return register;
	}

	private static final DeferredRegister<Item> ITEMS = createRegister(ForgeRegistries.ITEMS);

	private static RegistryObject<Item> createElement(String name) {
		return ITEMS.register(name, ()->new Item(new Item.Properties().tab(DeepPocketRegistry.TAB)));
	}

	public static final RegistryObject<Item> EARTH = createElement("earth");
	public static final RegistryObject<Item> WATER = createElement("water");
	public static final RegistryObject<Item> AIR = createElement("air");
	public static final RegistryObject<Item> FIRE = createElement("fire");
}
