package com.ofek2608.deep_pocket.integration;

import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import com.ofek2608.deep_pocket.registry.items.PocketItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.SlotTypeMessage;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public final class DeepPocketCurios {
	private DeepPocketCurios() {}
	@SuppressWarnings("EmptyMethod") public static void loadClass() {}

	private static final String MODID = "curios";
	private static final ResourceLocation CURIO_POCKET_ICON = DeepPocketMod.loc("item/empty_pocket");

	public static boolean hasMod() {
		return ModList.get().isLoaded(MODID);
	}

	static {
		if (hasMod())
			Integrator.init();
	}

	public static @Nullable UUID getPocket(Player player) {
		UUID pocket;
		pocket = PocketItem.getPocketId(player.getMainHandItem());
		if (pocket != null)
			return pocket;
		pocket = PocketItem.getPocketId(player.getOffhandItem());
		if (pocket != null)
			return pocket;
		if (hasMod())
			pocket = Integrator.getPocket(player);
		return pocket;
	}

	private static final class Integrator {
		private Integrator() {}

		private static void init() {
			InterModComms.sendTo(MODID, SlotTypeMessage.REGISTER_TYPE, ()->new SlotTypeMessage.Builder(DeepPocketMod.ID + "_pocket")
							.icon(CURIO_POCKET_ICON)
							.size(1)
							.build()
			);
		}

		private static @Nullable UUID getPocket(Player player) {
			return CuriosApi.getCuriosHelper()
							.findCurios(player, DeepPocketRegistry.POCKET_ITEM.get())
							.stream()
							.map(SlotResult::stack)
							.map(PocketItem::getPocketId)
							.filter(Objects::nonNull)
							.findAny()
							.orElse(null);
		}
	}

	@Mod.EventBusSubscriber(modid = DeepPocketMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
	private static final class ModEvents {
		private ModEvents() {}

		@SubscribeEvent
		public static void event(TextureStitchEvent.Pre event) {
			event.addSprite(CURIO_POCKET_ICON);
		}
	}
}
