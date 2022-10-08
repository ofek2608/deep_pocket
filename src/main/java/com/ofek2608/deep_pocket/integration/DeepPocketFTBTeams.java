package com.ofek2608.deep_pocket.integration;

import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.struct.Pocket;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.data.ClientTeamManager;
import dev.ftb.mods.ftbteams.data.KnownClientPlayer;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamBase;
import dev.ftb.mods.ftbteams.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.event.TeamEvent;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public final class DeepPocketFTBTeams {
	private DeepPocketFTBTeams() {}
	@SuppressWarnings("EmptyMethod")
	public static void loadClass() {}
	@SuppressWarnings("SpellCheckingInspection")
	private static final String MODID = "ftbteams";

	public static boolean hasMod() {
		return ModList.get().isLoaded(MODID);
	}

	static {
		if (hasMod())
			Integrator.init();
	}

	//Only for server
	public static List<ServerPlayer> getOnlinePlayers(UUID teamMember) {
		{
			List<ServerPlayer> onlineMembers = hasMod() ? Integrator.getOnlineMembers(teamMember) : null;
			if (onlineMembers != null)
				return onlineMembers;
		}
		//creating a list only with this player
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null)
			return Collections.emptyList();
		ServerPlayer player = server.getPlayerList().getPlayer(teamMember);
		if (player == null)
			return Collections.emptyList();
		return List.of(player);
	}

	//Only for server
	public static PacketDistributor.PacketTarget teamPacketTarget(UUID teamMember) {
		return PacketDistributor.NMLIST.with(()-> DeepPocketFTBTeams.getOnlinePlayers(teamMember).stream().map(player->player.connection.connection).toList());
	}

	public static boolean areInTheSameTeam(boolean clientSide, UUID teamMember1, UUID teamMember2) {
		return getTeamMembers(clientSide, teamMember1).contains(teamMember2);
	}

	public static Set<UUID> getTeamMembers(boolean clientSide, UUID teamMember) {
		return hasMod() ? Integrator.getTeamMembers(clientSide, teamMember) : Set.of(teamMember);
	}


	private static final class Integrator {
		private Integrator() {}

		private static void init() {
			TeamEvent.PLAYER_CHANGED.register(Integrator::event);
		}

		private static @Nullable TeamBase getPlayerTeam(boolean clientSide, UUID teamMember) {
			if (clientSide) {
				ClientTeamManager manager = FTBTeamsAPI.getClientManager();
				KnownClientPlayer knownClientPlayer = manager.getKnownPlayer(teamMember);
				if (knownClientPlayer == null)
					return null;
				return manager.getTeam(knownClientPlayer.teamId);
			} else {
				return FTBTeamsAPI.getManager().getPlayerTeam(teamMember);
			}
		}

		private static @Nullable List<ServerPlayer> getOnlineMembers(UUID teamMember) {
			Team team = FTBTeamsAPI.getPlayerTeam(teamMember);
			return team == null ? null : team.getOnlineMembers();
		}

		private static Set<UUID> getTeamMembers(boolean clientSide, UUID teamMember) {
			TeamBase team = getPlayerTeam(clientSide, teamMember);
			return team == null ? Set.of(teamMember) : team.getMembers();
		}

		private static void event(PlayerChangedTeamEvent event) {
			ServerPlayer player = event.getPlayer();
			UUID playerId = event.getPlayerId();

			DeepPocketServerApi api = DeepPocketServerApi.get();
			if (api == null)
				return;

			Team newTeam = event.getTeam();
			Team oldTeam = event.getPreviousTeam().orElse(null);
			UUID otherTeamMember = newTeam.getMembers().stream().filter(member -> !member.equals(playerId)).findAny().orElse(null);

			syncTeamItems(api, player, playerId, newTeam, (target, pocket) -> DeepPocketPacketHandler.cbPocketSetItemCount(target, pocket.getPocketId(), pocket.getItems()));
			if (oldTeam != null)
				syncTeamItems(api, player, playerId, oldTeam, (target, pocket) -> DeepPocketPacketHandler.cbPocketClearItems(target, pocket.getPocketId()));
			if (otherTeamMember != null)
				api.getKnowledge(playerId).add(api.getKnowledge(otherTeamMember).asSet().toArray(new ItemType[0]));
		}

		private static void syncTeamItems(DeepPocketServerApi api, @Nullable ServerPlayer player, UUID playerId, Team team, BiConsumer<PacketDistributor.PacketTarget, Pocket> syncSender) {
			List<Connection> teamConnections = team.getOnlineMembers()
							.stream()
							.filter(member->member != player)
							.map(member -> member.connection.connection)
							.toList();
			PacketDistributor.PacketTarget teamTarget = PacketDistributor.NMLIST.with(()->teamConnections);
			getPocketsForPlayer(api, playerId).forEach(pocket -> syncSender.accept(teamTarget, pocket));
			if (player == null)
				return;
			PacketDistributor.PacketTarget playerTarget = PacketDistributor.PLAYER.with(()->player);
			team.getMembers()
							.stream()
							.filter(member -> !member.equals(playerId))
							.flatMap(member -> getPocketsForPlayer(api, member))
							.forEach(pocket->syncSender.accept(playerTarget, pocket));
		}

		private static Stream<Pocket> getPocketsForPlayer(DeepPocketServerApi api, UUID player) {
			return api.getPockets().filter(pocket -> pocket.getOwner().equals(player));
		}
	}

}
