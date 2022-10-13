package com.ofek2608.deep_pocket_conversions.registry;

import com.ofek2608.deep_pocket_conversions.DPCMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MatterItem extends Item {
	public static final int MIN_MATTER_NUM = 1;
	public static final int MAX_MATTER_NUM = 22;
	public final int num;
	public final long value;

	public MatterItem(Properties properties, int num) {
		super(properties);
		if (num < MIN_MATTER_NUM || MAX_MATTER_NUM < num)
			throw new IllegalArgumentException("num");
		this.num = num;
		this.value = num == MAX_MATTER_NUM ? -1 : 1L << ((num - 1) * 3);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> text, TooltipFlag isAdvanced) {
		if (num == MIN_MATTER_NUM) {
			text.add(Component.translatable("tooltip." + DPCMod.ID + ".matter_value", "1").withStyle(ChatFormatting.YELLOW));
			text.add(Component.translatable("tooltip." + DPCMod.ID + ".matter_1").withStyle(ChatFormatting.YELLOW));
			return;
		}
		if (num == MAX_MATTER_NUM) {
			text.add(Component.translatable("tooltip." + DPCMod.ID + ".matter_inf").withStyle(ChatFormatting.YELLOW));
			return;
		}
		text.add(Component.translatable("tooltip." + DPCMod.ID + ".matter_i", num - 1).withStyle(ChatFormatting.YELLOW));
	}
}
