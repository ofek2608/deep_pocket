package com.ofek2608.deep_pocket.api.struct;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;
import java.util.UUID;

public final class WorldCraftingPatternOld extends CraftingPatternOld {
	private final ServerLevel level;
	private final BlockPos pos;

	public WorldCraftingPatternOld(UUID patternId, ItemTypeAmount[] input, ItemTypeAmount[] output, ServerLevel level, BlockPos pos) {
		super(patternId, input, output);
		this.level = level;
		this.pos = pos;
	}

	public WorldCraftingPatternOld(CompoundTag saved, MinecraftServer server) {
		super(saved);
		this.level = Objects.requireNonNull(server.getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(saved.getString("level")))));
		this.pos = BlockPos.of(saved.getLong("pos"));
	}

	@Override
	public CompoundTag save() {
		CompoundTag saved = super.save();
		saved.putString("level", level.dimension().location().toString());
		saved.putLong("pos", pos.asLong());
		return saved;
	}

	public ServerLevel getLevel() {
		return level;
	}

	public BlockPos getPos() {
		return pos;
	}
}
