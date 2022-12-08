package com.ofek2608.deep_pocket.api;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.api.enums.PocketDisplayFilter;
import com.ofek2608.deep_pocket.api.enums.PocketDisplayMode;
import com.ofek2608.deep_pocket.api.enums.SearchMode;
import com.ofek2608.deep_pocket.api.enums.SortingOrder;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.ElementTypeStack;
import com.ofek2608.deep_pocket.api.struct.ItemAmount;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import com.ofek2608.deep_pocket.impl.DeepPocketManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface DeepPocketClientHelper extends DeepPocketHelper {
	static DeepPocketClientHelper get() { return DeepPocketManager.getClientHelper(); }

	Minecraft getMinecraft();
	
	//===========
	// Rendering
	//===========
	void renderItem(PoseStack poseStack, int x, int y, ItemStack itemStack, ItemRenderer itemRenderer, Font font);
	void renderAmount(PoseStack poseStack, int x, int y, String amount, ItemRenderer itemRenderer, Font font);
	void renderAmount(PoseStack poseStack, int x, int y, long amount, ItemRenderer itemRenderer, Font font);
	void renderItemAmount(PoseStack poseStack, int x, int y, ItemStack itemStack, long amount, ItemRenderer itemRenderer, Font font);
	void renderItemAmount(PoseStack poseStack, int x, int y, ItemAmount itemAmount, ItemRenderer itemRenderer, Font font);
	void renderItemAmount(PoseStack poseStack, int x, int y, ItemTypeAmount itemAmount, ItemRenderer itemRenderer, Font font);
	
	void renderElementType(PoseStack poseStack, int x, int y, ElementType type, ItemRenderer itemRenderer, Font font);
	void renderPocketEntry(PoseStack poseStack, int x, int y, Pocket.Entry entry, @Nullable String amount, ItemRenderer itemRenderer, Font font);
	void renderElementTypeStack(PoseStack poseStack, int x, int y, ElementTypeStack stack, ItemRenderer itemRenderer, Font font);
	void renderElementTypeTooltip(PoseStack poseStack, int x, int y, ElementTypeStack stack, Screen screen);
	void renderElementTypeTooltip(PoseStack poseStack, int x, int y, Pocket.Entry entry, Screen screen);
	
	
	//===============
	// Client Config
	//===============
	SearchMode getSearchMode();
	void setSearchMode(SearchMode searchMode);
	SortingOrder getSortingOrder();
	void setSortingOrder(SortingOrder order);
	boolean isSortAscending();
	void setSortAscending(boolean sortAscending);
	PocketDisplayFilter getPocketDisplayFilter();
	void setPocketDisplayFilter(PocketDisplayFilter pocketDisplayMode);
	
	PocketDisplayMode getPocketDisplayMode();//TODO remove
	void setPocketDisplayMode(PocketDisplayMode pocketDisplayMode);//TODO remove
}
