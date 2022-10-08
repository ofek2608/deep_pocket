package com.ofek2608.deep_pocket.registry.items;

import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import com.ofek2608.deep_pocket.registry.interfaces.BlockEntityWithPocket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class PocketItem extends Item {
	public PocketItem(Properties prop) {
		super(prop);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer))
			return InteractionResultHolder.pass(stack);



		DeepPocketServerApi api = DeepPocketServerApi.get();
		if (api == null)
			return InteractionResultHolder.fail(stack);

		UUID pocketId = getPocketId(stack);
		Pocket pocket = pocketId == null ? null : api.getPocket(pocketId);
		if (pocket == null || !pocket.canAccess(player)) {
			stack = new ItemStack(DeepPocketRegistry.POCKET_LINK_ITEM.get());
			player.setItemInHand(hand, stack);
			return InteractionResultHolder.success(stack);
		}

		api.openPocket(serverPlayer, pocketId);


		return InteractionResultHolder.success(stack);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (!(level.getBlockEntity(context.getClickedPos()) instanceof BlockEntityWithPocket blockEntity)) return InteractionResult.PASS;
		if (level.isClientSide) return InteractionResult.CONSUME;
		DeepPocketServerApi api = DeepPocketServerApi.get();
		if (api == null) return InteractionResult.CONSUME;
		UUID pocketId = getPocketId(context.getItemInHand());
		Pocket pocket = pocketId == null ? null : api.getPocket(pocketId);
		Player player = context.getPlayer();
		if (pocket == null || player == null || !pocket.canAccess(player) || !blockEntity.canAccess(player)) return InteractionResult.CONSUME;
		blockEntity.setPocket(pocketId);
		return InteractionResult.CONSUME;
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		UUID pocketId = getPocketId(stack);
		if (pocketId == null)
			return super.getDescriptionId(stack);
		Pocket pocket = DeepPocketClientApi.get().getPocket(pocketId);
		if (pocket == null)
			return DeepPocketMod.ID + ".extra.invalid_pocket";
		return super.getDescriptionId(stack);
	}

	@Override
	public Component getName(ItemStack stack) {
		UUID pocketId = getPocketId(stack);
		if (pocketId == null)
			return Component.translatable(super.getDescriptionId(stack));
		Pocket pocket = DeepPocketClientApi.get().getPocket(pocketId);
		if (pocket == null)
			return Component.translatable(DeepPocketMod.ID + ".extra.invalid_pocket");
		return Component.literal(pocket.getName());
	}

	public static final String TAG_POCKET_ID = "pocket_id";
	public static ItemStack createStack(@Nullable UUID pocketId) {
		return setPocketId(new ItemStack(DeepPocketRegistry.POCKET_ITEM.get(), 1), pocketId);
	}

	public static ItemStack setPocketId(ItemStack stack, @Nullable UUID pocketId) {
		if (!stack.isEmpty() && stack.getItem() instanceof PocketItem)
			stack.setTag(setPocketId(stack.getTag(), pocketId));
		return stack;
	}

	public static @Nullable CompoundTag setPocketId(@Nullable CompoundTag tag, @Nullable UUID pocketId) {
		if (pocketId == null) {
			if (tag == null)
				return null;
			tag.remove(TAG_POCKET_ID);
			return tag;
		}
		if (tag == null)
			tag = new CompoundTag();
		tag.putUUID(TAG_POCKET_ID, pocketId);
		return tag;
	}

	public static @Nullable UUID getPocketId(ItemStack stack) {
		if (stack.isEmpty())
			return null;
		if (!(stack.getItem() instanceof PocketItem))
			return null;
		return getPocketId(stack.getTag());
	}

	public static @Nullable UUID getPocketId(@Nullable CompoundTag tag) {
		try {
			if (tag != null)
				return tag.getUUID(TAG_POCKET_ID);
		} catch (Exception ignored) {}
		return null;
	}

	public static int getTint(ItemStack stack, int tintIndex) {
		if (tintIndex != 0)
			return 0xFFFFFF;
		UUID pocketId = getPocketId(stack);
		if (pocketId == null)
			return 0xFFFFFF;
		Pocket pocket = DeepPocketClientApi.get().getPocket(pocketId);
		if (pocket == null)
			return 0xFFFFFF;
		return pocket.getColor();
	}
}
