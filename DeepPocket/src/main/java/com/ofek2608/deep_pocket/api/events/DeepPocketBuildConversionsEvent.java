package com.ofek2608.deep_pocket.api.events;

import com.ofek2608.deep_pocket.api.struct.ElementConversions;
import com.ofek2608.deep_pocket.api.struct.ElementConversionsOld;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.server.ServerElementIndices;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.event.server.ServerLifecycleEvent;
import net.minecraftforge.fluids.FluidStack;

public class DeepPocketBuildConversionsEvent extends ServerLifecycleEvent {
	private final ServerElementIndices elementIndices;
	private final ElementConversions.Builder builder;

	public DeepPocketBuildConversionsEvent(
			MinecraftServer server, ServerElementIndices elementIndices, ElementConversions.Builder builder
	) {
		super(server);
		this.elementIndices = elementIndices;
		this.builder = builder;
	}
	
	public ServerElementIndices getElementIndices() {
		return elementIndices;
	}
	
	public ElementConversions.Builder getBuilder() {
		return builder;
	}

	public ElementConversions.ElementValueBuilder element(ElementType type) { return builder.element(elementIndices.getIndexOrCreate(type)); }
	
	public ElementConversions.ElementValueBuilder item(ItemStack stack) { return element(ElementType.item(stack)); }
	public ElementConversions.ElementValueBuilder item(Item item) { return element(ElementType.item(item)); }
	public ElementConversions.ElementValueBuilder fluid(FluidStack stack) { return element(ElementType.fluid(stack)); }
	public ElementConversions.ElementValueBuilder fluid(Fluid fluid) { return element(ElementType.fluid(fluid)); }
	public ElementConversions.ElementValueBuilder energy() { return element(ElementType.energy()); }
}
