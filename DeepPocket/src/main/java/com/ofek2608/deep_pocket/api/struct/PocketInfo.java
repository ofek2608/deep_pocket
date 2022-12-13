package com.ofek2608.deep_pocket.api.struct;

import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;

public final class PocketInfo {
	public static final int MAX_NAME_LENGTH = 16;
	public @Nonnull String name;
	public @Nonnull ElementType icon;
	public int color;
	public @Nonnull PocketSecurityMode securityMode;

	public PocketInfo(String name, ElementType icon, int color, PocketSecurityMode securityMode) {
		if (name.length() > MAX_NAME_LENGTH)
			throw new IllegalArgumentException();
		this.name = name;
		this.icon = icon.isEmpty() ? ElementType.item(DeepPocketUtils.randomItem()) : icon;
		this.color = color & 0xFFFFFF;
		this.securityMode = securityMode;
	}

	public PocketInfo(PocketInfo copy) {
		this(copy.name, copy.icon, copy.color, copy.securityMode);
	}

	public PocketInfo() {
		this("My new pocket", ElementType.empty(), DeepPocketUtils.randomColor(), PocketSecurityMode.PRIVATE);
	}
	
	public static PocketInfo load(CompoundTag saved) {
		return new PocketInfo(
				saved.getString("name"),
				ElementType.load(saved.getCompound("icon")),
				saved.getInt("color") & 0xFFFFFF,
				PocketSecurityMode.valueOf(saved.getString("securityMode"))
		);
	}

	public static CompoundTag save(PocketInfo info) {
		CompoundTag saved = new CompoundTag();
		saved.putString("name", info.name);
		saved.put("icon", ElementType.save(info.icon));
		saved.putInt("color", info.color);
		saved.putString("securityMode", info.securityMode.name());
		return saved;
	}

	public static void encode(FriendlyByteBuf buf, PocketInfo pocketInfo) {
		buf.writeUtf(pocketInfo.name, MAX_NAME_LENGTH);
		ElementType.encode(buf, pocketInfo.icon);
		buf.writeInt(pocketInfo.color);
		buf.writeEnum(pocketInfo.securityMode);
	}

	public static PocketInfo decode(FriendlyByteBuf buf) {
		return new PocketInfo(buf.readUtf(MAX_NAME_LENGTH), ElementType.decode(buf), buf.readInt(), buf.readEnum(PocketSecurityMode.class));
	}
}
