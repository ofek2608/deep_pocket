package com.ofek2608.deep_pocket.api.struct;

import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.enums.NumberRelation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public final class SignalSettings {
	public @Nonnull ItemType first;
	public @Nonnull NumberRelation operator;
	public @Nullable ItemType secondItem;
	public double secondCount;

	public SignalSettings() {
		first = ItemType.EMPTY;
		operator = NumberRelation.BT;
		secondItem = null;
		secondCount = 0;
	}

	public SignalSettings(ItemType first, NumberRelation operator, ItemType second) {
		this(first, operator, second, 0);
	}

	public SignalSettings(ItemType first, NumberRelation operator, double second) {
		this(first, operator, null, second);
	}

	public SignalSettings(SignalSettings copy) {
		this(copy.first, copy.operator, copy.secondItem, copy.secondCount);
	}

	private SignalSettings(ItemType first, NumberRelation operator, @Nullable ItemType secondItem, double secondCount) {
		this.first = first;
		this.operator = operator;
		this.secondItem = secondItem;
		this.secondCount = secondCount;
	}

	public boolean check(UUID pocketId) {
		DeepPocketServerApi api = DeepPocketServerApi.get();
		if (api == null) return false;
		Pocket pocket = api.getPocket(pocketId);
		if (pocket == null) return false;
		double firstNum = api.getMaxExtract(pocket, first);
		double secondNum = secondItem == null ? secondCount : api.getMaxExtract(pocket, secondItem);
		return operator.check(firstNum, secondNum);
	}

	public void load(CompoundTag tag) {
		first = tag.contains("first", 10) ? ItemType.load(tag.getCompound("first")) : ItemType.EMPTY;
		try {
			operator = NumberRelation.valueOf(tag.getString("operator"));
		} catch (Exception e) {
			operator = NumberRelation.BT;
		}
		secondItem = tag.contains("second", 10) ? ItemType.load(tag.getCompound("second")) : null;
		secondCount = tag.contains("second", 99) ? tag.getDouble("second") : 0;
	}

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.put("first", first.save());
		tag.putString("operator", operator.name());
		if (secondItem == null)
			tag.putDouble("second", secondCount);
		else
			tag.put("second", secondItem.save());
		return tag;
	}


	public static void encode(FriendlyByteBuf buf, SignalSettings settings) {
		boolean hasSecondItem = settings.secondItem != null;
		buf.writeBoolean(hasSecondItem);
		ItemType.encode(buf, settings.first);
		buf.writeEnum(settings.operator);
		if (hasSecondItem)
			ItemType.encode(buf, settings.secondItem);
		else
			buf.writeDouble(settings.secondCount);
	}

	public static SignalSettings decode(FriendlyByteBuf buf) {
		return buf.readBoolean() ?
						new SignalSettings(ItemType.decode(buf), buf.readEnum(NumberRelation.class), ItemType.decode(buf)) :
						new SignalSettings(ItemType.decode(buf), buf.readEnum(NumberRelation.class), buf.readDouble());
	}

}
