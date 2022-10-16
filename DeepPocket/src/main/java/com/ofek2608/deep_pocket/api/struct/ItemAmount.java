package com.ofek2608.deep_pocket.api.struct;

import com.ofek2608.deep_pocket.DeepPocketUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public final class ItemAmount {
	private final Item item;
	private final long amount;

	public ItemAmount(Item item, long amount) {
		this.item = item;
		this.amount = amount;
	}

	public ItemAmount(CompoundTag saved) {
		this.item = getItem(new ResourceLocation(saved.getString("item")));
		this.amount = saved.getLong("amount");
	}

	public CompoundTag save() {
		CompoundTag saved = new CompoundTag();
		ResourceLocation itemLoc = ForgeRegistries.ITEMS.getKey(item);
		if (itemLoc != null)
			saved.putString("item", itemLoc.toString());
		saved.putLong("amount", amount);
		return saved;
	}

	public static void encode(FriendlyByteBuf buf, ItemAmount itemAmount) {
		DeepPocketUtils.encodeItem(buf, itemAmount.item);
		buf.writeLong(itemAmount.amount);
	}

	public static ItemAmount decode(FriendlyByteBuf buf) {
		return new ItemAmount(DeepPocketUtils.decodeItem(buf), buf.readLong());
	}

	public boolean isEmpty() {
		return item == Items.AIR || amount == 0;
	}

	public boolean isInfinite() {
		return item != Items.AIR && amount < 0;
	}

	public Item getItem() {
		return amount == 0 ? Items.AIR : item;
	}

	public long getAmount() {
		return item == Items.AIR ? 0 : amount < 0 ? -1 : amount;
	}







	private static Item getItem(ResourceLocation loc) {
		Item item = ForgeRegistries.ITEMS.getValue(loc);
		return item == null ? Items.AIR : item;
	}
}
