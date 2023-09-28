package com.ofek2608.deep_pocket.api.enums;

import com.ofek2608.deep_pocket.integration.DeepPocketFTBTeams;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public enum PocketAccess {
	PRIVATE {
		@Override
		public boolean canWatch(UUID owner, Player player) {
			return Objects.equals(player.getUUID(), owner) || player.hasPermissions(2);
		}
	},
	TEAM {
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
	PUBLIC {
		@Override
		public boolean canWatch(UUID owner, Player player) {
			return true;
		}
	};
	
	public abstract boolean canWatch(UUID owner, Player player);
}
