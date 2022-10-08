package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.UnmodifiableView;

import javax.annotation.Nonnull;
import java.util.*;

class PocketImpl implements Pocket {
	private final UUID pocketId;
	private final UUID owner;
	private final boolean isServer;

	private @Nonnull String name = "My new pocket";
	private @Nonnull ItemType icon = new ItemType(DeepPocketUtils.randomItem(), null);
	private int color = DeepPocketUtils.randomColor();
	private @Nonnull PocketSecurityMode securityMode = PocketSecurityMode.PRIVATE;


	private final Map<ItemType,Double> items = new HashMap<>();
	private boolean clearedItems = false;
	private final Map<ItemType,Double> changedItems = new HashMap<>();

	PocketImpl(UUID pocketId, UUID owner, boolean isServer) {
		this.pocketId = pocketId;
		this.owner = owner;
		this.isServer = isServer;
	}

	private PocketImpl(PocketImpl copy) {
		this.pocketId = copy.pocketId;
		this.owner = copy.owner;
		this.isServer = false;
		this.name = copy.name;
		this.icon = copy.icon;
		this.color = copy.color;
		this.securityMode = copy.securityMode;
		this.items.putAll(copy.items);
		this.clearedItems = false;
	}

	PocketImpl(CompoundTag saved) {
		this.pocketId = saved.getUUID("pocketId");
		this.owner = saved.getUUID("owner");
		this.isServer = true;
		String name = saved.getString("name");
		if (name.length() <= MAX_NAME_LENGTH)
			this.name = name;
		ItemType icon = ItemType.load(saved.getCompound("icon"));
		if (!icon.isEmpty()) this.icon = icon;
		this.color = saved.getInt("color") & 0xFFFFFF;
		this.securityMode = PocketSecurityMode.valueOf(saved.getString("securityMode"));
		for (Tag itemCount : saved.getList("itemCounts", 10)) {
			ItemType type = ItemType.load(((CompoundTag) itemCount).getCompound("item"));
			double count = ((CompoundTag)itemCount).getDouble("count");
			if (count <= 0)
				continue;
			items.put(type, count);
		}
		this.clearedItems = false;
	}

	CompoundTag save() {
		CompoundTag saved = new CompoundTag();
		saved.putUUID("pocketId", pocketId);
		saved.putUUID("owner", owner);
		saved.putString("name", name);
		saved.put("icon", icon.save());
		saved.putInt("color", color);
		saved.putString("securityMode", securityMode.name());
		ListTag itemCounts = new ListTag();
		for (var entry : items.entrySet()) {
			CompoundTag itemCount = new CompoundTag();
			itemCount.put("item", entry.getKey().save());
			itemCount.putDouble("count", entry.getValue());
			itemCounts.add(itemCount);
		}
		saved.put("itemCounts", itemCounts);
		return saved;
	}

	@Override public UUID getPocketId() { return pocketId; }
	@Override public UUID getOwner() { return owner; }
	@Override public String getName() { return name; }
	@Override public ItemType getIcon() { return icon; }
	@Override public int getColor() { return color; }
	@Override public PocketSecurityMode getSecurityMode() { return securityMode; }

	@Override
	public void setName(String name) {
		if (name.length() > MAX_NAME_LENGTH)
			name = name.substring(0, MAX_NAME_LENGTH);
		if (this.name.equals(name))
			return;
		this.name = name;
		if (isServer)
			DeepPocketPacketHandler.cbPocketSetName(PacketDistributor.ALL.noArg(), pocketId, name);
	}

	@Override
	public void setIcon(ItemType icon) {
		if (this.icon.equals(icon) || icon.isEmpty())
			return;
		this.icon = icon;
		if (isServer)
			DeepPocketPacketHandler.cbPocketSetIcon(PacketDistributor.ALL.noArg(), pocketId, icon);
	}

	@Override
	public void setColor(int color) {
		if (this.color == color)
			return;
		this.color = color;
		if (isServer)
			DeepPocketPacketHandler.cbPocketSetColor(PacketDistributor.ALL.noArg(), pocketId, color);
	}

	@Override
	public void setSecurityMode(PocketSecurityMode mode) {
		if (this.securityMode == mode)
			return;
		PocketSecurityMode oldMode = this.securityMode;
		this.securityMode = mode;
		if (!isServer)
			return;
		DeepPocketPacketHandler.cbPocketSetSecurity(PacketDistributor.ALL.noArg(), pocketId, mode);
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null)
			return;
		List<Connection> remove = server.getPlayerList().getPlayers().stream().filter(p->oldMode.canAccess(p,getOwner())).filter(p->!mode.canAccess(p,getOwner())).map(p->p.connection.connection).toList();
		List<Connection> add = server.getPlayerList().getPlayers().stream().filter(p->!oldMode.canAccess(p,getOwner())).filter(p->mode.canAccess(p,getOwner())).map(p->p.connection.connection).toList();
		if (!remove.isEmpty())
			DeepPocketPacketHandler.cbPocketClearItems(PacketDistributor.NMLIST.with(()->remove), getPocketId());
		if (!add.isEmpty())
			DeepPocketPacketHandler.cbPocketSetItemCount(PacketDistributor.NMLIST.with(()->add), getPocketId(), getItems());
	}

	@Override
	public boolean canAccess(Player player) {
		return getSecurityMode().canAccess(player, getOwner());
	}

	@Override
	public double getCount(ItemType type) {
		return type.isEmpty() ? 0 : items.getOrDefault(type, 0.0);
	}

	@Override
	public void setCount(ItemType type, double value) {
		if (type.isEmpty())
			return;
		if (value < 0)
			throw new IllegalArgumentException("value");
		boolean changed;
		if (value == 0)
			changed = items.remove(type) != null;
		else
			changed = !Objects.equals(items.put(type, value), value);
		if (changed)
			changedItems.put(type, value);
	}

	@Override
	public void addCount(ItemType type, double value) {
		setCount(type, getCount(type) + value);
	}

	@Override
	public @UnmodifiableView Map<ItemType, Double> getItems() {
		return Collections.unmodifiableMap(items);
	}

	@Override
	public void clearItems() {
		items.clear();
		clearedItems = true;
		changedItems.clear();
	}

	@Override
	public PocketImpl copy() {
		return new PocketImpl(this);
	}

	void sendUpdate() {
		PacketDistributor.PacketTarget target = securityMode.getPacketTarget(owner);
		if (clearedItems) {
			DeepPocketPacketHandler.cbPocketClearItems(target, pocketId);
			clearedItems = false;
		}
		if (!changedItems.isEmpty()) {
			DeepPocketPacketHandler.cbPocketSetItemCount(target, pocketId, changedItems);
			changedItems.clear();
		}
	}
}
