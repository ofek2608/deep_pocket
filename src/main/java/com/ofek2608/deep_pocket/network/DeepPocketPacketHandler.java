package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.ItemValue;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.api.struct.SignalSettings;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class DeepPocketPacketHandler {
	private DeepPocketPacketHandler() {}
	@SuppressWarnings("EmptyMethod") public static void loadClass() {}

	private static final String PROTOCOL_VERSION = "1";
	private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(DeepPocketMod.loc("main"), ()->PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

	static {
		int pid = -1;
		Optional<NetworkDirection> clientbound = Optional.of(NetworkDirection.PLAY_TO_CLIENT);
		Optional<NetworkDirection> serverbound = Optional.of(NetworkDirection.PLAY_TO_SERVER);

		CHANNEL.registerMessage(++pid, CBPermitPublicPocket.class, CBPermitPublicPocket::encode, CBPermitPublicPocket::new, CBPermitPublicPocket::handle, clientbound);

		CHANNEL.registerMessage(++pid, CBSetItemValue.class, CBSetItemValue::encode, CBSetItemValue::new, CBSetItemValue::handle, clientbound);
		CHANNEL.registerMessage(++pid, CBRemoveItemValue.class, CBRemoveItemValue::encode, CBRemoveItemValue::new, CBRemoveItemValue::handle, clientbound);
		CHANNEL.registerMessage(++pid, CBClearItemValues.class, CBClearItemValues::encode, CBClearItemValues::new, CBClearItemValues::handle, clientbound);

		CHANNEL.registerMessage(++pid, CBPocketCreate.class, CBPocketCreate::encode, CBPocketCreate::new, CBPocketCreate::handle, clientbound);
		CHANNEL.registerMessage(++pid, CBPocketDestroy.class, CBPocketDestroy::encode, CBPocketDestroy::new, CBPocketDestroy::handle, clientbound);
		CHANNEL.registerMessage(++pid, CBClearPockets.class, CBClearPockets::encode, CBClearPockets::new, CBClearPockets::handle, clientbound);

		CHANNEL.registerMessage(++pid, CBPocketSetName.class, CBPocketSetName::encode, CBPocketSetName::new, CBPocketSetName::handle, clientbound);
		CHANNEL.registerMessage(++pid, CBPocketSetIcon.class, CBPocketSetIcon::encode, CBPocketSetIcon::new, CBPocketSetIcon::handle, clientbound);
		CHANNEL.registerMessage(++pid, CBPocketSetColor.class, CBPocketSetColor::encode, CBPocketSetColor::new, CBPocketSetColor::handle, clientbound);
		CHANNEL.registerMessage(++pid, CBPocketSetSecurity.class, CBPocketSetSecurity::encode, CBPocketSetSecurity::new, CBPocketSetSecurity::handle, clientbound);
		CHANNEL.registerMessage(++pid, CBPocketSetItemCount.class, CBPocketSetItemCount::encode, CBPocketSetItemCount::new, CBPocketSetItemCount::handle, clientbound);
		CHANNEL.registerMessage(++pid, CBPocketClearItems.class, CBPocketClearItems::encode, CBPocketClearItems::new, CBPocketClearItems::handle, clientbound);

		CHANNEL.registerMessage(++pid, CBSetPlayersName.class, CBSetPlayersName::encode, CBSetPlayersName::new, CBSetPlayersName::handle, clientbound);

		CHANNEL.registerMessage(++pid, CBAddKnowledge.class, CBAddKnowledge::encode, CBAddKnowledge::new, CBAddKnowledge::handle, clientbound);
		CHANNEL.registerMessage(++pid, CBRemoveKnowledge.class, CBRemoveKnowledge::encode, CBRemoveKnowledge::new, CBRemoveKnowledge::handle, clientbound);
		CHANNEL.registerMessage(++pid, CBClearKnowledge.class, CBClearKnowledge::encode, CBClearKnowledge::new, CBClearKnowledge::handle, clientbound);

		CHANNEL.registerMessage(++pid, CBSetViewedPocket.class, CBSetViewedPocket::encode, CBSetViewedPocket::new, CBSetViewedPocket::handle, clientbound);

		CHANNEL.registerMessage(++pid, SBOpenPocket.class, SBOpenPocket::encode, SBOpenPocket::new, SBOpenPocket::handle, serverbound);
		CHANNEL.registerMessage(++pid, SBSelectPocket.class, SBSelectPocket::encode, SBSelectPocket::new, SBSelectPocket::handle, serverbound);
		CHANNEL.registerMessage(++pid, SBCreatePocket.class, SBCreatePocket::encode, SBCreatePocket::new, SBCreatePocket::handle, serverbound);
		CHANNEL.registerMessage(++pid, SBChangePocketSettings.class, SBChangePocketSettings::encode, SBChangePocketSettings::new, SBChangePocketSettings::handle, serverbound);
		CHANNEL.registerMessage(++pid, SBDestroyPocket.class, SBDestroyPocket::encode, SBDestroyPocket::new, SBDestroyPocket::handle, serverbound);

		CHANNEL.registerMessage(++pid, SBPocketInsert.class, SBPocketInsert::encode, SBPocketInsert::new, SBPocketInsert::handle, serverbound);
		CHANNEL.registerMessage(++pid, SBPocketExtract.class, SBPocketExtract::encode, SBPocketExtract::new, SBPocketExtract::handle, serverbound);
		CHANNEL.registerMessage(++pid, SBRequestRecipe.class, SBRequestRecipe::encode, SBRequestRecipe::new, SBRequestRecipe::handle, serverbound);
		CHANNEL.registerMessage(++pid, SBClearCraftingGrid.class, SBClearCraftingGrid::encode, SBClearCraftingGrid::new, SBClearCraftingGrid::handle, serverbound);

		CHANNEL.registerMessage(++pid, SBPocketSignalSettings.class, SBPocketSignalSettings::encode, SBPocketSignalSettings::new, SBPocketSignalSettings::handle, serverbound);
	}

	public static void cbPermitPublicPocket(PacketDistributor.PacketTarget target, boolean value) { CHANNEL.send(target, new CBPermitPublicPocket(value)); }

	public static void cbSetItemValue(PacketDistributor.PacketTarget target, Map<ItemType, ItemValue> values) { CHANNEL.send(target, new CBSetItemValue(values)); }
	public static void cbRemoveItemValue(PacketDistributor.PacketTarget target, ItemType ... types) { CHANNEL.send(target, new CBRemoveItemValue(types)); }
	public static void cbClearItemValues(PacketDistributor.PacketTarget target) { CHANNEL.send(target, new CBClearItemValues()); }

	public static void cbCreatePocket(PacketDistributor.PacketTarget target, UUID pocketId, UUID owner, String name, ItemType icon, int color, PocketSecurityMode securityMode) { CHANNEL.send(target, new CBPocketCreate(pocketId, owner, name, icon, color, securityMode)); }
	public static void cbDestroyPocket(PacketDistributor.PacketTarget target, UUID pocketId) { CHANNEL.send(target, new CBPocketDestroy(pocketId)); }
	public static void cbClearPockets(PacketDistributor.PacketTarget target) { CHANNEL.send(target, new CBClearPockets()); }

	public static void cbPocketSetName(PacketDistributor.PacketTarget target, UUID pocketId, String name) { CHANNEL.send(target, new CBPocketSetName(pocketId, name)); }
	public static void cbPocketSetIcon(PacketDistributor.PacketTarget target, UUID pocketId, ItemType icon) { CHANNEL.send(target, new CBPocketSetIcon(pocketId, icon)); }
	public static void cbPocketSetColor(PacketDistributor.PacketTarget target, UUID pocketId, int color) { CHANNEL.send(target, new CBPocketSetColor(pocketId, color)); }
	public static void cbPocketSetSecurity(PacketDistributor.PacketTarget target, UUID pocketId, PocketSecurityMode mode) { CHANNEL.send(target, new CBPocketSetSecurity(pocketId, mode)); }
	public static void cbPocketSetItemCount(PacketDistributor.PacketTarget target, UUID pocketId, Map<ItemType,Double> counts) { CHANNEL.send(target, new CBPocketSetItemCount(pocketId, counts)); }
	public static void cbPocketClearItems(PacketDistributor.PacketTarget target, UUID pocketId) { CHANNEL.send(target, new CBPocketClearItems(pocketId)); }

	public static void cbSetPlayersName(PacketDistributor.PacketTarget target, Map<UUID,String> names) { CHANNEL.send(target, new CBSetPlayersName(names)); }

	public static void cbAddKnowledge(PacketDistributor.PacketTarget target, ItemType ... types) { CHANNEL.send(target, new CBAddKnowledge(types)); }
	public static void cbRemoveKnowledge(PacketDistributor.PacketTarget target, ItemType ... types) { CHANNEL.send(target, new CBRemoveKnowledge(types)); }
	public static void cbClearKnowledge(PacketDistributor.PacketTarget target) { CHANNEL.send(target, new CBClearKnowledge()); }

	public static void cbSetViewedPocket(PacketDistributor.PacketTarget target, UUID pocketId) { CHANNEL.send(target, new CBSetViewedPocket(pocketId)); }

	public static void sbOpenPocket() { CHANNEL.send(serverTarget(), new SBOpenPocket()); }
	public static void sbSelectPocket(UUID pocketId) { CHANNEL.send(serverTarget(), new SBSelectPocket(pocketId)); }
	public static void sbCreatePocket(String name, ItemType icon, int color, PocketSecurityMode securityMode) { CHANNEL.send(serverTarget(), new SBCreatePocket(name, icon, color, securityMode)); }
	public static void sbChangePocketSettings(UUID pocketId, String name, ItemType icon, int color, PocketSecurityMode securityMode) { CHANNEL.send(serverTarget(), new SBChangePocketSettings(pocketId, name, icon, color, securityMode)); }
	public static void sbDestroyPocket(UUID pocketId) { CHANNEL.send(serverTarget(), new SBDestroyPocket(pocketId)); }

	public static void sbPocketInsert(byte count) { CHANNEL.send(serverTarget(), new SBPocketInsert(count)); }
	public static void sbPocketExtract(ItemType type, boolean toCarry, byte count) { CHANNEL.send(serverTarget(), new SBPocketExtract(type, toCarry, count)); }
	public static void sbRequestRecipe(ItemType[] items) { CHANNEL.send(serverTarget(), new SBRequestRecipe(items)); }
	public static void sbClearCraftingGrid(boolean up) { CHANNEL.send(serverTarget(), new SBClearCraftingGrid(up)); }

	public static void sbPocketSignalSettings(BlockPos pos, SignalSettings settings) { CHANNEL.send(serverTarget(), new SBPocketSignalSettings(pos, settings)); }

	private static PacketDistributor.PacketTarget serverTarget() {
		return PacketDistributor.SERVER.noArg();
	}
}
