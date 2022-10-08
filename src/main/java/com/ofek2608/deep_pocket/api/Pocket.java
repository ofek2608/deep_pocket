package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.enums.PocketSecurityMode;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;
import java.util.UUID;

public interface Pocket {
	int MAX_NAME_LENGTH = 16;

	UUID getPocketId();
	UUID getOwner();
	String getName();
	ItemType getIcon();
	int getColor();
	PocketSecurityMode getSecurityMode();
	void setName(String name);
	void setIcon(ItemType icon);
	void setColor(int color);
	void setSecurityMode(PocketSecurityMode mode);

	boolean canAccess(Player player);

	double getCount(ItemType type);
	void setCount(ItemType type, double value);
	void addCount(ItemType type, double value);
	@UnmodifiableView Map<ItemType,Double> getItems();
	void clearItems();

	Pocket copy();
}
