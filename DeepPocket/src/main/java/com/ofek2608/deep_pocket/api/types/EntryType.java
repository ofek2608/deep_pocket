package com.ofek2608.deep_pocket.api.types;

import com.ofek2608.deep_pocket.DeepPocketMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record EntryType(ResourceLocation category, ResourceLocation id, @Nullable CompoundTag tag) {
	public EntryType(ResourceLocation category, ResourceLocation id) {
		this(category, id, null);
	}
	
	public static final ResourceLocation CATEGORY_EMPTY = DeepPocketMod.loc("empty");
	public static final ResourceLocation CATEGORY_ENERGY = DeepPocketMod.loc("energy");
	public static final ResourceLocation CATEGORY_ITEM = DeepPocketMod.loc("item");
	public static final ResourceLocation CATEGORY_FLUID = DeepPocketMod.loc("fluid");
	public static final ResourceLocation CATEGORY_GENERIC = DeepPocketMod.loc("generic");
	
	
	public static final EntryType EMPTY = new EntryType(CATEGORY_EMPTY, CATEGORY_EMPTY, null);
	public static final EntryType ENERGY = new EntryType(CATEGORY_ENERGY, CATEGORY_ENERGY, null);
	
	public static EntryType load(CompoundTag data) {
		return new EntryType(
				new ResourceLocation(data.getString("category")),
				new ResourceLocation(data.getString("id")),
				data.contains("tag") ? data.getCompound("tag") : null
		);
	}
	
	public static CompoundTag save(EntryType type) {
		CompoundTag result = new CompoundTag();
		result.putString("category", type.category.toString());
		result.putString("id", type.id.toString());
		if (type.tag != null) {
			result.put("tag", type.tag);
		}
		return result;
	}
	
	public static void encode(FriendlyByteBuf buf, EntryType entryType) {
		buf.writeResourceLocation(entryType.category);
		buf.writeResourceLocation(entryType.id);
		buf.writeNbt(entryType.tag);
	}
	
	public static EntryType decode(FriendlyByteBuf buf) {
		return new EntryType(
				buf.readResourceLocation(),
				buf.readResourceLocation(),
				buf.readNbt()
		);
	}
}
