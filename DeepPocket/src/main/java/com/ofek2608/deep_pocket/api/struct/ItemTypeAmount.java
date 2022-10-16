package com.ofek2608.deep_pocket.api.struct;

import com.ofek2608.deep_pocket.DeepPocketUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public final class ItemTypeAmount {
	private final ItemType itemType;
	private final long amount;

	public ItemTypeAmount(ItemType itemType, long amount) {
		this.itemType = itemType;
		this.amount = amount;
	}

	public ItemTypeAmount(CompoundTag saved) {
		this.itemType = ItemType.load(saved.getCompound("itemType"));
		this.amount = saved.getLong("amount");
	}

	public CompoundTag save() {
		CompoundTag saved = new CompoundTag();
		saved.put("itemType", itemType.save());
		saved.putLong("amount", amount);
		return saved;
	}

	public static void encode(FriendlyByteBuf buf, ItemTypeAmount itemTypeAmount) {
		ItemType.encode(buf, itemTypeAmount.itemType);
		buf.writeLong(itemTypeAmount.amount);
	}

	public static ItemTypeAmount decode(FriendlyByteBuf buf) {
		return new ItemTypeAmount(ItemType.decode(buf), buf.readLong());
	}

	public boolean isEmpty() {
		return itemType.isEmpty() || amount == 0;
	}

	public boolean isInfinite() {
		return !itemType.isEmpty() && amount < 0;
	}

	public ItemType getItemType() {
		return amount == 0 ? ItemType.EMPTY : itemType;
	}

	public long getAmount() {
		return itemType.isEmpty() ? 0 : amount < 0 ? -1 : amount;
	}







	private static Item getItem(ResourceLocation loc) {
		Item item = ForgeRegistries.ITEMS.getValue(loc);
		return item == null ? Items.AIR : item;
	}
}
