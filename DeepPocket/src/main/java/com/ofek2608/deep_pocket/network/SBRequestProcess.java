package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.RecipeRequest;
import com.ofek2608.deep_pocket.registry.pocket_screen.PocketMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

class SBRequestProcess {
	private final RecipeRequest[] requests;
	private final Map<ItemType, Optional<UUID>> setDefaultPatterns;

	SBRequestProcess(RecipeRequest[] requests, Map<ItemType, Optional<UUID>> setDefaultPatterns) {
		this.requests = requests;
		this.setDefaultPatterns = setDefaultPatterns;
	}

	SBRequestProcess(FriendlyByteBuf buf) {
		this(DeepPocketUtils.decodeArray(buf, RecipeRequest[]::new, RecipeRequest::decode), DPPacketUtils.decodeItemTypeUUIDMap(buf));
	}

	void encode(FriendlyByteBuf buf) {
		DeepPocketUtils.encodeArray(buf, requests, RecipeRequest::encode);
		DPPacketUtils.encodeItemTypeUUIDMap(buf, setDefaultPatterns);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{
			DeepPocketServerApi api = DeepPocketServerApi.get();
			ServerPlayer player = ctxSupplier.get().getSender();
			if (api == null || player == null || !(player.containerMenu instanceof PocketMenu menu))
				return;
			Pocket pocket = menu.getPocket();
			if (pocket == null)
				return;
			pocket.getDefaultPatternsMap().putAll(setDefaultPatterns);
			api.requestProcessFor(player, pocket.getPocketId(), requests);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
