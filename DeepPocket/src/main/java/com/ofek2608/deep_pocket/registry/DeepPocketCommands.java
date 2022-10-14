package com.ofek2608.deep_pocket.registry;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("SameReturnValue")
@Mod.EventBusSubscriber(modid = DeepPocketMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
class DeepPocketCommands {
	@SubscribeEvent
	public static void event(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("deep_pocket").then(
						Commands.literal("knowledge").then(
										Commands.literal("clear").requires(DeepPocketCommands::hasOp)
														.executes(DeepPocketCommands::knowledgeClear)
														.then(Commands.argument("targets", EntityArgument.players()).executes(DeepPocketCommands::knowledgeClearTargets))
						)
		));
	}

	private static boolean hasOp(CommandSourceStack sender) {
		return sender.hasPermission(2);
	}

	private static void send(CommandContext<CommandSourceStack> context, String msg, boolean success) {
		context.getSource().source.sendSystemMessage(Component.literal(msg).withStyle(success ? ChatFormatting.GREEN : ChatFormatting.RED));
	}

	private static int knowledgeClear(CommandContext<CommandSourceStack> context) {
		DeepPocketServerApi api = DeepPocketServerApi.get();
		if (context.getSource().source instanceof Player player && api != null) {
			api.getKnowledge(player.getUUID()).clear();
			send(context, "Successfully cleared the knowledge to " + player.getName().getString(), true);
		} else {
			send(context, "Failed to clear knowledge", false);
		}
		return 0;
	}

	private static int knowledgeClearTargets(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		DeepPocketServerApi api = DeepPocketServerApi.get();
		if (api == null) {
			send(context, "Failed to clear knowledge", false);
			return 0;
		}
		var players = EntityArgument.getPlayers(context, "targets");
		for (ServerPlayer player : players)
			api.getKnowledge(player.getUUID()).clear();
		send(context, "Successfully cleared the knowledge to " + players.size() + " players", true);
		return 0;
	}
}
