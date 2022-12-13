package com.ofek2608.deep_pocket.registry.interfaces;

import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class ActiveImporter extends Block implements EntityBlock {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public ActiveImporter(Properties properties) {
		super(properties);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return defaultBlockState().setValue(FACING, ctx.getClickedFace().getOpposite());
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new Ent(pos, state);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		BlockEntityWithPocket.onPlace(level, pos, placer);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
		return (level, pos, state, blockEntity) -> {
			if (blockEntity instanceof Ent ent)
					ent.tick(level, pos, state);
		};
	}

	public static class Ent extends BlockEntityWithPocket {

		public Ent(BlockEntityType<?> type, BlockPos pos, BlockState state) {
			super(type, pos, state);
		}

		public Ent(BlockPos pos, BlockState state) {
			super(DeepPocketRegistry.ACTIVE_IMPORTER_ENTITY.get(), pos, state);
		}

		public void tick(Level level, BlockPos pos, BlockState state) {
			Direction facing = state.getValue(FACING);
			BlockEntity targetEntity = level.getBlockEntity(pos.relative(facing));
			if (targetEntity == null) return;
			IItemHandler itemHandler = targetEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, facing.getOpposite()).resolve().orElse(null);
			if (itemHandler == null) return;
			Pocket pocket = getServerPocket();
			if (pocket == null) return;

			int slots = itemHandler.getSlots();
			for (int slot = 0; slot < slots; slot++) {
				ItemStack stack = itemHandler.extractItem(slot, itemHandler.getSlotLimit(slot), false);
				pocket.insertElement(ElementType.item(stack), stack.getCount());
			}
		}
	}
}
