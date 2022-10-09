package com.ofek2608.deep_pocket.registry.interfaces;

import com.ofek2608.deep_pocket.api.struct.Pocket;
import com.ofek2608.deep_pocket.api.struct.SignalSettings;
import com.ofek2608.deep_pocket.client_screens.ClientScreens;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SignalBlock extends Block implements EntityBlock {
	public SignalBlock(Properties properties) {
		super(properties);
	}

	@SuppressWarnings("deprecation")
	public boolean isSignalSource(BlockState state) {
		return true;
	}

	@SuppressWarnings("deprecation")
	public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
		return level.getBlockEntity(pos) instanceof Ent ent && ent.output ? 15 : 0;
	}

	@SuppressWarnings("deprecation")
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (player.getItemInHand(hand).is(DeepPocketRegistry.POCKET_ITEM.get()))
			return InteractionResult.PASS;
		if (level.isClientSide && level.getBlockEntity(pos) instanceof Ent ent) {
			Pocket pocket = ent.getClientPocket();
			int color = pocket == null ? 0xFFFFFF : pocket.getColor();
			ClientScreens.openScreenConfigureSignalBlock(color, pos, ent.settings);
		}
		return InteractionResult.CONSUME;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new Ent(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return (l, p, s, blockEntity) -> {
			if (blockEntity instanceof Ent ent)
				ent.tick();
		};
	}

	public static class Ent extends BlockEntityWithPocket {
		private @Nonnull SignalSettings settings = new SignalSettings();
		private boolean output;

		public Ent(BlockEntityType<?> type, BlockPos pos, BlockState state) {
			super(type, pos, state);
		}

		public Ent(BlockPos pos, BlockState state) {
			this(DeepPocketRegistry.SIGNAL_ENTITY.get(), pos, state);
		}

		public SignalSettings getSettings() {
			return new SignalSettings(settings);
		}

		public void setSettings(SignalSettings settings) {
			this.settings = new SignalSettings(settings);
			setChanged();
			sendBlockUpdate();
		}

		@Override
		public void load(CompoundTag tag) {
			super.load(tag);
			if (tag.contains("settings"))
				settings.load(tag.getCompound("settings"));
		}

		@Override
		protected void saveAdditional(CompoundTag tag) {
			super.saveAdditional(tag);
			tag.put("settings", settings.save());
		}

		public void tick() {
			setOutput(settings.check(getPocketId()));
		}

		private void setOutput(boolean newOutput) {
			if (output == newOutput)
				return;
			output = newOutput;
			setChanged();
			sendBlockUpdate();

			if (level != null) {
				BlockPos pos = getBlockPos();
				Block block = getBlockState().getBlock();
				level.blockUpdated(pos, block);
				for (Direction direction : Direction.values())
					level.updateNeighborsAt(pos.relative(direction), block);
			}
		}

		public boolean getOutput() {
			return output;
		}
	}
}
