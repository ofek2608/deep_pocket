package com.ofek2608.deep_pocket.integration;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.client.ClientTeamManager;
import dev.ftb.mods.ftbteams.api.client.KnownClientPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;

public final class DeepPocketFTBTeams {
	private DeepPocketFTBTeams() {}
	
	@SuppressWarnings("SpellCheckingInspection")
	private static final String MODID = "ftbteams";
	
	public static boolean hasMod() {
		return ModList.get().isLoaded(MODID);
	}
	
	//Only for server
	public static Collection<ServerPlayer> getOnlinePlayers(UUID teamMember) {
		{
			Optional<Collection<ServerPlayer>> onlineMembers = hasMod() ? Integrator.getOnlineMembers(teamMember) : Optional.empty();
			if (onlineMembers.isPresent())
				return onlineMembers.get();
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
	
	public static Optional<UUID> getPlayerTeamId(boolean clientSide, UUID playerId) {
		return hasMod() ? Integrator.getTeamId(clientSide, playerId) : Optional.empty();
	}
	
	
	private static final class Integrator {
		private Integrator() {}
		
		private static Optional<Team> getPlayerTeam(boolean clientSide, UUID teamMember) {
			if (clientSide) {
				ClientTeamManager manager = FTBTeamsAPI.api().getClientManager();
				Optional<KnownClientPlayer> knownClientPlayer = manager.getKnownPlayer(teamMember);
				return knownClientPlayer.flatMap(player -> manager.getTeamByID(player.teamId()));
			} else {
				return FTBTeamsAPI.api().getManager().getPlayerTeamForPlayerID(teamMember);
			}
		}
		
		private static Optional<Collection<ServerPlayer>> getOnlineMembers(UUID teamMember) {
			Optional<Team> team = FTBTeamsAPI.api().getManager().getPlayerTeamForPlayerID(teamMember);
			return team.map(Team::getOnlineMembers);
		}
		
		private static Set<UUID> getTeamMembers(boolean clientSide, UUID teamMember) {
			Optional<Team> team = getPlayerTeam(clientSide, teamMember);
			return team.map(Team::getMembers).orElseGet(() -> Set.of(teamMember));
		}
		
		private static Optional<UUID> getTeamId(boolean clientSide, UUID teamMember) {
			Optional<Team> team = getPlayerTeam(clientSide, teamMember);
			return team.map(Team::getId);
		}
	}
	
}