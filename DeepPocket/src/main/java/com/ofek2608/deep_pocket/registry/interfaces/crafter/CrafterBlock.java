package com.ofek2608.deep_pocket.registry.interfaces.crafter;

import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.PatternSupportedBlockEntity;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.ProvidedResources;
import com.ofek2608.deep_pocket.api.pocket_process.PocketProcessRecipe;
import com.ofek2608.deep_pocket.api.struct.*;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import com.ofek2608.deep_pocket.registry.ProtoMenu;
import com.ofek2608.deep_pocket.registry.interfaces.BlockEntityWithPocket;
import com.ofek2608.deep_pocket.registry.items.crafting_pattern.CraftingPatternItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;
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
	public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!oldState.is(newState.getBlock())) {
			if (level.getBlockEntity(pos) instanceof Container container) {
				Containers.dropContents(level, pos, container);
				container.clearContent();
				level.updateNeighbourForOutputSignal(pos, this);
			}

			super.onRemove(oldState, level, pos, newState, isMoving);
		}
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
		protected final NonNullList<ItemStack> items;
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
			clearContent();
			ContainerHelper.loadAllItems(pTag, this.items);
			ListTag savedPatterns = pTag.getList("patterns", 11);
			for (int i = 0; i < 9 && i < savedPatterns.size(); i++) {
				try {
					patterns[i] = NbtUtils.loadUUID(savedPatterns.get(i));
				} catch (Exception ignored) {}
			}
		}

		@Override
		protected void saveAdditional(CompoundTag tag) {
			super.saveAdditional(tag);
			ContainerHelper.saveAllItems(tag, this.items);
			ListTag savedPatterns = new ListTag();
			for (UUID pattern : patterns)
				savedPatterns.add(pattern == null ? new IntArrayTag(new int[0]) : NbtUtils.createUUID(pattern));
			tag.put("patterns", savedPatterns);
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
				return stack.isEmpty() || stack.is(DeepPocketRegistry.CRAFTING_PATTERN_ITEM.get());
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
		public boolean containsPattern(UUID patternId) {
			for (UUID thisPatternId : patterns)
				if (thisPatternId.equals(patternId))
					return true;
			return false;
		}

		@Override
		public boolean executePattern(CrafterContext ctx) {
			Pocket pocket = ctx.pocket;
			WorldCraftingPattern pattern = ctx.pattern;
			ProvidedResources resources = ctx.resources;
			PocketProcessRecipe pocketProcessRecipe = ctx.recipe;

			if (!containsPattern(pattern.getPatternId()))
				throw new IllegalArgumentException();
			long[] requirements = PatternSupportedBlockEntity.getRequirements(pattern, resources);
			ServerLevel level = pattern.getLevel();

			Direction facing = getBlockState().getValue(FACING);
			BlockPos pos = getBlockPos().relative(facing);
			if (level.getBlockState(pos).is(Blocks.CRAFTING_TABLE))
				return executeCraftingTable(level, pocket, pattern.getInput(), resources, requirements, pocketProcessRecipe);

			BlockEntity entity = level.getBlockEntity(pos);
			if (entity == null)
				return false;
			Optional<IItemHandler> itemHandler = entity.getCapability(ForgeCapabilities.ITEM_HANDLER, facing.getOpposite()).resolve();
			if (itemHandler.isEmpty())
				return false;
			return executeAdvanced(resources, requirements, itemHandler.get(), pocketProcessRecipe);
		}

		private boolean executeCraftingTable(ServerLevel level, Pocket pocket, ItemTypeAmount[] input, ProvidedResources resources, long[] requirements, PocketProcessRecipe pocketProcessRecipe) {
			if (input.length != 9)
				return false;
			CraftingContainer container = new CraftingContainer(ProtoMenu.INSTANCE, 3, 3);
			for (int i = 0; i < 9; i++) {
				ItemTypeAmount typeAmount = input[i];
				if (typeAmount.isEmpty())
					continue;
				if (typeAmount.getAmount() != 1)
					return false;
				container.setItem(i, typeAmount.getItemType().create());
			}
			RecipeManager recipeManager = level.getServer().getRecipeManager();
			Optional<CraftingRecipe> recipe = recipeManager.getRecipeFor(RecipeType.CRAFTING, container, level);
			if (recipe.isEmpty())
				return false;

			resources.returnAllToParent();
			long got = resources.requestFromParent(requirements, pocketProcessRecipe.getLeftToCraft());
			for (int i = 0; i < resources.getTypeCount(); i++)
				resources.take(i, -1);

			insertPocket(pocket, got, recipe.get().assemble(container));
			for (ItemStack result : recipe.get().getRemainingItems(container))
				insertPocket(pocket, got, result);

			return pocketProcessRecipe.removeLeftToCraft(got);
		}

		private void insertPocket(Pocket pocket, long times, ItemStack stack) {
			pocket.insertElement(ElementType.item(stack), DeepPocketUtils.advancedMul(times, stack.getCount()));
		}

		private boolean executeAdvanced(ProvidedResources resources, long[] requirements, IItemHandler handler, PocketProcessRecipe pocketProcessRecipe) {
			if (insertItemsToHandler(resources, handler))
				return false;
			if (pocketProcessRecipe.getLeftToCraft() == 0)
				return true;

			if (resources.requestFromParent(requirements, 1) != 1)
				return false;
			pocketProcessRecipe.removeLeftToCraft(1);
			insertItemsToHandler(resources, handler);
			return false;
		}

		private boolean insertItemsToHandler(ProvidedResources resources, IItemHandler handler) {
			boolean containItems = false;
			for (int i = 0; i < resources.getTypeCount(); i++) {
				long provided = resources.getProvided(i);
				if (provided == 0)
					continue;
				containItems = true;
				resources.take(i, insertItemToHandler(resources.getType(i), provided, handler));
			}
			return containItems;
		}

		private long insertItemToHandler(ItemType type, long provided, IItemHandler handler) {
			long taken = 0;
			for (int slotIndex = 0; slotIndex < handler.getSlots(); slotIndex++) {
				int slotLimit = handler.getSlotLimit(slotIndex);
				if (slotLimit <= 0)
					continue;
				int startingCount = provided < 0 ? slotLimit : (int)Math.min(provided - taken, slotLimit);
				ItemStack stack = type.create(startingCount);
				ItemStack remaining = handler.insertItem(slotIndex, stack, false);
				taken = DeepPocketUtils.advancedSum(taken, remaining.isEmpty() ? startingCount : startingCount - remaining.getCount());
			}
			return taken;
		}
	}
}
