package com.ofek2608.deep_pocket.api.struct;

import com.ofek2608.deep_pocket.DeepPocketMod;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Represents a type of element which can be in the pocket.
 * @see ElementType.TItem
 * @see ElementType.TFluid
 * @see TConvertible
 * @see ElementType.TEnergy
 * @see ElementType.TEmpty
 */
public sealed abstract class ElementType {
	/**
	 * @return the instance of TEmpty
	 * @see ElementType.TEmpty
	 */
	public static TEmpty empty() {
		return TEmpty.INSTANCE;
	}
	
	/**
	 * @return the instance of TEnergy
	 * @see TEnergy
	 */
	public static TEnergy energy() {
		return TEnergy.INSTANCE;
	}
	
	/**
	 * Generates a new TConvertible
	 * @param key the key of the convertible, when registered
	 * @return the new TConvertible
	 * @see TConvertible
	 */
	public static TConvertible convertible(ResourceLocation key) {
		return new TConvertible(key);
	}
	
	/**
	 * Creates a new TItem
	 * @param item the raw item
	 * @return a new TItem or Empty if {@code item == Items.AIR}
	 * @see ElementType.TItem
	 * @see ElementType.TEmpty
	 */
	public static ElementType item(Item item) {
		if (item == Items.AIR)
			return TEmpty.INSTANCE;
		//Create new ItemStack to get required tags
		return new TItem(item, new ItemStack(item).getTag());
	}
	
	/**
	 * Creates a new TItem with optional tag
	 * @param stack the raw item stack
	 * @return a new TItem or TEmpty if the {@code stack.isEmpty()}
	 * @see ElementType.TItem
	 * @see ElementType.TEmpty
	 */
	public static ElementType item(ItemStack stack) {
		if (stack.getCount() <= 0)
			return TEmpty.INSTANCE;
		return item(stack.getItem(), stack.getTag());
	}
	
	
	/**
	 * Creates a new TItem with optional tag
	 * @param item the raw item
	 * @param tag the tag (will be copied)
	 * @return a new TItem or TEmpty if the {@code item == Items.AIR}
	 * @see ElementType.TItem
	 * @see ElementType.TEmpty
	 */
	public static ElementType item(Item item, @Nullable CompoundTag tag) {
		if (item == Items.AIR)
			return TEmpty.INSTANCE;
		return new TItem(item, tag == null ? null : tag.copy());
	}
	
	/**
	 * Creates a new TFluid
	 * @param fluid the raw fluid
	 * @return a new TFluid or TEmpty if the {@code fluid == Fluids.EMPTY}
	 * @see ElementType.TFluid
	 * @see ElementType.TEmpty
	 */
	public static ElementType fluid(Fluid fluid) {
		if (fluid == Fluids.EMPTY)
			return TEmpty.INSTANCE;
		return new TFluid(fluid, null);
	}
	
	/**
	 * Creates a new TFluid with optional tag
	 * @param stack the raw fluid stack
	 * @return a new TFluid or TEmpty if the {@code stack.isEmpty()}
	 * @see ElementType.TFluid
	 * @see ElementType.TEmpty
	 */
	public static ElementType fluid(FluidStack stack) {
		return fluid(stack.getFluid(), stack.getTag());
	}
	
	/**
	 * Creates a new TFluid with optional tag
	 * @param fluid the raw fluid
	 * @param tag the fluid (will be copied)
	 * @return a new TFluid or TEmpty if the {@code fluid == Fluids.EMPTY}
	 * @see ElementType.TFluid
	 * @see ElementType.TEmpty
	 */
	public static ElementType fluid(Fluid fluid, @Nullable CompoundTag tag) {
		if (fluid == Fluids.EMPTY)
			return TEmpty.INSTANCE;
		return new TFluid(fluid, tag == null ? null : tag.copy());
	}
	
