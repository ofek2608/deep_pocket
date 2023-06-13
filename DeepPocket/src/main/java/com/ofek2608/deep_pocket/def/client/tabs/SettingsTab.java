package com.ofek2608.deep_pocket.def.client.tabs;

import com.ofek2608.deep_pocket.api.DPClientAPI;
import com.ofek2608.deep_pocket.api.implementable.PocketTabDefinition;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import net.minecraft.client.player.LocalPlayer;

import java.util.Objects;

public final class SettingsTab implements PocketTabDefinition<SettingsTab.Data> {
	private SettingsTab() {}
	public static final SettingsTab INSTANCE = new SettingsTab();
	
	
	@Override
	public boolean isVisible(DPClientAPI api, LocalPlayer player, Pocket pocket) {
		return Objects.equals(player.getUUID(), pocket.getProperties().getOwner());
	}
	
	@Override
	public Data onOpen(DPClientAPI api, LocalPlayer player, Pocket pocket) {
		return new Data(api, player, pocket);
	}
	
	@Override
	public int getLeftWidth(Data data) {
		return 50;
	}
	
	@Override
	public int getRightWidth(Data data) {
		return 50;
	}
	
	public static final class Data {
		private final DPClientAPI api;
		private final LocalPlayer player;
		private final Pocket pocket;
		
		
		private Data(DPClientAPI api, LocalPlayer player, Pocket pocket) {
			this.api = api;
			this.player = player;
			this.pocket = pocket;
		}
	}
}
