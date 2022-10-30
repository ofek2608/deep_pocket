package com.ofek2608.deep_pocket.registry;

import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.registry.interfaces.*;
import com.ofek2608.deep_pocket.registry.interfaces.crafter.CrafterBlock;
import com.ofek2608.deep_pocket.registry.interfaces.crafter.CrafterMenu;
import com.ofek2608.deep_pocket.registry.items.crafting_pattern.CraftingPatternItem;
import com.ofek2608.deep_pocket.registry.items.PocketFactoryItem;
import com.ofek2608.deep_pocket.registry.items.PocketItem;
import com.ofek2608.deep_pocket.registry.items.PocketLinkItem;
import com.ofek2608.deep_pocket.registry.pocket_screen.PocketMenu;
import com.ofek2608.deep_pocket.registry.process_screen.ProcessMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public final class DeepPocketRegistry {
	private DeepPocketRegistry() {}
	@SuppressWarnings("EmptyMethod") public static void loadClass() {}

	private static <T> DeferredRegister<T> createRegister(IForgeRegistry<T> forge) {
		DeferredRegister<T> register = DeferredRegister.create(forge, DeepPocketMod.ID);
		register.register(FMLJavaModLoadingContext.get().getModEventBus());
		return register;
	}

	private static final DeferredRegister<Block> BLOCKS = createRegister(ForgeRegistries.BLOCKS);
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = createRegister(ForgeRegistries.BLOCK_ENTITY_TYPES);
	private static final DeferredRegister<Item> ITEMS = createRegister(ForgeRegistries.ITEMS);
	private static final DeferredRegister<MenuType<?>> MENU_TYPES = createRegister(ForgeRegistries.MENU_TYPES);

	private static ItemStack createTabIcon() { return new ItemStack(POCKET_ITEM.get()); }
	public static final CreativeModeTab TAB = new CreativeModeTab(DeepPocketMod.ID) {public ItemStack makeIcon(){return createTabIcon();}};

	private static BlockBehaviour.Properties blockProperties() {
		return BlockBehaviour.Properties.copy(Blocks.STONE);
	}

	private static Item.Properties itemProperties(int stack) {
		return new Item.Properties().tab(TAB).stacksTo(stack);
	}

	private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> registerBlockEntity(RegistryObject<? extends Block> block, BlockEntityType.BlockEntitySupplier<T> factory) {
		//noinspection ConstantConditions
		return BLOCK_ENTITY_TYPES.register(block.getId().getPath(), ()->new BlockEntityType<>(factory, Set.of(block.get()), null));
	}

	private static RegistryObject<BlockItem> registerBlockItem(RegistryObject<? extends Block> block) {
		return ITEMS.register(block.getId().getPath(), ()->new BlockItem(block.get(), itemProperties(64)));
	}

	public static final RegistryObject<PocketLinkItem> POCKET_LINK_ITEM = ITEMS.register("pocket_link", ()->new PocketLinkItem(itemProperties(64)));
	public static final RegistryObject<PocketItem> POCKET_ITEM = ITEMS.register("pocket", ()->new PocketItem(itemProperties(1)));
	public static final RegistryObject<PocketFactoryItem> POCKET_FACTORY_ITEM = ITEMS.register("pocket_factory", ()->new PocketFactoryItem(itemProperties(1)));
	public static final RegistryObject<Item> EMPTY_CRAFTING_PATTERN_ITEM = ITEMS.register("empty_crafting_pattern", ()->new Item(itemProperties(64)));
	public static final RegistryObject<CraftingPatternItem> CRAFTING_PATTERN_ITEM = ITEMS.register("crafting_pattern", ()->new CraftingPatternItem(itemProperties(1)));

	public static final RegistryObject<MenuType<PocketMenu>> POCKET_MENU = MENU_TYPES.register("pocket", ()->new MenuType<>(PocketMenu::new));
	public static final RegistryObject<MenuType<ProcessMenu>> PROCESS_MENU = MENU_TYPES.register("process", ()->new MenuType<>(ProcessMenu::new));
	public static final RegistryObject<MenuType<CrafterMenu>> CRAFTER_MENU = MENU_TYPES.register("crafter", ()->new MenuType<>(CrafterMenu::new));

	public static final RegistryObject<PassiveImporter> PASSIVE_IMPORTER_BLOCK = BLOCKS.register("passive_importer", ()->new PassiveImporter(blockProperties()));
	public static final RegistryObject<PassiveExporter> PASSIVE_EXPORTER_BLOCK = BLOCKS.register("passive_exporter", ()->new PassiveExporter(blockProperties()));
	public static final RegistryObject<ActiveImporter> ACTIVE_IMPORTER_BLOCK = BLOCKS.register("active_importer", ()->new ActiveImporter(blockProperties()));
	public static final RegistryObject<ActiveExporter> ACTIVE_EXPORTER_BLOCK = BLOCKS.register("active_exporter", ()->new ActiveExporter(blockProperties()));
	public static final RegistryObject<SignalBlock> SIGNAL_BLOCK = BLOCKS.register("signal", ()->new SignalBlock(blockProperties()));
	public static final RegistryObject<CrafterBlock> CRAFTER_BLOCK = BLOCKS.register("crafter", ()->new CrafterBlock(blockProperties()));
	public static final RegistryObject<BlockEntityType<PassiveImporter.Ent>> PASSIVE_IMPORTER_ENTITY = registerBlockEntity(PASSIVE_IMPORTER_BLOCK, PassiveImporter.Ent::new);
	public static final RegistryObject<BlockEntityType<PassiveExporter.Ent>> PASSIVE_EXPORTER_ENTITY = registerBlockEntity(PASSIVE_EXPORTER_BLOCK, PassiveExporter.Ent::new);
	public static final RegistryObject<BlockEntityType<ActiveImporter.Ent>> ACTIVE_IMPORTER_ENTITY = registerBlockEntity(ACTIVE_IMPORTER_BLOCK, ActiveImporter.Ent::new);
	public static final RegistryObject<BlockEntityType<ActiveExporter.Ent>> ACTIVE_EXPORTER_ENTITY = registerBlockEntity(ACTIVE_EXPORTER_BLOCK, ActiveExporter.Ent::new);
	public static final RegistryObject<BlockEntityType<SignalBlock.Ent>> SIGNAL_ENTITY = registerBlockEntity(SIGNAL_BLOCK, SignalBlock.Ent::new);
	public static final RegistryObject<BlockEntityType<CrafterBlock.Ent>> CRAFTER_ENTITY = registerBlockEntity(CRAFTER_BLOCK, CrafterBlock.Ent::new);
	public static final RegistryObject<BlockItem> PASSIVE_IMPORTER_ITEM = registerBlockItem(PASSIVE_IMPORTER_BLOCK);
	public static final RegistryObject<BlockItem> PASSIVE_EXPORTER_ITEM = registerBlockItem(PASSIVE_EXPORTER_BLOCK);
	public static final RegistryObject<BlockItem> ACTIVE_IMPORTER_ITEM = registerBlockItem(ACTIVE_IMPORTER_BLOCK);
	public static final RegistryObject<BlockItem> ACTIVE_EXPORTER_ITEM = registerBlockItem(ACTIVE_EXPORTER_BLOCK);
	public static final RegistryObject<BlockItem> SIGNAL_ITEM = registerBlockItem(SIGNAL_BLOCK);
	public static final RegistryObject<BlockItem> CRAFTER_ITEM = registerBlockItem(CRAFTER_BLOCK);
}