	/**
	 * Saves an ElementType into CompoundTag
	 * @param type the type
	 * @return the saved ElementType
	 * @see ElementType#load(CompoundTag)
	 */
	public static CompoundTag save(ElementType type) {
		CompoundTag saved = new CompoundTag();
		saved.putString("class", type.getElementClassName());
		saved.putString("key", type.getKey().toString());
		if (type instanceof TaggedType taggedType) {
			CompoundTag tag = taggedType.getTag();
			if (tag != null)
				saved.put("tag", tag);
		}
		return saved;
	}
	
	/**
	 * Loads an ElementType from CompoundTag
	 * @param saved the saved ElementType
	 * @return the loaded ElementType
	 * @see ElementType#save(ElementType)
	 */
	public static ElementType load(CompoundTag saved) {
		String elementClassName = saved.getString("class");
		ResourceLocation elementKey;
		try {
			elementKey = new ResourceLocation(saved.getString("key"));
		} catch (ResourceLocationException exception) {
			return empty();
		}
		CompoundTag elementTag = saved.contains("tag") ? saved.getCompound("tag") : null;
		switch (elementClassName) {
			case TEmpty.ELEMENT_CLASS_NAME -> { return empty(); }
			case TEnergy.ELEMENT_CLASS_NAME -> { return energy(); }
			case TConvertible.ELEMENT_CLASS_NAME -> { return convertible(elementKey); }
			case TItem.ELEMENT_CLASS_NAME -> {
				Item item = ForgeRegistries.ITEMS.getValue(elementKey);
				if (item != null)
					return item(item, elementTag);
			}
			case TFluid.ELEMENT_CLASS_NAME -> {
				Fluid fluid = ForgeRegistries.FLUIDS.getValue(elementKey);
				if (fluid != null)
					return fluid(fluid, elementTag);
			}
		}
		return new TUnknown(elementClassName, elementKey, elementTag);
	}
	
	/**
	 * Writes an ElementType into a FriendlyByteBuf
	 * @param buf the buffer
	 * @param type the type to write
	 * @see ElementType#decode(FriendlyByteBuf)
	 */
	@SuppressWarnings("deprecation")
	public static void encode(FriendlyByteBuf buf, ElementType type) {
		if (type instanceof TEmpty) {
			buf.writeVarInt(0);
		} else if (type instanceof TEnergy) {
			buf.writeVarInt(1);
		} else if (type instanceof TConvertible) {
			buf.writeVarInt(2);
			buf.writeResourceLocation(type.getKey());
		} else if (type instanceof TItem item) {
			buf.writeVarInt(3);
			buf.writeId(Registry.ITEM, item.getItem());
			buf.writeNbt(item.getTag());
		} else if (type instanceof TFluid fluid) {
			buf.writeVarInt(4);
			buf.writeId(Registry.FLUID, fluid.getFluid());
			buf.writeNbt(fluid.getTag());
		} else if (type instanceof TUnknown unknown) {
			buf.writeVarInt(5);
			buf.writeUtf(type.getElementClassName());
			buf.writeResourceLocation(type.getKey());
			buf.writeNbt(unknown.getTag());
		} else {
			throw new UnsupportedOperationException("Unexpected element type " + type);
		}
	}
	
	/**
	 * Reads an ElementType from a FriendlyByteBuf and returns it.
	 * May throw exceptions.
	 * @param buf the buffer
	 * @return The read ElementType.
	 * @see ElementType#encode(FriendlyByteBuf, ElementType)
	 */
	@SuppressWarnings("deprecation")
	public static ElementType decode(FriendlyByteBuf buf) {
		return switch (buf.readVarInt()) {
			case 0 -> empty();
			case 1 -> energy();
			case 2 -> convertible(buf.readResourceLocation());
			case 3 -> item(Objects.requireNonNull(buf.readById(Registry.ITEM)), buf.readNbt());
			case 4 -> fluid(Objects.requireNonNull(buf.readById(Registry.FLUID)), buf.readNbt());
			case 5 -> new TUnknown(buf.readUtf(), buf.readResourceLocation(), buf.readNbt());
			default -> throw new IllegalStateException("Received illegal ElementTypeId");
		};
	}
	
	
	
