package com.ofek2608.deep_pocket.registry.interfaces;

import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.server.ServerPocket;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class ActiveExporter extends Block implements EntityBlock {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public ActiveExporter(Properties properties) {
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

	@SuppressWarnings("deprecation")
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		return BlockEntityWithPocketFilter.setFilter(level, pos, player, hand);
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

	public static class Ent extends BlockEntityWithPocketFilter {

		public Ent(BlockEntityType<?> type, BlockPos pos, BlockState state) {
			super(type, pos, state);
		}

		public Ent(BlockPos pos, BlockState state) {
			super(DeepPocketRegistry.ACTIVE_EXPORTER_ENTITY.get(), pos, state);
		}

		public void tick(Level level, BlockPos pos, BlockState state) {
			Direction facing = state.getValue(FACING);
			
			BlockEntity targetEntity = level.getBlockEntity(pos.relative(facing));
			if (targetEntity == null)
				return;
			
			DeepPocketServerApi api = DeepPocketServerApi.get();
			ServerPocket pocket = getServerPocket();
			if (api == null || pocket == null)
				return;
			
			ElementType filter = getFilter();
			int filterId = api.getElementIndices().getIndex(filter);
			
			if (filter instanceof ElementType.TItem filterItem) {
				exportItem(pocket, filterItem, filterId, targetEntity, facing);
			}
		}
		
		private static void exportItem(ServerPocket pocket, ElementType.TItem filter, int filterId, BlockEntity targetEntity, Direction facing) {
			IItemHandler itemHandler = targetEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, facing.getOpposite()).resolve().orElse(null);
			if (itemHandler == null)
				return;
			
			int slotCount = itemHandler.getSlots();
			for (int i = 0; i < slotCount; i++) {
				int transferCount = itemHandler.getSlotLimit(i);
				if (transferCount <= 0)
					continue;
				transferCount -= itemHandler.insertItem(i, filter.create(transferCount), true).getCount();
				if (transferCount <= 0)
					continue;
				transferCount = (int)pocket.extractElement(filterId, transferCount);
				if (transferCount <= 0)
					return;
				int leftOver = itemHandler.insertItem(i, filter.create(transferCount), false).getCount();
				if (leftOver <= 0)
					continue;
				pocket.insertElement(filterId, leftOver);
			}
		}
	}
}
