package com.ofek2608.deep_pocket.api.enums;

import com.ofek2608.deep_pocket.integration.DeepPocketFTBTeams;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public enum PocketAccess {
	PRIVATE(Component.literal("private").withStyle(ChatFormatting.RED)) {
		@Override
		public boolean canWatch(UUID owner, Player player) {
			return Objects.equals(player.getUUID(), owner) || player.hasPermissions(2);
		}
	},
	TEAM(Component.literal("team").withStyle(ChatFormatting.BLUE)) {
		@Override
		public boolean canWatch(UUID owner, Player player) {
			UUID playerUuid = player.getUUID();
			if (Objects.equals(playerUuid, owner) || player.hasPermissions(2)) {
				return true;
			}
			boolean isClientSide = player.level().isClientSide();
			Optional<UUID> ownerTeam = DeepPocketFTBTeams.getPlayerTeamId(isClientSide, owner);
			Optional<UUID> playerTeam = DeepPocketFTBTeams.getPlayerTeamId(isClientSide, playerUuid);
			return ownerTeam.isPresent() && ownerTeam.equals(playerTeam);
		}
	},
	PUBLIC(Component.literal("public").withStyle(ChatFormatting.GREEN)) {
		@Override
		public boolean canWatch(UUID owner, Player player) {
			return true;
		}
	};
	
	public final Component display;
	
	PocketAccess(Component display) {
		this.display = display;
	}
	
	public abstract boolean canWatch(UUID owner, Player player);
}
