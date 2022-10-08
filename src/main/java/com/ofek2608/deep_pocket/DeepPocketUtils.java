package com.ofek2608.deep_pocket;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;
import java.util.function.Predicate;

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
		return System.currentTimeMillis() % 2000 < 1000 ? "_" : "";
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

	private static final String[] NUMBER_SUFFIXES = {
					"","K","M","B","T","Q",
					"aa","ab","ac","ad","ae","af","ag","ah","ai","aj","ak","al","am","an","ao","ap","aq","ar","as","at","au","av","aw","ax","ay","az",
					"ba","bb","bc","bd","be","bf","bg","bh","bi","bj","bk","bl","bm","bn","bo","bp","bq","br","bs","bt","bu","bv","bw","bx","by","bz",
					"ca","cb","cc","cd","ce","cf","cg","ch","ci","cj","ck","cl","cm","cn","co","cp","cq","cr","cs","ct","cu","cv","cw","cx","cy","cz",
					"da","db","dc","dd","de","df","dg","dh","di","dj","dk","dl","dm","dn","do","dp","dq","dr","ds"
	};

	public static String bigNumberToString(double num) {
		if (Double.isNaN(num))
			return "NaN";

		String prefix;
		if (num < 0) {
			prefix = "-";
			num = -num;
		} else {
			prefix = "";
		}

		if (Double.isInfinite(num))
			return prefix + "Inf";

		{
			int numInt = (int)num;
			if (numInt < 1000 && (double)numInt == num)
				return prefix + numInt;
		}

		for (String suffix : NUMBER_SUFFIXES) {
			if (num < 1000) {
				String str = "" + (int)(num * 100);
				return prefix + switch (str.length()) {
					case 1 -> "0.0" + str;
					case 2 -> "0." + str;
					case 3 -> addDot(str, 1);
					case 4 -> addDot(str.substring(0,3), 2);
					case 5 -> str.substring(0,3);
					default -> "999";
				} + suffix;
			}
			num /= 1000;
		}
		return prefix + "Inf";
	}

	private static String addDot(String text, int index) {
		return text.substring(0,index) + "." + text.substring(index);
	}
}
