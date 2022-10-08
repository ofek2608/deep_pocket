package com.ofek2608.deep_pocket.registry.interfaces;

import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class BlockEntityWithPocketFilter extends BlockEntityWithPocket {
	private @Nonnull ItemType filter = ItemType.EMPTY;

	public BlockEntityWithPocketFilter(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public ItemType getFilter() {
		return filter;
	}

	public void setFilter(ItemType filter) {
		this.filter = filter;
		setChanged();
		sendBlockUpdate();
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		if (tag.contains("filter"))
			filter = ItemType.load(tag.getCompound("filter"));
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.put("filter", filter.save());
	}

	public static InteractionResult setFilter(Level level, BlockPos pos, Player player, InteractionHand hand) {
		if (!(level.getBlockEntity(pos) instanceof BlockEntityWithPocketFilter blockEntity)) return InteractionResult.PASS;
		ItemStack handItem = player.getItemInHand(hand);
		if (handItem.isEmpty() || handItem.is(DeepPocketRegistry.POCKET_ITEM.get())) return InteractionResult.PASS;
		if (level.isClientSide) return InteractionResult.CONSUME;
		if (!blockEntity.canAccess(player)) return InteractionResult.CONSUME;
		blockEntity.setFilter(new ItemType(handItem));
		return InteractionResult.CONSUME;
	}
}
