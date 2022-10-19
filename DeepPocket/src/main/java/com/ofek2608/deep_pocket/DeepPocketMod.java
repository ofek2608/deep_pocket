package com.ofek2608.deep_pocket;

import com.ofek2608.deep_pocket.impl.DeepPocketConfig;
import com.ofek2608.deep_pocket.integration.DeepPocketCurios;
import com.ofek2608.deep_pocket.integration.DeepPocketTheOneProbe;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

@Mod(DeepPocketMod.ID)
public final class DeepPocketMod {
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
	 * TODO before version 0.0.5
	 *  - Implement CrafterBlock.ent#executePattern
	 *  - Add processes class:
	 *    > Pockets should have processes, inserted items should get inserted to processes
	 *    > There should be a window to show the processes.
	 *  - Add a way to create a process: in the pocket screen
	 *      when you press on an item while holding ctrl you can craft the item using ClientScreens#selectNumber
	 *      and then opening a gui to show the required crafting, and warning if there are any. with a confirm/cancel options.
	 *      if confirmed, sent to the server and he adds it as a process.
	 * TODO before version 1.0.0
	 *  - test multiplayer
	 *  - Make more commands
	 *  - Add toast when the player expands his knowledge
	 * TODO future content
	 *  - Maybe a way to set filter to allow nbt variants (for exporters)
	 *  - Improve the textures of the blocks
	 *  - Maybe integrate with ftb chunks to prevent pockets from being used in forgotten places (or in houses of old team members)
	 *  - Cleanup around pocket insertion and extractions
	 *  - A way to extract infinite amount of an item (mini pockets)
	 *  - Maybe fluids inside pocket
	 */
}
