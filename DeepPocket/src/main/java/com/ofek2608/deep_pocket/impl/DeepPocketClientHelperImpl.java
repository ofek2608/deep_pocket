package com.ofek2608.deep_pocket.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.enums.PocketDisplayFilter;
import com.ofek2608.deep_pocket.api.enums.PocketDisplayMode;
import com.ofek2608.deep_pocket.api.enums.SearchMode;
import com.ofek2608.deep_pocket.api.enums.SortingOrder;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.ElementTypeStack;
import com.ofek2608.deep_pocket.api.struct.ItemAmount;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import com.ofek2608.deep_pocket.integration.DeepPocketJEI;
import com.ofek2608.deep_pocket.utils.AdvancedLongMath;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Predicate;

final class DeepPocketClientHelperImpl extends DeepPocketHelperImpl implements DeepPocketClientHelper {
	private final Minecraft minecraft;
	private String lastJeiSearch = null;

	DeepPocketClientHelperImpl() {
		this.minecraft = Minecraft.getInstance();
	}

	@Override
	public Minecraft getMinecraft() {
		return minecraft;
	}



	@Override
	public void renderItem(PoseStack poseStack, int x, int y, ItemStack itemStack, ItemRenderer itemRenderer, Font font) {
		if (itemStack.isEmpty())
			return;
		itemRenderer.renderAndDecorateItem(itemStack, x, y);
		itemRenderer.renderGuiItemDecorations(font, itemStack, x, y, null);
	}

	@Override
	public void renderAmount(PoseStack poseStack, int x, int y, String amount, ItemRenderer itemRenderer, Font font) {
		poseStack.pushPose();
		poseStack.translate(0.0D, 0.0D, itemRenderer.blitOffset + 200);
		poseStack.scale(0.5f, 0.5f, 1f);
		font.draw(poseStack, amount, x * 2 + 32 - font.width(amount), y * 2 + 24, 0xFFFFFF);
		poseStack.popPose();
	}

	@Override
	public void renderAmount(PoseStack poseStack, int x, int y, long amount, ItemRenderer itemRenderer, Font font) {
		if (amount != 1)
			renderAmount(poseStack, x, y, AdvancedLongMath.advancedToString(amount, 6), itemRenderer, font);
	}

	@Override
	public void renderItemAmount(PoseStack poseStack, int x, int y, ItemStack itemStack, long amount, ItemRenderer itemRenderer, Font font) {
		if (itemStack.isEmpty() || amount == 0)
			return;
		renderItem(poseStack, x, y, itemStack, itemRenderer, font);
		renderAmount(poseStack, x, y, amount, itemRenderer, font);
	}

	@Override
	public void renderItemAmount(PoseStack poseStack, int x, int y, ItemAmount itemAmount, ItemRenderer itemRenderer, Font font) {
		renderItemAmount(poseStack, x, y, new ItemStack(itemAmount.getItem()), itemAmount.getAmount(), itemRenderer, font);
	}

	@Override
	public void renderItemAmount(PoseStack poseStack, int x, int y, ItemTypeAmount itemAmount, ItemRenderer itemRenderer, Font font) {
		renderItemAmount(poseStack, x, y, itemAmount.getItemType().create(), itemAmount.getAmount(), itemRenderer, font);
	}
	
	@Override
	public void renderElementType(PoseStack poseStack, int x, int y, ElementType type, ItemRenderer itemRenderer, Font font) {
		if (type instanceof ElementType.TItem item) {
			renderItem(poseStack, x, y, item.create(), itemRenderer, font);
		}
		//TODO more types
	}
	
	public void renderPocketEntry(PoseStack poseStack, int x, int y, Pocket.Entry entry, @Nullable String amount, ItemRenderer itemRenderer, Font font) {
		renderElementType(poseStack, x, y, entry.getType(), itemRenderer, font);
		if (amount == null)
			renderAmount(poseStack, x, y, entry.getMaxExtract(), itemRenderer, font);
		else
			renderAmount(poseStack, x, y, amount, itemRenderer, font);
	}
	