	/**
	 * Same as {@code element == empty()} or {@code element instanceof TEmpty}
	 * @return weather the element is the empty element.
	 * @see ElementType.TEmpty
	 */
	public boolean isEmpty() { return this == TEmpty.INSTANCE; }
	
	/**
	 * @return a string representation of the element class
	 * @see TEmpty#ELEMENT_CLASS_NAME
	 * @see TEnergy#ELEMENT_CLASS_NAME
	 * @see TConvertible#ELEMENT_CLASS_NAME
	 * @see TItem#ELEMENT_CLASS_NAME
	 * @see TFluid#ELEMENT_CLASS_NAME
	 */
	public abstract String getElementClassName();
	
	/**
	 * @return the key of the element, something like "minecraft:stone"
	 */
	public abstract ResourceLocation getKey();
	
	
	/**
	 * An abstract representation of an element type with tag.
	 * @see ElementType.TItem
	 * @see ElementType.TFluid
	 * @see ElementType.TUnknown
	 */
	public static sealed abstract class TaggedType extends ElementType {
		private final @Nullable CompoundTag tag;
		
		protected TaggedType(@Nullable CompoundTag tag) {
			this.tag = tag;
		}
		
		@Override
		public boolean equals(Object o) {
			return o instanceof TaggedType that && Objects.equals(tag, that.tag);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(tag);
		}
		
		/**
		 * @return weather the tag is null, same as {@code element.getTag() != null}
		 */
		public boolean hasTag() {
			return tag != null;
		}
		
		/**
		 * @return a copy of the element's tag
		 */
		public @Nullable CompoundTag getTag() {
			return tag == null ? null : tag.copy();
		}
		
		protected String getTagString() {
			return tag == null ? "" : tag.toString();
		}
	}
	
	
	/**
	 * Represents an empty ElementType (could be items, fluids or anything)
	 * It has only one instance, so equality can be checked using {@code x == y}
	 * @see ElementType#empty()
	 */
	public static final class TEmpty extends ElementType {
		public static final String ELEMENT_CLASS_NAME = "empty";
		private static final ResourceLocation KEY = DeepPocketMod.loc("empty");
		private static final TEmpty INSTANCE = new TEmpty();
		private TEmpty() {}
		
		@Override
		public int hashCode() {
			return 0;
		}
		
		@Override
		public String toString() {
			return "ElementType.TEmpty";
		}
		
		/**
		 * @see ElementType#getElementClassName()
		 */
		@Override
		public String getElementClassName() {
			return ELEMENT_CLASS_NAME;
		}
		
		/**
		 * @see ElementType#getKey()
		 */
		@Override
		public ResourceLocation getKey() {
			return KEY;
		}
	}
	
	/**
	 * Represents an ElementType of Energy (forge energy)
	 * It has only one instance, so equality can be checked using {@code x == y}
	 * @see ElementType#energy()
	 */
	public static final class TEnergy extends ElementType {
		public static final String ELEMENT_CLASS_NAME = "energy";
		private static final ResourceLocation KEY = DeepPocketMod.loc("energy");
		private static final TEnergy INSTANCE = new TEnergy();
		private TEnergy() {}
		
		@Override
		public int hashCode() {
			return 1;
		}
		
		@Override
		public String toString() {
			return "ElementType.TEnergy";
		}
		
		/**
		 * @see ElementType#getElementClassName()
		 */
		@Override
		public String getElementClassName() {
			return ELEMENT_CLASS_NAME;
		}
		
		/**
		 * @see ElementType#getKey()
		 */
		@Override
		public ResourceLocation getKey() {
			return KEY;
		}
	}
	
	/**
	 * Represents an ElementType of convertible
	 * @see ElementType#convertible(ResourceLocation)
	 */
	public static final class TConvertible extends ElementType {
		public static final String ELEMENT_CLASS_NAME = "convertible";
		private final ResourceLocation key;
		
