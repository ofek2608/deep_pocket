package com.ofek2608.deep_pocket.api.events;

import com.ofek2608.deep_pocket.api.struct.ElementConversions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.event.server.ServerLifecycleEvent;
import net.minecraftforge.fluids.FluidStack;

public class DeepPocketBuildConversionsEvent extends ServerLifecycleEvent {
	private final ElementConversions.Builder builder;

	public DeepPocketBuildConversionsEvent(MinecraftServer server, ElementConversions.Builder builder) {
		super(server);
		this.builder = builder;
	}

	public ElementConversions.Builder getBuilder() {
		return builder;
	}

	public ElementConversions.ElementValueBuilder item(ItemStack stack) { return builder.item(stack); }
	public ElementConversions.ElementValueBuilder item(Item item) { return builder.item(item); }
	public ElementConversions.ElementValueBuilder fluid(FluidStack stack) { return builder.fluid(stack); }
	public ElementConversions.ElementValueBuilder fluid(Fluid fluid) { return builder.fluid(fluid); }
	public ElementConversions.ElementValueBuilder energy() { return builder.energy(); }
}