	@Override
	public void renderElementTypeStack(PoseStack poseStack, int x, int y, ElementTypeStack stack, ItemRenderer itemRenderer, Font font) {
		if (stack.isEmpty())
			return;
		renderElementType(poseStack, x, y, stack.getType(), itemRenderer, font);
		renderAmount(poseStack, x, y, stack.getCount(), itemRenderer, font);
	}
	
	@Override
	public void renderElementTypeTooltip(PoseStack poseStack, int x, int y, ElementTypeStack stack, Screen screen) {
		if (stack.isEmpty())
			return;
		ElementType type = stack.getType();
		if (type instanceof ElementType.TItem item)
			screen.renderTooltip(poseStack, item.create(), x, y);
		else
			screen.renderTooltip(poseStack, type.getDisplayName(), x, y);
	}
	
	@Override
	public void renderElementTypeTooltip(PoseStack poseStack, int x, int y, Pocket.Entry entry, Screen screen) {
		renderElementTypeTooltip(poseStack, x, y, ElementTypeStack.of(entry.getType(), entry.getMaxExtract()), screen);
	}
	
	@Override
	public String getSearch() {
		String jeiSearch = DeepPocketJEI.getSearch();
		if (!Objects.equals(jeiSearch, lastJeiSearch)) {
			lastJeiSearch = jeiSearch;
			if (jeiSearch != null && getSearchMode().syncFrom) {
				DeepPocketConfig.Client.SEARCH.set(jeiSearch);
				return jeiSearch;
			}
		}
		return DeepPocketConfig.Client.SEARCH.get();
	}
	
	@Override
	public void setSearch(String search) {
		if (search.equals(DeepPocketConfig.Client.SEARCH.get()))
			return;
		DeepPocketConfig.Client.SEARCH.set(search);
		if (getSearchMode().syncTo)
			DeepPocketJEI.setSearch(search);
	}
	
	@Override public SearchMode getSearchMode() { return DeepPocketConfig.Client.SEARCH_MODE.get(); }
	@Override public void setSearchMode(SearchMode searchMode) { DeepPocketConfig.Client.SEARCH_MODE.set(searchMode); }
	@Override public SortingOrder getSortingOrder() { return DeepPocketConfig.Client.SORTING_ORDER.get(); }
	@Override public void setSortingOrder(SortingOrder order) { DeepPocketConfig.Client.SORTING_ORDER.set(order); }
	@Override public boolean isSortAscending() { return DeepPocketConfig.Client.SORT_ASCENDING.get(); }
	@Override public void setSortAscending(boolean sortAscending) { DeepPocketConfig.Client.SORT_ASCENDING.set(sortAscending); }
	@Override public PocketDisplayFilter getPocketDisplayFilter() { return DeepPocketConfig.Client.POCKET_DISPLAY_FILTER.get(); }
	@Override public void setPocketDisplayFilter(PocketDisplayFilter pocketDisplayFilter) { DeepPocketConfig.Client.POCKET_DISPLAY_FILTER.set(pocketDisplayFilter); }
	@Override public PocketDisplayMode getPocketDisplayMode() { return DeepPocketConfig.Client.POCKET_DISPLAY_MODE.get(); }
	@Override public void setPocketDisplayMode(PocketDisplayMode pocketDisplayMode) { DeepPocketConfig.Client.POCKET_DISPLAY_MODE.set(pocketDisplayMode); }
	
	
	
	@Override
	public Comparator<Pocket.Entry> getSearchComparator() {
		Comparator<Pocket.Entry> comparator = getSortingOrder();
		return isSortAscending() ? comparator : comparator.reversed();
	}
	
	@Override
	public Predicate<Pocket.Entry> getSearchFilter() {
		Predicate<ElementType> searchFilter = DeepPocketUtils.createFilter(getSearch());
		PocketDisplayFilter displayFilter = getPocketDisplayFilter();
		return entry -> {
			ElementType type = entry.getType();
			return (type instanceof ElementType.TItem && displayFilter.displayItems ||
					type instanceof ElementType.TFluid && displayFilter.displayFluids) &&
					searchFilter.test(type);
		};
	}
}
