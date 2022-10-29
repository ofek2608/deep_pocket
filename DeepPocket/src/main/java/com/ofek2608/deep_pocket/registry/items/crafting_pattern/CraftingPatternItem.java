package com.ofek2608.deep_pocket.registry.items.crafting_pattern;

import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import com.ofek2608.deep_pocket.registry.DeepPocketBEWLR;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

	public static ItemStack createItem(ItemTypeAmount[] input, ItemTypeAmount[] output) {
		ItemStack newPattern = new ItemStack(DeepPocketRegistry.CRAFTING_PATTERN_ITEM.get());
		CompoundTag tag = new CompoundTag();
		tag.put("patternInput", saveParam(input));
		tag.put("patternOutput", saveParam(output));
		newPattern.setTag(tag);
		return newPattern;
	}

	public static ItemTypeAmount[] retrieveInput(ItemStack stack) {
		return retrieveInput(stack.getTag());
	}

	public static ItemTypeAmount[] retrieveInput(@Nullable CompoundTag itemTag) {
		return retrieveParam(itemTag, "patternInput");
	}

	public static ItemTypeAmount[] retrieveOutput(ItemStack stack) {
		return retrieveOutput(stack.getTag());
	}

	public static ItemTypeAmount[] retrieveOutput(@Nullable CompoundTag itemTag) {
		return retrieveParam(itemTag, "patternOutput");
	}

	private static Tag saveParam(ItemTypeAmount[] value) {
		ListTag saved = new ListTag();
		Stream.of(value).map(ItemTypeAmount::save).forEach(saved::add);
		return saved;
	}

	private static ItemTypeAmount[] retrieveParam(@Nullable CompoundTag itemTag, String name) {
		if (itemTag != null) {
			ListTag inputTag = itemTag.getList(name, 10);
			ItemTypeAmount[] value = inputTag.stream()
							.map(tag->tag instanceof CompoundTag compoundTag ? compoundTag : null)
							.filter(Objects::nonNull)
							.map(ItemTypeAmount::new)
							.toArray(ItemTypeAmount[]::new);
			if (value.length > 0)
				return value;
		}
		return new ItemTypeAmount[] {new ItemTypeAmount(ItemType.EMPTY, 0)};
	}
}
