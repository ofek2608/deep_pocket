package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.types.EntryType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.minecraftforge.network.PacketDistributor.PacketTarget;

public final class PacketHandler {
	private PacketHandler() {}
	public static void loadClass() {}
	
	private static final String PROTOCOL_VERSION = "0.0.8";
	private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			DeepPocketMod.loc("main"), ()->PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	private static int nextPid = 0;
	
	static {
		register(CBSetServerConfig.class, CBSetServerConfig::new);
		register(CBAddProperties.class, CBAddProperties::new);
		register(CBAddPocket.class, CBAddPocket::new);
		register(CBClearPocket.class, CBClearPocket::new);
		register(CBDeletePocket.class, CBDeletePocket::new);
		register(CBSetTypeCount.class, CBSetTypeCount::new);
		register(SBCreatePocket.class, SBCreatePocket::new);
		
//		register(CBPermitPublicPocket.class, CBPermitPublicPocket::new);
//		register(CBConversions.class, CBConversions::new);
//
//		register(CBSetPlayersName.class, CBSetPlayersName::new);
//
//		register(CBPocketCreate.class, CBPocketCreate::new);
//		register(CBPocketDestroy.class, CBPocketDestroy::new);
//		register(CBClearPockets.class, CBClearPockets::new);
//
//		register(CBPocketInfo.class, CBPocketInfo::new);
//		register(CBPocketUpdate.class, CBPocketUpdate::new);
//		register(CBPocketClearData.class, CBPocketClearData::new);
//
//		register(CBKnowledgeClear.class, CBKnowledgeClear::new);
//		register(CBKnowledgeAdd.class, CBKnowledgeAdd::new);
//		register(CBKnowledgeRem.class, CBKnowledgeRem::new);
//
//		register(CBSetViewedPocket.class, CBSetViewedPocket::new);
//		register(CBSetViewedProcessData.class, CBSetViewedProcessData::new);
//
//		register(SBOpenPocket.class, SBOpenPocket::new);
//		register(SBSelectPocket.class, SBSelectPocket::new);
//		register(SBCreatePocket.class, SBCreatePocket::new);
//		register(SBChangePocketSettings.class, SBChangePocketSettings::new);
//		register(SBDestroyPocket.class, SBDestroyPocket::new);
//
//		register(SBPocketInsert.class, SBPocketInsert::new);
//		register(SBPocketExtract.class, SBPocketExtract::new);
//		register(SBPatternCreate.class, SBPatternCreate::new);
//		register(SBRequestRecipe.class, SBRequestRecipe::new);
//		register(SBClearCraftingGrid.class, SBClearCraftingGrid::new);
//		register(SBBulkCrafting.class, SBBulkCrafting::new);
//		register(SBRequestProcess.class, SBRequestProcess::new);
//
//		register(SBPocketSignalSettings.class, SBPocketSignalSettings::new);
	}
	
	
	public static void cbSetServerConfig(PacketTarget target, ServerConfigImpl config) { send(target, new CBSetServerConfig(config)); }
	public static void cbAddProperties(PacketTarget target, PocketPropertiesImpl properties) { send(target, new CBAddProperties(properties)); }
	public static void cbAddPocket(PacketTarget target, UUID pocketId) { send(target, new CBAddPocket(pocketId)); }
	public static void cbClearPocket(PacketTarget target, UUID pocketId) { send(target, new CBClearPocket(pocketId)); }
	public static void cbDeletePocket(PacketTarget target, UUID pocketId) { send(target, new CBDeletePocket(pocketId)); }
	public static void cbSetTypeCount(PacketTarget target, UUID pocketId, EntryType type, long count) { send(target, new CBSetTypeCount(pocketId, type, count)); }
	
	public static void sbCreatePocket() { send(new SBCreatePocket()); }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private static <MSG extends PacketBase> void register(Class<MSG> clazz, Function<FriendlyByteBuf, MSG> decoder) {
		boolean cb = CBPacketBase.class.isAssignableFrom(clazz);
		boolean sb = SBPacketBase.class.isAssignableFrom(clazz);
		Optional<NetworkDirection> direction = cb == sb ? Optional.empty() :
				Optional.of(cb ? NetworkDirection.PLAY_TO_CLIENT : NetworkDirection.PLAY_TO_SERVER);
		CHANNEL.registerMessage(nextPid++, clazz, PacketBase::encode, decoder, PacketBase::handle, direction);
	}
	
	private static <MSG extends PacketBase> void send(PacketTarget target, MSG msg) {
		CHANNEL.send(target, msg);
	}
	
