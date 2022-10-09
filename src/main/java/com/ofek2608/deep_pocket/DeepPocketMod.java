package com.ofek2608.deep_pocket;

import com.ofek2608.deep_pocket.impl.DeepPocketConfig;
import com.ofek2608.deep_pocket.integration.DeepPocketCurios;
import com.ofek2608.deep_pocket.integration.DeepPocketTheOneProbe;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

@Mod(DeepPocketMod.ID)
public class DeepPocketMod
{
	public static final String ID = "deep_pocket";
	public static ResourceLocation loc(String path) {
		return new ResourceLocation(ID, path);
	}

	public DeepPocketMod() {
		DeepPocketPacketHandler.loadClass();
		DeepPocketRegistry.loadClass();
		DeepPocketConfig.loadClass();
		DeepPocketCurios.loadClass();
		DeepPocketTheOneProbe.loadClass();
	}

	/*
	 * TODO future content
	 *  - In PocketScreen#renderPocketTooltip add a way to see the conversion
	 *  - Maybe add custom tooltip with ItemTooltipEvent
	 *  - Maybe a way to set filter to allow nbt variants
	 *  - Maybe a crafting patten
	 *  - Improve the textures of the importers, exporters and signal
	 *  - Maybe integrate with ftb chunks to prevent pockets from being used in forgotten places (or in houses of old team members)
	 *  - Add toast when the player expands his knowledge
	 *  - Make more commands
	 *  - Cleanup around pocket insertion and extractions
	 */
}
