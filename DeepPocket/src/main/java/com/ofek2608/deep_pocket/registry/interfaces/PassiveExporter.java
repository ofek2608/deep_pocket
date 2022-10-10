package com.ofek2608.deep_pocket.registry.interfaces;

import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.struct.Pocket;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PassiveExporter extends Block implements EntityBlock {
	public PassiveExporter(Properties properties) {
		super(properties);
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



	public static class Ent extends BlockEntityWithPocketFilter {
		private final LazyOptional<IItemHandler> CAPABILITY = LazyOptional.of(()->new IItemHandler() {
			@Override
			public int getSlots() {
				return 1;
			}

			@Override
			public @NotNull ItemStack getStackInSlot(int slot) {
				if (slot != 0) return ItemStack.EMPTY;
				DeepPocketServerApi api = DeepPocketServerApi.get();
				Pocket pocket = getServerPocket();
				if (api == null || pocket == null) return ItemStack.EMPTY;
				long maxExtract = pocket.getMaxExtract(null, getFilter());
				return getFilter().create(maxExtract < 0 || Integer.MAX_VALUE <= maxExtract ? Integer.MAX_VALUE : (int)maxExtract);
			}

			@Override
			public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
				return stack;
			}

			@Override
			public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
				if (slot != 0 || amount <= 0) return ItemStack.EMPTY;
				DeepPocketServerApi api = DeepPocketServerApi.get();
				Pocket pocket = getServerPocket();
				if (api == null || pocket == null) return ItemStack.EMPTY;
				ItemType filter = getFilter();
				if (filter.isEmpty()) return ItemStack.EMPTY;

				if (!simulate)
					return filter.create((int)pocket.extractItem(null, filter, amount));
				long maxExtract = pocket.getMaxExtract(null, filter);
				return filter.create(maxExtract < 0 || amount <= maxExtract ? amount : (int)maxExtract);
			}

			@Override
			public int getSlotLimit(int slot) {
				return Integer.MAX_VALUE;
			}

			@Override
			public boolean isItemValid(int slot, @NotNull ItemStack stack) {
				return slot == 0 && new ItemType(stack).equals(getFilter());
			}
		});

		public Ent(BlockEntityType<?> type, BlockPos pos, BlockState state) {
			super(type, pos, state);
		}

		public Ent(BlockPos pos, BlockState state) {
			super(DeepPocketRegistry.PASSIVE_EXPORTER_ENTITY.get(), pos, state);
		}

		@Override
		public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
			if (cap == ForgeCapabilities.ITEM_HANDLER)
				return CAPABILITY.cast();
			return super.getCapability(cap, side);
		}
	}
}