	private static <MSG extends PacketBase> void send(MSG msg) {
		CHANNEL.send(PacketDistributor.SERVER.noArg(), msg);
	}
	
	private interface PacketBase {
		void encode(FriendlyByteBuf buf);
		void handle(NetworkEvent.Context ctx);
		
		default void handle(Supplier<NetworkEvent.Context> ctxSup) {
			NetworkEvent.Context ctx = ctxSup.get();
			ctx.enqueueWork(() -> handle(ctx));
			ctx.setPacketHandled(true);
		}
	}
	
	private interface CBPacketBase extends PacketBase {
		void handle(ClientAPIImpl api);
		
		@Override
		default void handle(NetworkEvent.Context ctx) {
			if (ClientAPIImpl.instance == null) {
				return;
			}
			handle(ClientAPIImpl.instance);
		}
	}
	
	private interface SBPacketBase extends PacketBase {
		void handle(ServerAPIImpl api, ServerPlayer player);
		
		@Override
		default void handle(NetworkEvent.Context ctx) {
			if (ServerAPIImpl.instance == null) {
				return;
			}
			handle(ServerAPIImpl.instance, Objects.requireNonNull(ctx.getSender()));
		}
	}
	
	
	
	
	
	public record CBSetServerConfig(ServerConfigImpl serverConfig) implements CBPacketBase {
		public CBSetServerConfig(FriendlyByteBuf buf) {
			this(
					ServerConfigImpl.decode(buf)
			);
		}
		
		@Override
		public void encode(FriendlyByteBuf buf) {
			ServerConfigImpl.encode(serverConfig, buf);
		}
		
		@Override
		public void handle(ClientAPIImpl api) {
			api.serverConfig = serverConfig;
		}
	}
	
	public record CBAddProperties(PocketPropertiesImpl properties) implements CBPacketBase {
		public CBAddProperties(FriendlyByteBuf buf) {
			this(PocketPropertiesImpl.decode(buf));
		}
		
		@Override
		public void encode(FriendlyByteBuf buf) {
			PocketPropertiesImpl.encode(buf, properties);
		}
		
		@Override
		public void handle(ClientAPIImpl api) {
			api.putProperties(properties);
		}
	}
	
	public record CBAddPocket(UUID pocketId) implements CBPacketBase {
		public CBAddPocket(FriendlyByteBuf buf) {
			this(buf.readUUID());
		}
		
		@Override
		public void encode(FriendlyByteBuf buf) {
			buf.writeUUID(pocketId);
		}
		
		@Override
		public void handle(ClientAPIImpl api) {
			api.getOrCreatePocket(pocketId);
		}
	}
	
	public record CBClearPocket(UUID pocketId) implements CBPacketBase {
		public CBClearPocket(FriendlyByteBuf buf) {
			this(buf.readUUID());
		}
		
		@Override
		public void encode(FriendlyByteBuf buf) {
			buf.writeUUID(pocketId);
		}
		
		@Override
		public void handle(ClientAPIImpl api) {
			api.clearPocket(pocketId);
		}
	}
	
	public record CBDeletePocket(UUID pocketId) implements CBPacketBase {
		public CBDeletePocket(FriendlyByteBuf buf) {
			this(buf.readUUID());
		}
		
		@Override
		public void encode(FriendlyByteBuf buf) {
			buf.writeUUID(pocketId);
		}
		
		@Override
		public void handle(ClientAPIImpl api) {
			api.deletePocket(pocketId);
		}
	}
	
	public record CBSetTypeCount(UUID pocketId, EntryType type, long count) implements CBPacketBase {
		public CBSetTypeCount(FriendlyByteBuf buf) {
			this(
					buf.readUUID(),
					EntryType.decode(buf),
					buf.readLong()
			);
		}
		
		@Override
		public void encode(FriendlyByteBuf buf) {
			buf.writeUUID(pocketId);
			EntryType.encode(buf, type);
			buf.writeLong(count);
		}
		
		@Override
		public void handle(ClientAPIImpl api) {
			api.getOrCreatePocket(pocketId).ifPresent(pocket -> pocket.getTypeData(type).count = count);
		}
	}
	
	public record SBCreatePocket() implements SBPacketBase {
		public SBCreatePocket(FriendlyByteBuf buf) {
			this();
		}
		
		@Override
		public void encode(FriendlyByteBuf buf) {
		}
		
		@Override
		public void handle(ServerAPIImpl api, ServerPlayer player) {
			api.payAndCreatePocket(player);
		}
	}
}