		private TConvertible(ResourceLocation key) {
			this.key = key;
		}
		
		@Override
		public boolean equals(Object o) {
			return this == o || o instanceof TConvertible that && this.key.equals(that.key);
		}
		
		@Override
		public int hashCode() {
			return key.hashCode();
		}
		
		@Override
		public String toString() {
			return "ElementType.TConvertible[" + key + "]";
		}
		
		/**
		 * @see ElementType#getElementClassName()
		 */
		@Override
		public String getElementClassName() {
			return ELEMENT_CLASS_NAME;
		}
		
		/**
		 * @see ElementType#getKey()
		 */
		@Override
		public ResourceLocation getKey() {
			return key;
		}
	}
	
	/**
	 * Represents an ElementType of corrupted type (for example removed item)
	 * this object can only be created while decoding or loading ElementType
	 */
	public static final class TUnknown extends TaggedType {
		private final String type;
		private final ResourceLocation key;
		
		private TUnknown(String type, ResourceLocation key, @Nullable CompoundTag tag) {
			super(tag);
			this.type = type;
			this.key = key;
		}
		
		@Override
		public boolean equals(Object o) {
			return this == o || o instanceof TUnknown that &&
					super.equals(that) &&
					this.type.equals(that.type) &&
					this.key.equals(that.key);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), type, key);
		}
		
		@Override
		public String toString() {
			return "ElementType.TUnknown[" + type + ";" + key + getTagString() + "]";
		}
		
		/**
		 * @see ElementType#getElementClassName()
		 */
		@Override
		public String getElementClassName() {
			return type;
		}
		
		/**
		 * @see ElementType#getKey()
		 */
		@Override
		public ResourceLocation getKey() {
			return key;
		}
	}
	
	/**
	 * Represents an ElementType of ItemStack
	 * @see ElementType#item
	 */
	public static final class TItem extends TaggedType {
		public static final String ELEMENT_CLASS_NAME = "item";
		private final Item item;
		
		TItem(Item item, @Nullable CompoundTag tag) {
			super(tag);
			this.item = item;
		}
		
		
		
		@Override
		public boolean equals(Object o) {
			return this == o || o instanceof TItem that &&
					super.equals(that) &&
					this.item == that.item;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), item);
		}
		
		@Override
		public String toString() {
			return "ElementType.TItem[" + getKey() + getTagString() + "]";
		}
		
		/**
		 * @return the item of this element, will never return {@code Items.AIR}
		 */
		public Item getItem() {
			return item;
		}
		
		/**
		 * @see ElementType#getElementClassName()
		 */
		@Override
		public String getElementClassName() {
			return ELEMENT_CLASS_NAME;
		}
		
		/**
		 * @see ElementType#getKey()
		 */
		@Override
		public ResourceLocation getKey() {
			return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item));
		}
	}
	
	/**
	 * Represents an ElementType of FluidStack
	 * @see ElementType#fluid
	 */
	public static final class TFluid extends TaggedType {
		public static final String ELEMENT_CLASS_NAME = "fluid";
		private final Fluid fluid;
		
		TFluid(Fluid fluid, @Nullable CompoundTag tag) {
			super(tag);
			this.fluid = fluid;
		}
		
		@Override
		public boolean equals(Object o) {
			return this == o || o instanceof TFluid that &&
					super.equals(that) &&
					this.fluid == that.fluid;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), fluid);
		}
		
		@Override
		public String toString() {
			return "ElementType.TFluid[" + getKey() + getTagString() + "]";
		}
		
		/**
		 * @return the fluid of this element, will never return {@code Fluids.EMPTY}
		 */
		public Fluid getFluid() {
			return fluid;
		}
		
		/**
		 * @see ElementType#getElementClassName()
		 */
		@Override
		public String getElementClassName() {
			return ELEMENT_CLASS_NAME;
		}
		
		/**
		 * @see ElementType#getKey()
		 */
		@Override
		public ResourceLocation getKey() {
			return Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluid));
		}
	}
}
