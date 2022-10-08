package com.ofek2608.deep_pocket.api;

import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.ItemValue;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.UnmodifiableView;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public interface DeepPocketApi {
	public static @Nullable DeepPocketApi get(boolean isClientSide) { return isClientSide ? DeepPocketClientApi.get() : DeepPocketServerApi.get(); }
	public static @Nullable DeepPocketApi get(Level level) { return get(level.isClientSide); }

	@Nullable
	ItemValue getItemValue(ItemType type);
	@UnmodifiableView Map<ItemType,ItemValue> getItemValues();
	void setItemValue(ItemType type, @Nullable ItemValue value);//!!Do not call setItemValue if you are working with client api unless you know what you are doing!!
	void clearItemValues();//!!Do not call clearItemValues if you are working with client api unless you know what you are doing!!

	@UnmodifiableView Map<UUID, Pocket> getPockets();
	@Nullable
	Pocket getPocket(UUID pocketId);
	@Nullable
	Pocket createPocket(UUID pocketId, UUID owner);
	Pocket getOrCreatePocket(UUID pocketId, UUID owner);
	void destroyPocket(UUID pocketId);
	void clearPockets();

	boolean cachePlayerName(UUID id, String name);
	String getCachedPlayerName(UUID id);
	@UnmodifiableView Map<UUID, String> getPlayerNameCache();

	void insertItem(Pocket pocket, ItemStack stack);
	ItemStack extractItem(Pocket pocket, ItemStack stack);
	double getMaxExtract(Pocket pocket, ItemType type);




}
