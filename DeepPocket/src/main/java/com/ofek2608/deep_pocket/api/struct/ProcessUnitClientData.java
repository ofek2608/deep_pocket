package com.ofek2608.deep_pocket.api.struct;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class ProcessUnitClientData {
	public static final ProcessUnitClientData EMPTY = new ProcessUnitClientData(0, 1, Collections.emptyList(), Collections.emptyList());
	public final int pageIndex;
	public final int pageCount;
	public final @UnmodifiableView List<CraftingItem> craftingItems;
	public final @UnmodifiableView List<IngredientItem> ingredientItems;

	public ProcessUnitClientData(int pageIndex, int pageCount, List<CraftingItem> craftingItems, List<IngredientItem> ingredientItems) {
		this.pageIndex = pageIndex;
		this.pageCount = pageCount;
		this.craftingItems = List.copyOf(craftingItems);
		this.ingredientItems = List.copyOf(ingredientItems);
	}


	public static void encode(FriendlyByteBuf buf, ProcessUnitClientData data) {
		buf.writeVarInt(data.pageIndex);
		buf.writeVarInt(data.pageCount);
		buf.writeCollection(data.craftingItems, CraftingItem::encode);
		buf.writeCollection(data.ingredientItems, IngredientItem::encode);
	}

	public static ProcessUnitClientData decode(FriendlyByteBuf buf) {
		return new ProcessUnitClientData(
						buf.readVarInt(),
						buf.readVarInt(),
						buf.readList(CraftingItem::decode),
						buf.readList(IngredientItem::decode)
		);
	}

	public static final class CraftingItem {
		public final ItemType item;
		public final long leftToCraft;
		public final UUID recipeId;

		public CraftingItem(ItemType item, long leftToCraft, UUID recipeId) {
			this.item = item;
			this.leftToCraft = leftToCraft;
			this.recipeId = recipeId;
		}


		public static void encode(FriendlyByteBuf buf, CraftingItem data) {
			ItemType.encode(buf, data.item);
			buf.writeLong(data.leftToCraft);
			buf.writeUUID(data.recipeId);
		}

		public static CraftingItem decode(FriendlyByteBuf buf) {
			return new CraftingItem(
							ItemType.decode(buf),
							buf.readLong(),
							buf.readUUID()
			);
		}
	}

	public static final class IngredientItem {
		public final ElementType type;
		public final long required;

		public IngredientItem(ElementType type, long required) {
			this.type = type;
			this.required = required;
		}


		public static void encode(FriendlyByteBuf buf, IngredientItem data) {
			ElementType.encode(buf, data.type);
			buf.writeLong(data.required);
		}

		public static IngredientItem decode(FriendlyByteBuf buf) {
			return new IngredientItem(
					ElementType.decode(buf),
					buf.readLong()
			);
		}
	}
}
