package com.ofek2608.deep_pocket.def.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.ofek2608.deep_pocket.DeepPocketMod;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;

public final class KeyBinds {
	private KeyBinds() {}
	public static void loadClass() {}
	
	private static KeyMapping add(String name, IKeyConflictContext conflict, int def) {
		return new KeyMapping(
				"key." + DeepPocketMod.ID + "." + name,
				conflict,
				InputConstants.getKey(def, 0),
				"key.categories." + DeepPocketMod.ID
		);
	}
	
	public static final KeyMapping KEY_OPEN_POCKET = add("open pocket", KeyConflictContext.IN_GAME, InputConstants.KEY_R);
}
