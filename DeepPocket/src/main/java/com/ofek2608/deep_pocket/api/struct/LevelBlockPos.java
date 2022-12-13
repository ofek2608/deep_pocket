package com.ofek2608.deep_pocket.api.struct;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Objects;

public final class LevelBlockPos {
	public static final LevelBlockPos ZERO = new LevelBlockPos(Level.OVERWORLD, BlockPos.ZERO);
	private final ResourceKey<Level> level;
	private final BlockPos pos;

	public LevelBlockPos(ResourceKey<Level> level, BlockPos pos) {
		this.level = level;
		this.pos = pos;
	}
	
	public static LevelBlockPos load(CompoundTag saved) {
		return new LevelBlockPos(
				ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(saved.getString("level"))),
				BlockPos.of(saved.getLong("pos"))
		);
	}
	
	public static CompoundTag save(LevelBlockPos levelBlockPos) {
		CompoundTag saved = new CompoundTag();
		saved.putString("level", levelBlockPos.level.location().toString());
		saved.putLong("pos", levelBlockPos.pos.asLong());
		return saved;
	}

	public @Nullable ServerLevel getLevel(MinecraftServer server) {
		return server.getLevel(level);
	}

	public BlockPos getPos() {
		return pos;
	}
	
	@Override
	public boolean equals(Object o) {
		return this == o ||
				o instanceof LevelBlockPos that &&
						this.level.location().equals(that.level.location()) &&
						this.pos.equals(that.pos);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(level.location(), pos);
	}
}
