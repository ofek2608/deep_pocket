package com.ofek2608.deep_pocket.registry.interfaces;

import com.ofek2608.deep_pocket.api.struct.SignalSettings;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

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
			this(DeepPocketRegistry.ACTIVE_EXPORTER_ENTITY.get(), pos, state);//TODO fix block entity type
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
			boolean newOutput = settings.check(getPocketId());
			if (output == newOutput)
				return;
			output = newOutput;
			setChanged();
			sendBlockUpdate();
		}
	}
}
