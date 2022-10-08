package com.ofek2608.deep_pocket.api.enums;

import com.ofek2608.deep_pocket.integration.DeepPocketFTBTeams;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public enum PocketSecurityMode {
	PRIVATE("private", 0x990000) {
		@Override
		public boolean canAccess(Player player, UUID owner) {
			return player.getUUID().equals(owner);
		}

		private @Nullable ServerPlayer getPlayer(UUID owner) {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			return server == null ? null : server.getPlayerList().getPlayer(owner);
		}

		@Override
		public List<ServerPlayer> getViewers(UUID owner) {
			ServerPlayer player = getPlayer(owner);
			return player == null ? Collections.emptyList() : List.of(player);
		}

		@Override
		public PacketDistributor.PacketTarget getPacketTarget(UUID owner) {
			ServerPlayer player = getPlayer(owner);
			return player == null ? PacketDistributor.NMLIST.with(Collections::emptyList) : PacketDistributor.PLAYER.with(()->player);
		}
	},
	TEAM("team", 0x0000FF) {
		@Override
		public boolean canAccess(Player player, UUID owner) {
			return DeepPocketFTBTeams.areInTheSameTeam(player.level.isClientSide, player.getUUID(), owner);
		}

		@Override
		public List<ServerPlayer> getViewers(UUID owner) {
			return DeepPocketFTBTeams.getOnlinePlayers(owner);
		}

		@Override
		public PacketDistributor.PacketTarget getPacketTarget(UUID owner) {
			return DeepPocketFTBTeams.teamPacketTarget(owner);
		}
	},
	PUBLIC("public", 0x009900) {
		@Override
		public boolean canAccess(Player player, UUID owner) {
			return true;
		}

		@Override
		public List<ServerPlayer> getViewers(UUID owner) {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			return server == null ? Collections.emptyList() : server.getPlayerList().getPlayers();
		}

		@Override
		public PacketDistributor.PacketTarget getPacketTarget(UUID owner) {
			return PacketDistributor.ALL.noArg();
		}
	};

	public final String displayName;
	public final int displayColor;

	PocketSecurityMode(String displayName, int displayColor) {
		this.displayName = displayName;
		this.displayColor = displayColor;
	}

	public abstract boolean canAccess(Player player, UUID owner);
	public abstract List<ServerPlayer> getViewers(UUID owner);
	public abstract PacketDistributor.PacketTarget getPacketTarget(UUID owner);
}
