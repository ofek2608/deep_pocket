package com.ofek2608.deep_pocket.registry.interfaces;

import com.ofek2608.deep_pocket.api.DeepPocketApi;
import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.Pocket;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.UUID;

public class BlockEntityWithPocket extends BlockEntity {
	private @Nonnull UUID pocketId = Util.NIL_UUID;

	public BlockEntityWithPocket(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public @Nullable Pocket getServerPocket() {
		DeepPocketServerApi api = DeepPocketServerApi.get();
		return api == null ? null : api.getPocket(pocketId);
	}

	public @Nullable Pocket getClientPocket() {
		return DeepPocketClientApi.get().getPocket(pocketId);
	}

	public UUID getPocketId() {
		return pocketId;
	}

	public void setPocket(UUID pocketId) {
		this.pocketId = pocketId;
		setChanged();
		sendBlockUpdate();
	}

	protected void sendBlockUpdate() {
		if (level != null)
			level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
	}

	public boolean canAccess(Player player) {
		if (level == null)
			return false;
		DeepPocketApi api = DeepPocketApi.get(level);
		if (api == null)
			return false;
		Pocket pocket = api.getPocket(pocketId);
		return pocket == null || pocket.canAccess(player);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		if (tag.contains("pocketId")) {
			pocketId = tag.getUUID("pocketId");
			sendBlockUpdate();
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putUUID("pocketId", pocketId);
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = new CompoundTag();
		saveAdditional(tag);
		return tag;
	}

	public static int getTint(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
		if (tintIndex != 0)
			return 0xFFFFFF;
		if (level == null || pos == null)
			return 0xFFFFFF;
		if (!(level.getBlockEntity(pos) instanceof BlockEntityWithPocket entity))
			return 0xFFFFFF;
		Pocket pocket = entity.getClientPocket();
		if (pocket == null)
			return 0xFFFFFF;
		return pocket.getColor();
	}
}
