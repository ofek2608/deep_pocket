package com.ofek2608.deep_pocket.api.struct;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
public final class ItemType {
	public static final ItemType EMPTY = new ItemType(Items.AIR, null);
	private final @Nonnull Item item;
	private final @Nonnull CompoundTag tag;

	public ItemType(Item item, @Nullable CompoundTag tag) {
		this.item = item;
		this.tag = tag == null ? new CompoundTag() : tag.copy();
	}

	public ItemType(Item item) {
		this(item, null);
	}

	public ItemType(ItemStack stack) {
		this(stack.getItem(), stack.getTag());
	}

	public Item getItem() {
		return item;
	}

	public boolean isEmpty() {
		return item == Items.AIR;
	}

	public CompoundTag getTag() {
		return tag.copy();
	}

	public ItemStack create(int count) {
		if (count <= 0)
			return ItemStack.EMPTY;
		ItemStack newStack = new ItemStack(item, count);
		if (!tag.isEmpty())
			newStack.setTag(tag.copy());
		return newStack;
	}

	public ItemStack create() {
		return create(1);
	}

	public boolean is(ItemStack stack) {
		return isEmpty() && stack.isEmpty() ||
						item != stack.getItem() && (stack.hasTag() ? tag.equals(stack.getTag()) : tag.isEmpty());
	}

	public CompoundTag save() {
		return create().save(new CompoundTag());
	}

	public static ItemType load(CompoundTag saved) {
		ItemStack item = ItemStack.of(saved);
		return item.isEmpty() ? EMPTY : new ItemType(item);
	}

	@Override
	public boolean equals(Object o) {
		return this == o || o instanceof ItemType that && this.item == that.item && this.tag.equals(that.tag);
	}

	@Override
	public int hashCode() {
		return 31 * item.hashCode() + tag.hashCode();
	}

	@Override
	public String toString() {
		return "" + item + tag;
	}




	public static void encode(FriendlyByteBuf buf, ItemType type) {
		//noinspection deprecation
		buf.writeId(Registry.ITEM, type.getItem());
		buf.writeNbt(type.getTag());
	}

	public static ItemType decode(FriendlyByteBuf buf) {
		//noinspection deprecation
		Item item = buf.readById(Registry.ITEM);
		CompoundTag tag = buf.readNbt();
		if (item == null)
			throw new RuntimeException("DeepPocket: received illegal item.");
		return new ItemType(item, tag == null ? new CompoundTag() : tag);
	}
}
