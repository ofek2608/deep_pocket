package com.ofek2608.deep_pocket.registry.interfaces;

import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.ItemType;
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
	private @Nonnull ElementType filter = ElementType.empty();

	public BlockEntityWithPocketFilter(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public ElementType getFilter() {
		return filter;
	}

	public void setFilter(ElementType filter) {
		this.filter = filter;
		setChanged();
		sendBlockUpdate();
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		if (tag.contains("filter"))
			filter = ElementType.load(tag.getCompound("filter"));
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.put("filter", ElementType.save(filter));
	}

	public static InteractionResult setFilter(Level level, BlockPos pos, Player player, InteractionHand hand) {
		if (!(level.getBlockEntity(pos) instanceof BlockEntityWithPocketFilter blockEntity)) return InteractionResult.PASS;
		ItemStack handItem = player.getItemInHand(hand);
		if (handItem.isEmpty() || handItem.is(DeepPocketRegistry.POCKET_ITEM.get())) return InteractionResult.PASS;
		if (level.isClientSide) return InteractionResult.CONSUME;
		if (!blockEntity.canAccess(player)) return InteractionResult.CONSUME;
		blockEntity.setFilter(ElementType.item(handItem));
		return InteractionResult.CONSUME;
	}
}
