package com.ofek2608.deep_pocket.network;

import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.struct.*;
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

	private static final String PROTOCOL_VERSION = "0.0.8";
	private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(DeepPocketMod.loc("main"), ()->PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

	static {
		int pid = -1;
		Optional<NetworkDirection> clientbound = Optional.of(NetworkDirection.PLAY_TO_CLIENT);
		Optional<NetworkDirection> serverbound = Optional.of(NetworkDirection.PLAY_TO_SERVER);

		CHANNEL.registerMessage(++pid, CBPermitPublicPocket.class, CBPermitPublicPocket::encode, CBPermitPublicPocket::new, CBPermitPublicPocket::handle, clientbound);
		CHANNEL.registerMessage(++pid, CBConversions.class, CBConversions::encode, CBConversions::new, CBConversions::handle, clientbound);
		
		CHANNEL.registerMessage(++pid, CBSetPlayersName.class, CBSetPlayersName::encode, CBSetPlayersName::new, CBSetPlayersName::handle, clientbound);

		CHANNEL.registerMessage(++pid, CBPocketCreate.class, CBPocketCreate::encode, CBPocketCreate::new, CBPocketCreate::handle, clientbound);
		CHANNEL.registerMessage(++pid, CBPocketDestroy.class, CBPocketDestroy::encode, CBPocketDestroy::new, CBPocketDestroy::handle, clientbound);
		CHANNEL.registerMessage(++pid, CBClearPockets.class, CBClearPockets::encode, CBClearPockets::new, CBClearPockets::handle, clientbound);
		
		CHANNEL.registerMessage(++pid, CBPocketInfo.class, CBPocketInfo::encode, CBPocketInfo::new, CBPocketInfo::handle, clientbound);
		CHANNEL.registerMessage(++pid, CBPocketUpdate.class, CBPocketUpdate::encode, CBPocketUpdate::new, CBPocketUpdate::handle, clientbound);
		CHANNEL.registerMessage(++pid, CBPocketClearData.class, CBPocketClearData::encode, CBPocketClearData::new, CBPocketClearData::handle, clientbound);

		CHANNEL.registerMessage(++pid, CBKnowledgeClear.class, CBKnowledgeClear::encode, CBKnowledgeClear::new, CBKnowledgeClear::handle, clientbound);
		CHANNEL.registerMessage(++pid, CBKnowledgeAdd.class, CBKnowledgeAdd::encode, CBKnowledgeAdd::new, CBKnowledgeAdd::handle, clientbound);
		CHANNEL.registerMessage(++pid, CBKnowledgeRem.class, CBKnowledgeRem::encode, CBKnowledgeRem::new, CBKnowledgeRem::handle, clientbound);

		CHANNEL.registerMessage(++pid, CBSetViewedPocket.class, CBSetViewedPocket::encode, CBSetViewedPocket::new, CBSetViewedPocket::handle, clientbound);
		CHANNEL.registerMessage(++pid, CBSetViewedProcessData.class, CBSetViewedProcessData::encode, CBSetViewedProcessData::new, CBSetViewedProcessData::handle, clientbound);

		CHANNEL.registerMessage(++pid, SBOpenPocket.class, SBOpenPocket::encode, SBOpenPocket::new, SBOpenPocket::handle, serverbound);
		CHANNEL.registerMessage(++pid, SBSelectPocket.class, SBSelectPocket::encode, SBSelectPocket::new, SBSelectPocket::handle, serverbound);
		CHANNEL.registerMessage(++pid, SBCreatePocket.class, SBCreatePocket::encode, SBCreatePocket::new, SBCreatePocket::handle, serverbound);
		CHANNEL.registerMessage(++pid, SBChangePocketSettings.class, SBChangePocketSettings::encode, SBChangePocketSettings::new, SBChangePocketSettings::handle, serverbound);
		CHANNEL.registerMessage(++pid, SBDestroyPocket.class, SBDestroyPocket::encode, SBDestroyPocket::new, SBDestroyPocket::handle, serverbound);

		CHANNEL.registerMessage(++pid, SBPocketInsert.class, SBPocketInsert::encode, SBPocketInsert::new, SBPocketInsert::handle, serverbound);
		CHANNEL.registerMessage(++pid, SBPocketExtract.class, SBPocketExtract::encode, SBPocketExtract::new, SBPocketExtract::handle, serverbound);
		CHANNEL.registerMessage(++pid, SBPatternCreate.class, SBPatternCreate::encode, SBPatternCreate::new, SBPatternCreate::handle, serverbound);
		CHANNEL.registerMessage(++pid, SBRequestRecipe.class, SBRequestRecipe::encode, SBRequestRecipe::new, SBRequestRecipe::handle, serverbound);
		CHANNEL.registerMessage(++pid, SBClearCraftingGrid.class, SBClearCraftingGrid::encode, SBClearCraftingGrid::new, SBClearCraftingGrid::handle, serverbound);
		CHANNEL.registerMessage(++pid, SBBulkCrafting.class, SBBulkCrafting::encode, SBBulkCrafting::new, SBBulkCrafting::handle, serverbound);
		CHANNEL.registerMessage(++pid, SBRequestProcess.class, SBRequestProcess::encode, SBRequestProcess::new, SBRequestProcess::handle, serverbound);

		CHANNEL.registerMessage(++pid, SBPocketSignalSettings.class, SBPocketSignalSettings::encode, SBPocketSignalSettings::new, SBPocketSignalSettings::handle, serverbound);
	}

	public static void cbPermitPublicPocket(PacketDistributor.PacketTarget target, boolean value) { CHANNEL.send(target, new CBPermitPublicPocket(value)); }
	public static void cbConversions(PacketDistributor.PacketTarget target, ElementConversions conversions) { CHANNEL.send(target, new CBConversions(conversions)); }
	
	public static void cbSetPlayersName(PacketDistributor.PacketTarget target, Map<UUID,String> names) { CHANNEL.send(target, new CBSetPlayersName(names)); }
	
	public static void cbCreatePocket(PacketDistributor.PacketTarget target, UUID pocketId, UUID owner, PocketInfo info) { CHANNEL.send(target, new CBPocketCreate(pocketId, owner, info)); }
	public static void cbDestroyPocket(PacketDistributor.PacketTarget target, UUID pocketId) { CHANNEL.send(target, new CBPocketDestroy(pocketId)); }
	public static void cbClearPockets(PacketDistributor.PacketTarget target) { CHANNEL.send(target, new CBClearPockets()); }

	public static void cbPocketInfo(PacketDistributor.PacketTarget target, UUID pocketId, PocketInfo info) { CHANNEL.send(target, new CBPocketInfo(pocketId, info)); }
	public static void cbPocketUpdate(PacketDistributor.PacketTarget target, UUID pocketId, PocketUpdate update) { CHANNEL.send(target, new CBPocketUpdate(pocketId, update)); }
	public static void cbPocketClearData(PacketDistributor.PacketTarget target, UUID pocketId) { CHANNEL.send(target, new CBPocketClearData(pocketId)); }

	public static void cbKnowledgeClear(PacketDistributor.PacketTarget target) { CHANNEL.send(target, new CBKnowledgeClear()); }
	public static void cbKnowledgeAdd(PacketDistributor.PacketTarget target, int ... elementIds) { CHANNEL.send(target, new CBKnowledgeAdd(elementIds)); }
	public static void cbKnowledgeRem(PacketDistributor.PacketTarget target, int ... elementIds) { CHANNEL.send(target, new CBKnowledgeRem(elementIds)); }

	public static void cbSetViewedPocket(PacketDistributor.PacketTarget target, UUID pocketId) { CHANNEL.send(target, new CBSetViewedPocket(pocketId)); }
	public static void cbSetViewedProcessData(PacketDistributor.PacketTarget target, ProcessUnitClientData data) { CHANNEL.send(target, new CBSetViewedProcessData(data)); }

	public static void sbOpenPocket(int type) { CHANNEL.send(serverTarget(), new SBOpenPocket(type)); }
	public static void sbSelectPocket(UUID pocketId) { CHANNEL.send(serverTarget(), new SBSelectPocket(pocketId)); }
	public static void sbCreatePocket(PocketInfo info) { CHANNEL.send(serverTarget(), new SBCreatePocket(info)); }
	public static void sbChangePocketSettings(UUID pocketId, PocketInfo info) { CHANNEL.send(serverTarget(), new SBChangePocketSettings(pocketId, info)); }
	public static void sbDestroyPocket(UUID pocketId) { CHANNEL.send(serverTarget(), new SBDestroyPocket(pocketId)); }

	public static void sbPocketInsert(byte count) { CHANNEL.send(serverTarget(), new SBPocketInsert(count)); }
	public static void sbPocketExtract(ElementType.TItem type, boolean toCarry, byte count) { CHANNEL.send(serverTarget(), new SBPocketExtract(type, toCarry, count)); }
	public static void sbPatternCreate(ElementTypeStack[] input, ElementTypeStack[] output, boolean toCarry) { CHANNEL.send(serverTarget(), new SBPatternCreate(input, output, toCarry)); }
	public static void sbRequestRecipe(ElementType[] elements) { CHANNEL.send(serverTarget(), new SBRequestRecipe(elements)); }
	public static void sbClearCraftingGrid(boolean up) { CHANNEL.send(serverTarget(), new SBClearCraftingGrid(up)); }
	public static void sbBulkCrafting(long count) { CHANNEL.send(serverTarget(), new SBBulkCrafting(count)); }
	public static void sbRequestProcess(RecipeRequest[] requests, Map<ElementType, Optional<UUID>> setDefaultPatterns) { CHANNEL.send(serverTarget(), new SBRequestProcess(requests, setDefaultPatterns)); }
	
	public static void sbPocketSignalSettings(BlockPos pos, SignalSettings settings) { CHANNEL.send(serverTarget(), new SBPocketSignalSettings(pos, settings)); }
	
	private static PacketDistributor.PacketTarget serverTarget() { return PacketDistributor.SERVER.noArg(); }
}
