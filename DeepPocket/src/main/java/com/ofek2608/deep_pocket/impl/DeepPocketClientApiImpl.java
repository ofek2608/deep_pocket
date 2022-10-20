package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.api.*;
import com.ofek2608.deep_pocket.api.enums.PocketDisplayMode;
import com.ofek2608.deep_pocket.api.enums.SearchMode;
import com.ofek2608.deep_pocket.api.enums.SortingOrder;
import com.ofek2608.deep_pocket.api.events.DeepPocketItemConversionsUpdatedEvent;
import com.ofek2608.deep_pocket.api.struct.ItemConversions;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

class DeepPocketClientApiImpl extends DeepPocketApiImpl<DeepPocketHelper> implements DeepPocketClientApi {
	private final Minecraft minecraft;
	private PlayerKnowledge knowledge = DeepPocketManager.getHelper().createKnowledge(conversions);
	private boolean permitPublicPocket;

	DeepPocketClientApiImpl(DeepPocketClientHelper helper) {
		super(helper);
		this.minecraft = helper.getMinecraft();
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
	public PlayerKnowledge getKnowledge() {
		return knowledge;
	}

	@Override
	public Stream<ItemTypeAmount> getSortedKnowledge(Pocket pocket) {
		Map<ItemType,Long> counts = new HashMap<>();
		for (ItemType type : getKnowledge().getPocketItems(pocket).toList()) {
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
