package com.ofek2608.deep_pocket.api.events;

import com.ofek2608.deep_pocket.api.struct.ItemConversions;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.server.ServerLifecycleEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.Event;

public class DeepPocketBuildConversionsEvent extends ServerLifecycleEvent {
	private final ItemConversions.Builder builder;

	public DeepPocketBuildConversionsEvent(MinecraftServer server, ItemConversions.Builder builder) {
		super(server);
		this.builder = builder;
	}

	public ItemConversions.Builder getBuilder() {
		return builder;
	}

	public ItemConversions.ItemValueBuilder item(ItemType item ) { return builder.item(item); }
	public ItemConversions.ItemValueBuilder item(ItemStack item) { return builder.item(item); }
	public ItemConversions.ItemValueBuilder item(Item item     ) { return builder.item(item); }
}
