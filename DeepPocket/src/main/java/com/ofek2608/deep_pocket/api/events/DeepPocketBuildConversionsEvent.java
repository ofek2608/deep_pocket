package com.ofek2608.deep_pocket.api.events;

import com.ofek2608.deep_pocket.api.struct.ElementConversionsOld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.event.server.ServerLifecycleEvent;
import net.minecraftforge.fluids.FluidStack;

public class DeepPocketBuildConversionsEvent extends ServerLifecycleEvent {
	private final ElementConversionsOld.Builder builder;

	public DeepPocketBuildConversionsEvent(MinecraftServer server, ElementConversionsOld.Builder builder) {
		super(server);
		this.builder = builder;
	}

	public ElementConversionsOld.Builder getBuilder() {
		return builder;
	}

	public ElementConversionsOld.ElementValueBuilder item(ItemStack stack) { return builder.item(stack); }
	public ElementConversionsOld.ElementValueBuilder item(Item item) { return builder.item(item); }
	public ElementConversionsOld.ElementValueBuilder fluid(FluidStack stack) { return builder.fluid(stack); }
	public ElementConversionsOld.ElementValueBuilder fluid(Fluid fluid) { return builder.fluid(fluid); }
	public ElementConversionsOld.ElementValueBuilder energy() { return builder.energy(); }
}
