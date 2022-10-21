package com.ofek2608.deep_pocket.api.struct;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public final class ItemTypeAmount {
	private final ItemType itemType;
	private final long amount;

	public ItemTypeAmount(ItemType itemType, long amount) {
		this.itemType = itemType;
		this.amount = amount;
	}

	public ItemTypeAmount(CompoundTag saved) {
		this.itemType = ItemType.load(saved);
		this.amount = saved.getLong("amount");
	}

	public CompoundTag save() {
		CompoundTag saved = itemType.save();
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
		return itemType;
	}

	public long getAmount() {
		return itemType.isEmpty() ? 0 : amount < 0 ? -1 : amount;
	}

	@Override
	public boolean equals(Object o) {
		return this == o || o instanceof ItemTypeAmount that &&
						this.amount == that.amount &&
						itemType.equals(that.itemType);
	}

	@Override
	public int hashCode() {
		return 31 * itemType.hashCode() + (int) (amount ^ (amount >>> 32));
	}

	@Override
	public String toString() {
		return amount + " x " + itemType;
	}
}
