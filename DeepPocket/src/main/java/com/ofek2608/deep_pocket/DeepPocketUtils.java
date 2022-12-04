package com.ofek2608.deep_pocket;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

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

	public static Predicate<ItemStack> createFilter(String filterText) {
		filterText = filterText.toLowerCase();
		Predicate<ItemStack> resultPredicate = stack->false;
		for (String filterTextPossibility : filterText.split("\\|")) {
			Predicate<ItemStack> possibilityPredicate = stack->true;
			for (String filterTextUnit : filterTextPossibility.split(" ")) {
				boolean negate = filterTextUnit.startsWith("-");
				if (negate) filterTextUnit = filterTextUnit.substring(1);
				boolean mod = filterTextUnit.startsWith("@");
				if (mod) filterTextUnit = filterTextUnit.substring(1);

				if (filterTextUnit.isEmpty()) continue;

				Predicate<ItemStack> unitPredicate = mod ? getModFilter(filterTextUnit) : getNameFilter(filterTextUnit);
				if (negate) unitPredicate = unitPredicate.negate();
				possibilityPredicate = possibilityPredicate.and(unitPredicate);
			}
			resultPredicate = resultPredicate.or(possibilityPredicate);
		}
		return resultPredicate;
	}

	private static Predicate<ItemStack> getNameFilter(String itemName) {
		return (stack)->stack.getDisplayName().getString().toLowerCase().contains(itemName);
	}

	private static Predicate<ItemStack> getModFilter(String modName) {
		return (stack)->getItemMod(stack).toLowerCase().contains(modName);
	}

	private static String getItemMod(ItemStack stack) {
		ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
		return key == null ? ResourceLocation.DEFAULT_NAMESPACE : key.getNamespace();
	}











	public static long advancedSum(long a, long b) {
		if (a < 0 || b < 0) return -1;
		try {
			return Math.addExact(a, b);
		} catch (Exception e) {
			return -1;
		}
	}

	public static long advancedMul(long a, long b) {
		if (a == 0 || b == 0) return 0;
		if (a < 0 || b < 0) return -1;
		try {
			return Math.multiplyExact(a, b);
		} catch (Exception e) {
			return -1;
		}
	}
	
	public static long advancedDiv(long a, long b) {
		if (a == 0) return 0;
		if (a < 0 || b == 0) return -1;
		if (b < 0) return 0;
		return a / b;
	}
	
	public static long advancedMin(long a, long b) {
		if (a < 0) return b < 0 ? -1 : 0;
		if (b < 0) return a;
		return Math.min(a, b);
	}
	
	public static long advancedMax(long a, long b) {
		if (a < 0 || b < 0) return -1;
		return Math.max(a, b);
	}

	private static final String[] ADVANCED_NUMBER_SUFFIXES = {"K","M","B","T","Q"};
	public static String advancedToString(long a) {
		if (a < 0)
			return "Inf";
		if (a < 10000)
			return a + "";
		//a >= 10000;
		a /= 10;
		//a >= 1000;
		for (String suffix : ADVANCED_NUMBER_SUFFIXES) {
			if (a < 1000000) {
				String str = a + "";
				return switch (str.length()) {
					case 3 -> addDot(str, 1);
					case 4 -> addDot(str.substring(0, 4), 2);
					case 5 -> addDot(str.substring(0, 4), 3);
					default -> str.substring(0, 4);
				} + suffix;
			}
			a /= 1000;
		}
		return "Inf";
	}

	private static String addDot(String text, int index) {
		return text.substring(0,index) + "." + text.substring(index);
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
