package com.ofek2608.deep_pocket.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class DeepPocketUtils {
	private DeepPocketUtils() {}

	public static void playClickSound() {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	public static int randomColor() {
		return new Random().nextInt() & 0xFFFFFF;
	}

	private static final Item[] RANDOM_ITEM_PALETTE = {
					Items.STONE,
					Items.DIRT,
					Items.COBBLESTONE,
					Items.DEEPSLATE,
					Items.COAL_BLOCK,
					Items.CRAFTING_TABLE,
					Items.CHEST,
					Items.FURNACE,
					Items.OAK_LOG,
					Items.OAK_PLANKS,

	};
	public static Item randomItem() {
		return RANDOM_ITEM_PALETTE[new Random().nextInt(RANDOM_ITEM_PALETTE.length)];
	}

	public static void setRenderShaderColor(int color) {
		RenderSystem.setShaderColor((color >> 16) / 255F, ((color >> 8) & 0xFF) / 255F, (color & 0xFF) / 255F, 1.0F);
	}

	public static String getTimedTextEditSuffix() {
		return displayTimeSuffix() ? "_" : "";
	}
	
	public static boolean displayTimeSuffix() {
		return System.currentTimeMillis() % 2000 < 1000;
	}

	public static Predicate<ElementType> createFilter(String filterText) {
		filterText = filterText.toLowerCase();
		Predicate<ElementType> resultPredicate = stack->false;
		for (String filterTextPossibility : filterText.split("\\|")) {
			Predicate<ElementType> possibilityPredicate = stack->true;
			for (String filterTextUnit : filterTextPossibility.split(" ")) {
				boolean negate = filterTextUnit.startsWith("-");
				if (negate) filterTextUnit = filterTextUnit.substring(1);
				boolean mod = filterTextUnit.startsWith("@");
				if (mod) filterTextUnit = filterTextUnit.substring(1);

				if (filterTextUnit.isEmpty()) continue;

				Predicate<ElementType> unitPredicate = mod ? getModFilter(filterTextUnit) : getNameFilter(filterTextUnit);
				if (negate) unitPredicate = unitPredicate.negate();
				possibilityPredicate = possibilityPredicate.and(unitPredicate);
			}
			resultPredicate = resultPredicate.or(possibilityPredicate);
		}
		return resultPredicate;
	}

	private static Predicate<ElementType> getNameFilter(String itemName) {
		return (type)->type.getDisplayName().getString().toLowerCase().contains(itemName);
	}

	private static Predicate<ElementType> getModFilter(String modName) {
		return (type)->type.getKey().getNamespace().toLowerCase().contains(modName);
	}








	public static <T> T[] loadArray(ListTag list, IntFunction<T[]> arrayFactory, Function<CompoundTag,T> loader) {
		return list.stream()
						.map(elem -> elem instanceof CompoundTag tag ? tag : null)
						.filter(Objects::nonNull)
						.map(loader)
						.toArray(arrayFactory);
	}

	public static <T> ListTag saveArray(T[] array, Function<T,CompoundTag> saver) {
		ListTag saved = new ListTag();
		Stream.of(array)
						.map(saver)
						.forEach(saved::add);
		return saved;
	}



	@SuppressWarnings("deprecation")
	public static void encodeItem(FriendlyByteBuf buf, Item item) {
		buf.writeId(Registry.ITEM, item);
	}

	@SuppressWarnings("deprecation")
	public static Item decodeItem(FriendlyByteBuf buf) {
		Item item = buf.readById(Registry.ITEM);
		return item == null ? Items.AIR : item;
	}

	public static <T> T[] decodeArray(FriendlyByteBuf buf, IntFunction<T[]> arrayFactory, Function<FriendlyByteBuf,T> decoder) {
		int length = buf.readVarInt();
		T[] result = arrayFactory.apply(length);
		for (int i = 0; i < length; i++)
			result[i] = decoder.apply(buf);
		return result;
	}

	public static <T> void encodeArray(FriendlyByteBuf buf, T[] array, BiConsumer<FriendlyByteBuf,T> encoder) {
		buf.writeVarInt(array.length);
		for (T t : array)
			encoder.accept(buf, t);
	}

	public static int sqrt(int integer) {
		int result = (int)Math.sqrt(integer);
		while (result * result < integer)
			result++;
		while (result * result > integer)
			result--;
		return result;
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
	}
}
