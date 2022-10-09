package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.enums.SearchMode;
import com.ofek2608.deep_pocket.api.enums.SortingOrder;
import com.ofek2608.deep_pocket.api.struct.ItemConversions;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.PlayerKnowledge;
import com.ofek2608.deep_pocket.api.struct.Pocket;
import net.minecraft.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

class DeepPocketClientApiImpl extends DeepPocketApiImpl implements DeepPocketClientApi {
	private PlayerKnowledge knowledge = new PlayerKnowledge(conversions, Util.NIL_UUID);
	private boolean permitPublicPocket;

	DeepPocketClientApiImpl() {}

	@Override
	public void setItemConversions(ItemConversions conversions) {
		this.conversions = conversions;
		this.knowledge = new PlayerKnowledge(conversions, Util.NIL_UUID);
	}

	//Client config
	@Override public SearchMode getSearchMode() { return DeepPocketConfig.Client.SEARCH_MODE.get(); }
	@Override public void setSearchMode(SearchMode searchMode) { DeepPocketConfig.Client.SEARCH_MODE.set(searchMode); }
	@Override public SortingOrder getSortingOrder() { return DeepPocketConfig.Client.SORTING_ORDER.get(); }
	@Override public void setSortingOrder(SortingOrder order) { DeepPocketConfig.Client.SORTING_ORDER.set(order); }
	@Override public boolean isSortAscending() { return DeepPocketConfig.Client.SORT_ASCENDING.get(); }
	@Override public void setSortAscending(boolean sortAscending) { DeepPocketConfig.Client.SORT_ASCENDING.set(sortAscending); }
	@Override public boolean isDisplayCrafting() { return DeepPocketConfig.Client.DISPLAY_CRAFTING.get(); }
	@Override public void setDisplayCrafting(boolean displayCrafting) { DeepPocketConfig.Client.DISPLAY_CRAFTING.set(displayCrafting); }
	//Server config
	@Override public boolean isPermitPublicPocket() { return permitPublicPocket; }
	@Override public void setPermitPublicPocket(boolean value) { this.permitPublicPocket = value; }

	@Override
	public PlayerKnowledge getKnowledge() {
		return knowledge;
	}

	@Override
	public Stream<Map.Entry<ItemType,Long>> getSortedKnowledge(Pocket pocket) {
		Map<ItemType,Long> counts = new HashMap<>();
		for (ItemType type : getKnowledge().getPocketItems(pocket).toList()) {
			long count = pocket.getMaxExtract(type);
			if (count > 0)
				counts.put(type, count);
			else if (count < 0)
				counts.put(type, -1L);
		}
		var comparator = getSortingOrder().comparator;
		if (!isSortAscending())
			comparator = comparator.reversed();
		return counts.entrySet().stream().sorted(comparator);
	}
}
