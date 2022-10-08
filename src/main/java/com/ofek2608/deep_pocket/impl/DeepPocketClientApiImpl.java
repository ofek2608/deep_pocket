package com.ofek2608.deep_pocket.impl;

import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.api.enums.SearchMode;
import com.ofek2608.deep_pocket.api.enums.SortingOrder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

class DeepPocketClientApiImpl extends DeepPocketApiImpl implements DeepPocketClientApi {
	private final PlayerKnowledgeImpl knowledge = new PlayerKnowledgeImpl();
	private boolean permitPublicPocket;

	DeepPocketClientApiImpl() {}
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
	public PlayerKnowledgeImpl getKnowledge() {
		return knowledge;
	}

	@Override
	public Stream<Map.Entry<ItemType,Double>> getSortedKnowledge(Pocket pocket) {
		Map<ItemType,Double> counts = new HashMap<>();
		for (ItemType type : getKnowledge().asSet()) {
			double count = pocket.getCount(type);
			if (count > 0)
				counts.put(type, count);
		}
		var comparator = getSortingOrder().comparator;
		if (!isSortAscending())
			comparator = comparator.reversed();
		return counts.entrySet().stream().sorted(comparator);
	}

	@Override
	PocketImpl generatePocket(UUID pocketId, UUID owner) {
		return new PocketImpl(pocketId, owner, false);
	}

	@Override
	public void openScreenSettings(@Nullable UUID pocketId, String name, ItemType icon, int color, PocketSecurityMode securityMode) {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.setScreen(new PocketSettingsScreen(minecraft.screen, pocketId, name, icon, color, securityMode));
	}

	@Override
	public void openScreenSettingsNew() {
		openScreenSettings(null, "My new pocket", new ItemType(DeepPocketUtils.randomItem(), null), DeepPocketUtils.randomColor(), PocketSecurityMode.PRIVATE);
	}

	@Override
	public void openScreenSettingsEdit(Pocket pocket) {
		openScreenSettings(pocket.getPocketId(), pocket.getName(), pocket.getIcon(), pocket.getColor(), pocket.getSecurityMode());
	}

	@Override
	public void openScreenSelectPocket() {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player != null)
			minecraft.setScreen(new PocketSelectionScreen(minecraft.screen, player));
	}

	@Override
	public void openScreenSelectItem(Component title, int color, Consumer<ItemStack> onSelect, Runnable onCancel) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player != null)
			minecraft.setScreen(new ItemSelectionScreen(title, color, player.getInventory(), onSelect, onCancel));
	}
}
