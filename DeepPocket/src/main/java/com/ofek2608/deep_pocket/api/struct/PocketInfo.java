package com.ofek2608.deep_pocket.api.struct;

import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;

public class PocketInfo {
	public static final int MAX_NAME_LENGTH = 16;
	public @Nonnull String name;
	public @Nonnull ItemType icon;
	public int color;
	public @Nonnull PocketSecurityMode securityMode;

	public PocketInfo(String name, ItemType icon, int color, PocketSecurityMode securityMode) {
		if (name.length() > MAX_NAME_LENGTH)
			throw new IllegalArgumentException();
		this.name = name;
		this.icon = icon;
		this.color = color & 0xFFFFFF;
		this.securityMode = securityMode;
	}

	public PocketInfo(PocketInfo copy) {
		this(copy.name, copy.icon, copy.color, copy.securityMode);
	}

	public PocketInfo() {
		this("My new pocket", new ItemType(DeepPocketUtils.randomItem(), null), DeepPocketUtils.randomColor(), PocketSecurityMode.PRIVATE);
	}

	public PocketInfo(CompoundTag saved) {
		this.name = saved.getString("name");
		this.icon = ItemType.load(saved.getCompound("icon"));
		this.color = saved.getInt("color") & 0xFFFFFF;
		this.securityMode = PocketSecurityMode.valueOf(saved.getString("securityMode"));
		if (name.length() > MAX_NAME_LENGTH || icon.isEmpty())
			throw new IllegalArgumentException();
	}

	public CompoundTag save() {
		CompoundTag saved = new CompoundTag();
		saved.putString("name", name);
		saved.put("icon", icon.save());
		saved.putInt("color", color);
		saved.putString("securityMode", securityMode.name());
		return saved;
	}

	public static void encode(FriendlyByteBuf buf, PocketInfo pocketInfo) {
		buf.writeUtf(pocketInfo.name, MAX_NAME_LENGTH);
		ItemType.encode(buf, pocketInfo.icon);
		buf.writeInt(pocketInfo.color);
		buf.writeEnum(pocketInfo.securityMode);
	}

	public static PocketInfo decode(FriendlyByteBuf buf) {
		return new PocketInfo(buf.readUtf(MAX_NAME_LENGTH), ItemType.decode(buf), buf.readInt(), buf.readEnum(PocketSecurityMode.class));
	}
}
