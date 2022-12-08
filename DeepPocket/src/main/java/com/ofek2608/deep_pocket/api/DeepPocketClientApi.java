package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.enums.PocketDisplayMode;
import com.ofek2608.deep_pocket.api.enums.SearchMode;
import com.ofek2608.deep_pocket.api.enums.SortingOrder;
import com.ofek2608.deep_pocket.api.struct.*;
import com.ofek2608.deep_pocket.impl.DeepPocketManager;

import java.util.stream.Stream;

public interface DeepPocketClientApi extends DeepPocketApi {
	static DeepPocketClientApi get() { return DeepPocketManager.getClientApi(); }


	@Override DeepPocketClientHelper getHelper();

	void setItemConversions(ItemConversions conversions);
	//client config
	@Deprecated(forRemoval = true) SearchMode getSearchMode();
	@Deprecated(forRemoval = true) void setSearchMode(SearchMode searchMode);
	@Deprecated(forRemoval = true) SortingOrder getSortingOrder();
	@Deprecated(forRemoval = true) void setSortingOrder(SortingOrder order);
	@Deprecated(forRemoval = true) boolean isSortAscending();
	@Deprecated(forRemoval = true) void setSortAscending(boolean sortAscending);
	@Deprecated(forRemoval = true) PocketDisplayMode getPocketDisplayMode();
	@Deprecated(forRemoval = true) void setPocketDisplayMode(PocketDisplayMode pocketDisplayMode);
	//server config
	boolean isPermitPublicPocket();
	void setPermitPublicPocket(boolean value);

	Knowledge0 getKnowledge0();
	Knowledge getKnowledge();
	@Deprecated(forRemoval = true) Stream<ItemTypeAmount> getSortedKnowledge(Pocket pocket);
	Stream<Pocket.Entry> getSortedKnowledge0(Pocket pocket);
}
