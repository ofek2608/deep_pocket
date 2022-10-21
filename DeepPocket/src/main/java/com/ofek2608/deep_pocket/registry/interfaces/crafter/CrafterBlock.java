package com.ofek2608.deep_pocket.registry.interfaces.crafter;

import com.ofek2608.deep_pocket.api.PatternSupportedBlockEntity;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.ProvidedResources;
import com.ofek2608.deep_pocket.api.struct.WorldCraftingPattern;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import com.ofek2608.deep_pocket.registry.interfaces.BlockEntityWithPocket;
import com.ofek2608.deep_pocket.registry.items.crafting_pattern.CraftingPatternItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;

public class CrafterBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public CrafterBlock(Properties properties) {
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
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand pHand, BlockHitResult pHit) {
		if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer))
			return InteractionResult.SUCCESS;
		if (!(level.getBlockEntity(pos) instanceof Ent ent))
			return InteractionResult.CONSUME;
		Pocket pocket = ent.getServerPocket();
		MenuProvider menuprovider = new SimpleMenuProvider(
						(id,inv,p)->{
							CrafterMenu menu = new CrafterMenu(id, inv, ent.itemHandler, pos);
							menu.setPocket(pocket);
							return menu;
						},
						Component.literal("Crafter")
		);
		player.openMenu(menuprovider);
		if (pocket != null)
			DeepPocketPacketHandler.cbSetViewedPocket(PacketDistributor.PLAYER.with(()->serverPlayer), pocket.getPocketId());

		return InteractionResult.CONSUME;
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

	public static class Ent extends BlockEntityWithPocket implements Container, PatternSupportedBlockEntity {
		protected NonNullList<ItemStack> items;
		protected final UUID[] patterns;

		public Ent(BlockEntityType<?> type, BlockPos pos, BlockState state, int size) {
			super(type, pos, state);
			this.items = NonNullList.withSize(size, ItemStack.EMPTY);
			this.patterns = new UUID[size];
		}

		public Ent(BlockPos pos, BlockState state) {
			this(DeepPocketRegistry.CRAFTER_ENTITY.get(), pos, state, 9);
		}

		@Override
		public void setPocket(UUID pocketId) {
			if (level == null || level.isClientSide) {
				super.setPocket(pocketId);
				return;
			}
			Pocket oldPocket = getServerPocket();
			if (oldPocket != null)
				for (UUID pattern : patterns)
					if (pattern != null)
						oldPocket.removePattern(pattern);
			Arrays.fill(patterns, null);

			super.setPocket(pocketId);

			for (int i = 0; i < patterns.length; i++)
				updatePattern(i);
		}

		private void updatePattern(int slot) {
			if (!(level instanceof ServerLevel serverLevel))
				return;
			Pocket pocket = getServerPocket();
			if (pocket == null)
				return;
			ItemStack stack = items.get(slot);
			if (stack.isEmpty()) {
				UUID patternId = patterns[slot];
				if (patternId != null) {
					pocket.removePattern(patternId);
					patterns[slot] = null;
				}
				return;
			}
			patterns[slot] = pocket.addPattern(
							CraftingPatternItem.retrieveInput(stack),
							CraftingPatternItem.retrieveOutput(stack),
							serverLevel,
							getBlockPos()
			);
		}

		@Override
		public void load(CompoundTag pTag) {
			super.load(pTag);
			this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		}

		@Override
		protected void saveAdditional(CompoundTag tag) {
			super.saveAdditional(tag);
			ContainerHelper.saveAllItems(tag, this.items);
		}







		@Override
		public int getContainerSize() {
			return items.size();
		}

		@Override
		public boolean isEmpty() {
			for(ItemStack stack : this.items)
				if (!stack.isEmpty())
					return false;
			return true;
		}

		@Override
		public ItemStack getItem(int slot) {
			return this.items.get(slot);
		}

		@Override
		public ItemStack removeItem(int slot, int amount) {
			return removeItemNoUpdate(slot);
		}

		@Override
		public ItemStack removeItemNoUpdate(int slot) {
			ItemStack stack = this.items.get(slot);
			this.items.set(slot, ItemStack.EMPTY);
			updatePattern(slot);
			setChanged();
			return stack;
		}

		@Override
		public void setItem(int slot, ItemStack stack) {
			items.set(slot, stack.copy());
			updatePattern(slot);
			setChanged();
		}

		@Override
		public boolean stillValid(Player player) {
			if (level == null || level.getBlockEntity(this.worldPosition) != this)
				return false;
			return player.distanceToSqr((double)this.worldPosition.getX() + 0.5D, (double)this.worldPosition.getY() + 0.5D, (double)this.worldPosition.getZ() + 0.5D) <= 64.0D;
		}

		@Override
		public void clearContent() {
			for (int i = 0; i < getContainerSize(); i++)
				removeItemNoUpdate(i);
		}


		private final IItemHandler itemHandler = new IItemHandlerModifiable() {
			@Override
			public int getSlots() {
				return CrafterBlock.Ent.this.getContainerSize();
			}

			@Override
			public @NotNull ItemStack getStackInSlot(int slot) {
				if (slot < 0 || CrafterBlock.Ent.this.getContainerSize() <= slot)
					return ItemStack.EMPTY;

				return CrafterBlock.Ent.this.getItem(slot);
			}

			@Override
			public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
				if (slot < 0 || CrafterBlock.Ent.this.getContainerSize() <= slot)
					return stack;

				if (!isItemValid(slot, stack))
					return stack;
				if (!CrafterBlock.Ent.this.getItem(slot).isEmpty())
					return stack;
				if (!simulate) {
					ItemStack insert = stack;
					if (insert.getCount() != 1) {
						insert = insert.copy();
						insert.setCount(1);
					}
					CrafterBlock.Ent.this.setItem(slot, insert);
				}

				if (stack.getCount() == 1)
					return ItemStack.EMPTY;
				stack = stack.copy();
				stack.shrink(1);
				return stack;
			}

			@Override
			public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
				if (slot < 0 || CrafterBlock.Ent.this.getContainerSize() <= slot)
					return ItemStack.EMPTY;

				ItemStack stack = CrafterBlock.Ent.this.getItem(slot);
				if (!simulate)
					CrafterBlock.Ent.this.setItem(slot, ItemStack.EMPTY);
				return stack;
			}

			@Override
			public int getSlotLimit(int slot) {
				return 1;
			}

			@Override
			public boolean isItemValid(int slot, @NotNull ItemStack stack) {
				return stack.is(DeepPocketRegistry.CRAFTING_PATTERN_ITEM.get());
			}

			@Override
			public void setStackInSlot(int slot, @NotNull ItemStack stack) {
				if (slot < 0 || CrafterBlock.Ent.this.getContainerSize() <= slot)
					return;
				if (!isItemValid(slot, stack))
					return;
				stack = stack.copy();
				stack.setCount(1);
				CrafterBlock.Ent.this.setItem(slot, stack);
			}
		};




		@Override
		public long executePattern(Pocket pocket, WorldCraftingPattern pattern, ProvidedResources resources, long max) {
			//TODO implement
			return 0;
		}
	}
}
