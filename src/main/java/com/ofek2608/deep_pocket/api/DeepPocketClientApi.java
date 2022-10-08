package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.api.enums.SearchMode;
import com.ofek2608.deep_pocket.api.enums.SortingOrder;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.SignalSettings;
import com.ofek2608.deep_pocket.impl.DeepPocketManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface DeepPocketClientApi extends DeepPocketApi {
	static DeepPocketClientApi get() { return DeepPocketManager.getClientApi(); }
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
	Stream<Map.Entry<ItemType,Double>> getSortedKnowledge(Pocket pocket);

	void openScreenSettings(@Nullable UUID pocketId, String name, ItemType icon, int color, PocketSecurityMode securityMode);
	void openScreenSettingsNew();
	void openScreenSettingsEdit(Pocket pocket);
	void openScreenSelectPocket();
	void openScreenSelectItem(Component title, int color, Consumer<ItemStack> onSelect, Runnable onCancel);
	void openScreenConfigureSignalBlock(int color, BlockPos pos, SignalSettings settings);
}
