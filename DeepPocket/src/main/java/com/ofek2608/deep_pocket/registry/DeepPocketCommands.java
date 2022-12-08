package com.ofek2608.deep_pocket.registry;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.client.client_screens.ClientScreens;
import com.ofek2608.deep_pocket.integration.DeepPocketCurios;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;
import java.util.UUID;

@SuppressWarnings("SameReturnValue")
@Mod.EventBusSubscriber(modid = DeepPocketMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
class DeepPocketCommands {
	private static final boolean DEBUG_MODE = true;
	@SubscribeEvent
	public static void event(RegisterCommandsEvent event) {
		var prefix = Commands.literal("deep_pocket").requires(DeepPocketCommands::hasOp);
		prefix.then(
						Commands.literal("knowledge").then(
										Commands.literal("clear")
														.executes(DeepPocketCommands::knowledgeClear)
														.then(Commands.argument("targets", EntityArgument.players()).executes(DeepPocketCommands::knowledgeClearTargets))
						)
		);
		prefix.then(
				Commands.literal("item").then(
						Commands.literal("clear").executes(DeepPocketCommands::itemClear)
				)
		);
		if (DEBUG_MODE) {
			prefix.then(Commands.literal("test").then(Commands.argument("function", IntegerArgumentType.integer()).executes(ctx->{
				if (!(ctx.getSource().source instanceof ServerPlayer player))
					return 0;
				int function = IntegerArgumentType.getInteger(ctx, "function");
				test(player, function);
				return 0;
			})));
		}

		event.getDispatcher().register(prefix);
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
			api.getKnowledge(player.getUUID()).asSet().clear();
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
			api.getKnowledge(player.getUUID()).asSet().clear();
		send(context, "Successfully cleared the knowledge to " + players.size() + " players", true);
		return 0;
	}
	
	private static int itemClear(CommandContext<CommandSourceStack> context) {
		DeepPocketServerApi api = DeepPocketServerApi.get();
		if (context.getSource().source instanceof Player player && api != null) {
			UUID pocketId = DeepPocketCurios.getPocket(player);
			Pocket pocket = pocketId == null ? null : api.getPocket(pocketId);
			if (pocket == null) {
				send(context, "Failed to clear items, you are not holding a pocket.", true);
			} else {
				pocket.getItemsMap().clear();
				send(context, "Successfully cleared the items.", true);
			}
		} else {
			send(context, "Failed to clear items", false);
		}
		return 0;
	}

	@SuppressWarnings("all")
	private static void test(ServerPlayer player, int function) {
		if (function == 0) {
			Pocket pocket = DeepPocketServerApi.get().getPocket(DeepPocketCurios.getPocket(player));
			ItemType requiredItem = new ItemType(player.getMainHandItem());
			Minecraft minecraft = Minecraft.getInstance();
			minecraft.submit(()->{
				Screen oldScreen = minecraft.screen;
				ClientScreens.selectRecipe(pocket, requiredItem, result->{
					player.sendSystemMessage(Component.literal("selected " + result));
					minecraft.setScreen(oldScreen);
				});
			});
			return;
		}
		
		if (function == 1) {
			Pocket pocket = DeepPocketServerApi.get().getPocket(DeepPocketCurios.getPocket(player));
			Random random = new Random();
			for (var entry : ForgeRegistries.ITEMS.getEntries()) {
				pocket.insertElement(ElementType.item(entry.getValue()), random.nextInt(0xFFFFFF));
			}
			return;
		}

	}
}
