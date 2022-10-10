package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.api.struct.SignalSettings;
import com.ofek2608.deep_pocket.registry.interfaces.SignalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

class SBPocketSignalSettings {
	private final BlockPos pos;
	private final SignalSettings settings;

	SBPocketSignalSettings(BlockPos pos, SignalSettings settings) {
		this.pos = pos;
		this.settings = settings;
	}

	SBPocketSignalSettings(FriendlyByteBuf buf) {
		this(buf.readBlockPos(), SignalSettings.decode(buf));
	}

	void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(pos);
		SignalSettings.encode(buf, settings);
	}

	void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
		ctxSupplier.get().enqueueWork(()->{

			ServerPlayer player = ctxSupplier.get().getSender();
			if (player == null)
				return;
			if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64.0)
				return;
			if (!(player.level.getBlockEntity(pos) instanceof SignalBlock.Ent entity))
				return;
			if (!entity.canAccess(player))
				return;
			entity.setSettings(settings);
		});
		ctxSupplier.get().setPacketHandled(true);
	}
}
