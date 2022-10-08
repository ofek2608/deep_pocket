package com.ofek2608.deep_pocket.registry;

import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.registry.interfaces.BlockEntityWithPocket;
import com.ofek2608.deep_pocket.registry.interfaces.InterfaceBER;
import com.ofek2608.deep_pocket.registry.items.PocketItem;
import com.ofek2608.deep_pocket.registry.pocket_screen.PocketScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = DeepPocketMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
class ClientSetup {
	@SubscribeEvent
	public static void event(FMLClientSetupEvent event) {
		MenuScreens.register(DeepPocketRegistry.POCKET_MENU.get(), PocketScreen::new);
	}

	@SubscribeEvent
	public static void event(RegisterColorHandlersEvent.Block event) {
		event.register(
						BlockEntityWithPocket::getTint,
						DeepPocketRegistry.PASSIVE_IMPORTER_BLOCK.get(),
						DeepPocketRegistry.PASSIVE_EXPORTER_BLOCK.get(),
						DeepPocketRegistry.ACTIVE_IMPORTER_BLOCK.get(),
						DeepPocketRegistry.ACTIVE_EXPORTER_BLOCK.get()
		);
	}

	@SubscribeEvent
	public static void event(RegisterColorHandlersEvent.Item event) {
		event.register(PocketItem::getTint, DeepPocketRegistry.POCKET_ITEM.get());
	}

	@SubscribeEvent
	public static void event(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(DeepPocketRegistry.PASSIVE_IMPORTER_ENTITY.get(), InterfaceBER::new);
		event.registerBlockEntityRenderer(DeepPocketRegistry.PASSIVE_EXPORTER_ENTITY.get(), InterfaceBER::new);
		event.registerBlockEntityRenderer(DeepPocketRegistry.ACTIVE_IMPORTER_ENTITY.get(), InterfaceBER::new);
		event.registerBlockEntityRenderer(DeepPocketRegistry.ACTIVE_EXPORTER_ENTITY.get(), InterfaceBER::new);
	}
}
