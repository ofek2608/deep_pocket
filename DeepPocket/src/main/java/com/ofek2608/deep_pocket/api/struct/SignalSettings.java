package com.ofek2608.deep_pocket.api.struct;

import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public final class SignalSettings {
	public @Nonnull ItemType first;
	public boolean bigger;
	public @Nullable ItemType secondItem;
	public long secondCount;

	public SignalSettings() {
		this(ItemType.EMPTY, false, 0L);
	}

	public SignalSettings(ItemType first, boolean bigger, ItemType second) {
		this(first, bigger, second, 0);
	}

	public SignalSettings(ItemType first, boolean bigger, long second) {
		this(first, bigger, null, second);
	}

	public SignalSettings(SignalSettings copy) {
		this(copy.first, copy.bigger, copy.secondItem, copy.secondCount);
	}

	private SignalSettings(ItemType first, boolean bigger, @Nullable ItemType secondItem, long secondCount) {
		this.first = first;
		this.bigger = bigger;
		this.secondItem = secondItem;
		this.secondCount = secondCount;
	}

	public boolean check(UUID pocketId) {
		if (first.isEmpty() || secondItem != null && secondItem.isEmpty())
			return false;
		DeepPocketServerApi api = DeepPocketServerApi.get();
		if (api == null) return false;
		Pocket pocket = api.getPocket(pocketId);
		if (pocket == null) return false;
		long firstNum = pocket.getMaxExtractOld(null, first);
		long secondNum = secondItem == null ? secondCount : pocket.getMaxExtractOld(null, secondItem);
		if (bigger) {
			long temp = firstNum;
			firstNum = secondNum;
			secondNum = temp;
		}
		//check if firstNum < secondNum
		if (firstNum < 0) return false;
		if (secondNum < 0) return true;
		return firstNum < secondNum;
	}

	public void load(CompoundTag tag) {
		first = tag.contains("first", 10) ? ItemType.load(tag.getCompound("first")) : ItemType.EMPTY;
		bigger = tag.getBoolean("bigger");
		secondItem = tag.contains("second", 10) ? ItemType.load(tag.getCompound("second")) : null;
		secondCount = tag.contains("second", 99) ? tag.getLong("second") : 0;
	}

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.put("first", first.save());
		tag.putBoolean("bigger", bigger);
		if (secondItem == null)
			tag.putLong("second", secondCount);
		else
			tag.put("second", secondItem.save());
		return tag;
	}


	public static void encode(FriendlyByteBuf buf, SignalSettings settings) {
		boolean hasSecondItem = settings.secondItem != null;
		buf.writeBoolean(hasSecondItem);
		ItemType.encode(buf, settings.first);
		buf.writeBoolean(settings.bigger);
		if (hasSecondItem)
			ItemType.encode(buf, settings.secondItem);
		else
			buf.writeLong(settings.secondCount);
	}

	public static SignalSettings decode(FriendlyByteBuf buf) {
		return buf.readBoolean() ?
						new SignalSettings(ItemType.decode(buf), buf.readBoolean(), ItemType.decode(buf)) :
						new SignalSettings(ItemType.decode(buf), buf.readBoolean(), buf.readLong());
	}

}
