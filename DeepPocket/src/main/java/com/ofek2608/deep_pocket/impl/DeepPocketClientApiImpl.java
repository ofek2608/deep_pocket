package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import com.ofek2608.deep_pocket.api.Knowledge;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.enums.PocketDisplayMode;
import com.ofek2608.deep_pocket.api.enums.SearchMode;
import com.ofek2608.deep_pocket.api.enums.SortingOrder;
import com.ofek2608.deep_pocket.api.events.DeepPocketItemConversionsUpdatedEvent;
import com.ofek2608.deep_pocket.api.struct.ItemConversions;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

final class DeepPocketClientApiImpl extends DeepPocketApiImpl<DeepPocketClientHelper> implements DeepPocketClientApi {
	private Knowledge knowledge = DeepPocketManager.getHelper().createKnowledge(conversions);
	private boolean permitPublicPocket;

	DeepPocketClientApiImpl(DeepPocketClientHelper helper) {
		super(helper);
	}

	@Override
	public void setItemConversions(ItemConversions conversions) {
		this.conversions = conversions;
		this.knowledge = DeepPocketManager.getHelper().createKnowledge(conversions);
		MinecraftForge.EVENT_BUS.post(new DeepPocketItemConversionsUpdatedEvent(this, conversions));
	}

	//Client config
	@Override public SearchMode getSearchMode() { return DeepPocketConfig.Client.SEARCH_MODE.get(); }
	@Override public void setSearchMode(SearchMode searchMode) { DeepPocketConfig.Client.SEARCH_MODE.set(searchMode); }
	@Override public SortingOrder getSortingOrder() { return DeepPocketConfig.Client.SORTING_ORDER.get(); }
	@Override public void setSortingOrder(SortingOrder order) { DeepPocketConfig.Client.SORTING_ORDER.set(order); }
	@Override public boolean isSortAscending() { return DeepPocketConfig.Client.SORT_ASCENDING.get(); }
	@Override public void setSortAscending(boolean sortAscending) { DeepPocketConfig.Client.SORT_ASCENDING.set(sortAscending); }
	@Override public PocketDisplayMode getPocketDisplayMode() { return DeepPocketConfig.Client.POCKET_DISPLAY_MODE.get(); }
	@Override public void setPocketDisplayMode(PocketDisplayMode pocketDisplayMode) { DeepPocketConfig.Client.POCKET_DISPLAY_MODE.set(pocketDisplayMode); }
	//Server config
	@Override public boolean isPermitPublicPocket() { return permitPublicPocket; }
	@Override public void setPermitPublicPocket(boolean value) { this.permitPublicPocket = value; }

	@Override
	public Knowledge getKnowledge() {
		return knowledge;
	}

	@Override
	public Stream<ItemTypeAmount> getSortedKnowledge(Pocket pocket) {
		Map<ItemType,Long> counts = new HashMap<>();
		for (ItemType type : helper.getExtractableItems(pocket, getKnowledge()).toList()) {
			long count = pocket.getMaxExtract(getKnowledge(), type);
			if (count > 0)
				counts.put(type, count);
			else if (count < 0)
				counts.put(type, -1L);
		}
		var comparator = getSortingOrder().comparator;
		if (!isSortAscending())
			comparator = comparator.reversed();
		return counts.entrySet()
						.stream()
						.map(entry->new ItemTypeAmount(entry.getKey(), entry.getValue()))
						.sorted(comparator);
	}
}
