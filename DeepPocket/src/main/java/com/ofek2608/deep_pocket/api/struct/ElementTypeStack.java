package com.ofek2608.deep_pocket.api.struct;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;

public final class ElementTypeStack {
	private static final ElementTypeStack EMPTY = new ElementTypeStack(ElementType.empty(), 0);
	
	public static ElementTypeStack empty() {
		return EMPTY;
	}
	
	public static ElementTypeStack of(ElementType type, long count) {
		return type.isEmpty() || count == 0 ? EMPTY : new ElementTypeStack(type, count < 0 ? -1 : count);
	}
	
	public static ElementTypeStack of(ElementType type) {
		return type.isEmpty() ? EMPTY : new ElementTypeStack(type, 1);
	}
	
	private final ElementType type;
	private final long count;
	
	private ElementTypeStack(ElementType type, long count) {
		this.type = type;
		this.count = count;
	}
	
	public long getCount() {
		return count;
	}
	
	public ElementType getType() {
		return type;
	}
	
	public boolean isEmpty() {
		return this == EMPTY;
	}
	
	public static CompoundTag save(ElementTypeStack stack) {
		CompoundTag result = ElementType.save(stack.type);
		result.putLong("Count", stack.count);
		return result;
	}
	
	public static ElementTypeStack load(CompoundTag saved) {
		ElementType type = ElementType.load(saved);
		if (type.isEmpty())
			return empty();
		long count = saved.getLong("Count");
		return count == 0 ? of(type) : of(type, count);
	}
	
	public static void encode(FriendlyByteBuf buf, ElementTypeStack stack) {
		ElementType.encode(buf, stack.type);
		if (!stack.isEmpty())
			buf.writeVarLong(stack.count < 0 ? 0L : stack.count);
	}
	
	public static ElementTypeStack decode(FriendlyByteBuf buf) {
		ElementType type = ElementType.decode(buf);
		if (type.isEmpty())
			return empty();
		long count = buf.readVarLong();
		return of(type, count <= 0 ? -1 : count);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ElementTypeStack that)) return false;
		return count == that.count && type.equals(that.type);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(type, count);
	}
}
