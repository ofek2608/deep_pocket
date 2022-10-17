package com.ofek2608.deep_pocket.registry.items.crafting_pattern;

import com.ofek2608.deep_pocket.api.struct.ItemAmount;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import com.ofek2608.deep_pocket.registry.DeepPocketBEWLR;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CraftingPatternItem extends Item {
	public CraftingPatternItem(Properties properties) {
		super(properties);
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		return Optional.of(new CraftingPatternTooltip(retrieveInput(stack), retrieveOutput(stack)));
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return DeepPocketBEWLR.INSTANCE;
			}
		});
		super.initializeClient(consumer);
	}

	private static final Map<ItemStack,ItemStack> DISPLAYED_RESULT_CACHE = new HashMap<>();


	public static ItemStack getCachedDisplayedResult(ItemStack stack) {
		if (DISPLAYED_RESULT_CACHE.size() > 65536)
			DISPLAYED_RESULT_CACHE.clear();
		return DISPLAYED_RESULT_CACHE.computeIfAbsent(stack, CraftingPatternItem::getDisplayedResult);
	}

	private static ItemStack getDisplayedResult(ItemStack stack) {
		ItemTypeAmount[] outputs = CraftingPatternItem.retrieveOutput(stack);
		for (ItemTypeAmount output : outputs)
			if (!output.isEmpty())
				return output.getItemType().create();
		return ItemStack.EMPTY;
	}

	public static ItemStack createItem(ItemAmount[] input, ItemTypeAmount[] output) {
		ItemStack newPattern = new ItemStack(DeepPocketRegistry.CRAFTING_PATTERN_ITEM.get());
		CompoundTag tag = new CompoundTag();

		ListTag inputTag = new ListTag();
		Stream.of(input).map(ItemAmount::save).forEach(inputTag::add);
		tag.put("patternInput", inputTag);

		ListTag outputTag = new ListTag();
		Stream.of(output).map(ItemTypeAmount::save).forEach(outputTag::add);
		tag.put("patternOutput", outputTag);

		newPattern.setTag(tag);
		return newPattern;
	}

	public static ItemAmount[] retrieveInput(@Nullable CompoundTag itemTag) {
		if (itemTag != null) {
			ListTag inputTag = itemTag.getList("patternInput", 10);
			ItemAmount[] input = inputTag.stream()
							.map(tag->tag instanceof CompoundTag compoundTag ? compoundTag : null)
							.filter(Objects::nonNull)
							.map(ItemAmount::new)
							.toArray(ItemAmount[]::new);
			if (input.length > 0)
				return input;
		}
		return new ItemAmount[] {new ItemAmount(Items.AIR, 0)};
	}

	public static ItemAmount[] retrieveInput(ItemStack stack) {
		return retrieveInput(stack.getTag());
	}

	public static ItemTypeAmount[] retrieveOutput(@Nullable CompoundTag itemTag) {
		if (itemTag != null) {
			ListTag inputTag = itemTag.getList("patternOutput", 10);
			ItemTypeAmount[] input = inputTag.stream()
							.map(tag->tag instanceof CompoundTag compoundTag ? compoundTag : null)
							.filter(Objects::nonNull)
							.map(ItemTypeAmount::new)
							.toArray(ItemTypeAmount[]::new);
			if (input.length > 0)
				return input;
		}
		return new ItemTypeAmount[] {new ItemTypeAmount(ItemType.EMPTY, 0)};
	}

	public static ItemTypeAmount[] retrieveOutput(ItemStack stack) {
		return retrieveOutput(stack.getTag());
	}
}
