package com.ofek2608.deep_pocket.registry.interfaces;

import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PassiveImporter extends Block implements EntityBlock {
	public PassiveImporter(Properties properties) {
		super(properties);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new Ent(pos, state);
	}



	public static class Ent extends BlockEntityWithPocket {
		private final LazyOptional<IItemHandler> CAPABILITY = LazyOptional.of(()->new IItemHandler() {
			@Override
			public int getSlots() {
				return 1;
			}

			@Override
			public @NotNull ItemStack getStackInSlot(int slot) {
				return ItemStack.EMPTY;
			}

			@Override
			public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
				DeepPocketServerApi api = DeepPocketServerApi.get();
				Pocket pocket = getServerPocket();
				if (api == null || pocket == null)
					return stack;
				if (!simulate)
					api.insertItem(pocket, stack);
				return ItemStack.EMPTY;
			}

			@Override
			public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
				return ItemStack.EMPTY;
			}

			@Override
			public int getSlotLimit(int slot) {
				return Integer.MAX_VALUE;
			}

			@Override
			public boolean isItemValid(int slot, @NotNull ItemStack stack) {
				return slot == 0;
			}
		});

		public Ent(BlockEntityType<?> type, BlockPos pos, BlockState state) {
			super(type, pos, state);
		}

		public Ent(BlockPos pos, BlockState state) {
			super(DeepPocketRegistry.PASSIVE_IMPORTER_ENTITY.get(), pos, state);
		}

		@Override
		public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
			if (cap == ForgeCapabilities.ITEM_HANDLER)
				return CAPABILITY.cast();
			return super.getCapability(cap, side);
		}
	}
}
