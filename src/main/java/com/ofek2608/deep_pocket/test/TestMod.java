package com.ofek2608.deep_pocket.test;

import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.events.DeepPocketBuildConversionsEvent;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TestMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TestMod {
	public static final String ID = DeepPocketMod.ID;

	@SubscribeEvent
	public static void event(DeepPocketBuildConversionsEvent event) {
		event.item(Items.OAK_PLANKS).add(Items.STONE, 16);
		event.item(Items.OAK_LOG).add(Items.STONE, 64);
		event.item(Items.OAK_SLAB).add(Items.STONE, 8);
		event.item(Items.STICK).add(Items.STONE, 8);
		event.item(Items.DIAMOND).add(Items.STONE, 64).add(Items.COAL, 64);
		event.item(Items.OBSIDIAN).add(Items.DIAMOND, 1).add(Items.STICK, 2);
		event.item(Items.GOLD_NUGGET);
	}
}
