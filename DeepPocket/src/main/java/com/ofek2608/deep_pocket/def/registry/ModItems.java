package com.ofek2608.deep_pocket.def.registry;

import com.ofek2608.deep_pocket.DeepPocketMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
	private ModItems() {}
	
	public static final DeferredRegister<Item> REG = DeferredRegister.create(ForgeRegistries.ITEMS, DeepPocketMod.ID);
	
	public static final RegistryObject<Item> POCKET_FACTORY = REG.register("pocket_factory", () -> new Item(new Item.Properties().stacksTo(1)));
}
