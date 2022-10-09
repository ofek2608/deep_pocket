package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.enums.SearchMode;
import com.ofek2608.deep_pocket.api.enums.SortingOrder;
import com.ofek2608.deep_pocket.api.struct.ItemConversions;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.Pocket;
import com.ofek2608.deep_pocket.impl.DeepPocketManager;

import java.util.Map;
import java.util.stream.Stream;

public interface DeepPocketClientApi extends DeepPocketApi {
	static DeepPocketClientApi get() { return DeepPocketManager.getClientApi(); }
	void setItemConversions(ItemConversions conversions);
	//client config
	SearchMode getSearchMode();
	void setSearchMode(SearchMode searchMode);
	SortingOrder getSortingOrder();
	void setSortingOrder(SortingOrder order);
	boolean isSortAscending();
	void setSortAscending(boolean sortAscending);
	boolean isDisplayCrafting();
	void setDisplayCrafting(boolean displayCrafting);
	//server config
	boolean isPermitPublicPocket();
	void setPermitPublicPocket(boolean value);

	PlayerKnowledge getKnowledge();
	Stream<Map.Entry<ItemType,Long>> getSortedKnowledge(Pocket pocket);
}
