package com.ofek2608.deep_pocket.registry.process_screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.struct.CraftingPatternOld;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.ProcessUnitClientData;
import com.ofek2608.deep_pocket.api.struct.client.ClientPocket;
import com.ofek2608.deep_pocket.utils.DeepPocketUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.ofek2608.deep_pocket.utils.AdvancedLongMath.advancedToString;

public class ProcessScreen extends AbstractContainerScreen<ProcessMenu> {
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/process.png");
	private final DeepPocketClientHelper dpClientHelper = DeepPocketClientHelper.get();
	private static final int DISPLAY_ROW_COUNT = 9;
	private static final int VIEW_WIDTH = 172;
	private static final int VIEW_HEIGHT = 50 + DISPLAY_ROW_COUNT * 18;

	//Update Fields
	private int leftPos;
	private int topPos;
	private boolean holdShift;
	private ProcessUnitClientData data;
	private boolean hoverCount, hoverPrev, hoverNext, hoverStop, hoverScroll;
	private int hoveredTypeIndex;
	private int maxScroll;
	private int scroll;
	private final List<DisplayedTypes> allTypes = new ArrayList<>();
	private final DisplayedTypes[] visibleTypes = new DisplayedTypes[9 * DISPLAY_ROW_COUNT];
	private boolean isEmpty;
	private boolean isFinalizing;
	//focus fields
	private boolean focusScroll;


	public ProcessScreen(ProcessMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}

	private void updateFields(int mx, int my) {
		leftPos = (width - VIEW_WIDTH) >> 1;
		topPos = (height - VIEW_HEIGHT) >> 1;
		mx -= leftPos;
		my -= topPos;

		holdShift = Screen.hasShiftDown();
		data = menu.clientData;

		//hover checks
		hoverCount = 5 <= my && my <= 20 && 151 <= mx && mx <= 166;
		hoverPrev = VIEW_HEIGHT - 21 <= my && my <= VIEW_HEIGHT - 6 && 49 <= mx && mx <= 64;
		hoverNext = VIEW_HEIGHT - 21 <= my && my <= VIEW_HEIGHT - 6 && 107 <= mx && mx <= 122;
		hoverStop = VIEW_HEIGHT - 21 <= my && my <= VIEW_HEIGHT - 6 && 151 <= mx && mx <= 166;
		hoverScroll = 171 <= mx && mx <= 178 && 25 <= my && my <= 24 + 18 * DISPLAY_ROW_COUNT;
		if (5 <= mx && mx <= 166 && 25 <= my && my <= 24 + 18 * DISPLAY_ROW_COUNT)
			hoveredTypeIndex = (mx - 5) / 18 + (my - 25) / 18 * 9;
		else
			hoveredTypeIndex = -1;

		updateTypes();
		//scroll
		maxScroll = Math.max((allTypes.size() - 1) / 9 + 1 - DISPLAY_ROW_COUNT, 0);
		scroll = Math.max(Math.min(scroll, maxScroll), 0);
		int patternIndex = scroll * 9;
		for (int i = 0; i < visibleTypes.length; i++)
			visibleTypes[i] = patternIndex < allTypes.size() ? allTypes.get(patternIndex++) : null;
		isEmpty = allTypes.isEmpty();
		isFinalizing = allTypes.stream().allMatch(display -> display.crafting == null || display.crafting.leftToCraft == 0);
	}

	private void updateTypes() {
		allTypes.clear();
		Map<ItemType,DisplayedTypes> typeDisplayMap = new HashMap<>();
		Function<ItemType, DisplayedTypes> newDisplayedType = type -> {
			var ret = new DisplayedTypes(type);
			allTypes.add(ret);
			return ret;
		};
		//FIXME
		for (ProcessUnitClientData.CraftingItem craftingItem : data.craftingItems)
			typeDisplayMap.computeIfAbsent(craftingItem.item, newDisplayedType).crafting = craftingItem;
//		for (ProcessUnitClientData.IngredientItem ingredientItem : data.ingredientItems)
//			typeDisplayMap.computeIfAbsent(ingredientItem.type, newDisplayedType).ingredient = ingredientItem;
	}

	@Override
	public void render(PoseStack stack, int mx, int my, float partialTick) {
		updateFields(mx, my);

		renderBackground(stack);

		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);

		//outline
		ClientPocket pocket = menu.getClientPocket();
		DeepPocketUtils.setRenderShaderColor(pocket == null ? 0xFFFFFF : pocket.getColor());
		Sprites.OUTLINE_TOP.blit(stack, leftPos, topPos);
		Sprites.OUTLINE_MIDDLE.blit(stack, leftPos, topPos + Sprites.OUTLINE_TOP.h, 184, VIEW_HEIGHT - Sprites.OUTLINE_TOP.h - Sprites.OUTLINE_BOTTOM.h);
		Sprites.OUTLINE_BOTTOM.blit(stack, leftPos, topPos + VIEW_HEIGHT - Sprites.OUTLINE_BOTTOM.h);

		DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
		//frame
		Sprites.FRAME_TOP.blit(stack, leftPos, topPos);
		Sprites.FRAME_MIDDLE.blit(stack, leftPos, topPos + Sprites.FRAME_TOP.h, 184, VIEW_HEIGHT - Sprites.FRAME_TOP.h - Sprites.FRAME_BOTTOM.h);
		Sprites.FRAME_BOTTOM.blit(stack, leftPos, topPos + VIEW_HEIGHT - Sprites.FRAME_BOTTOM.h);

		//scroll
		{
			int x = leftPos + 171;
			int y = topPos + 25 + (maxScroll == 0 ? 0 : (18 * DISPLAY_ROW_COUNT - 2) * scroll / maxScroll);
			(hoverScroll || focusScroll ? Sprites.SCROLL_H : Sprites.SCROLL_N).blit(stack, x, y);
		}

		//buttons

		(hoverCount ? Sprites.BUTTON_EMPTY_H : Sprites.BUTTON_EMPTY_N).blit(stack, leftPos + 151, topPos + 5);
		(hoverPrev ? Sprites.BUTTON_PAGE_PREV_H : Sprites.BUTTON_PAGE_PREV_N).blit(stack, leftPos + 49, topPos + VIEW_HEIGHT - 21);
		(hoverNext ? Sprites.BUTTON_PAGE_NEXT_H : Sprites.BUTTON_PAGE_NEXT_N).blit(stack, leftPos + 107, topPos + VIEW_HEIGHT - 21);
		(isEmpty ? hoverStop ? Sprites.BUTTON_EMPTY_H : Sprites.BUTTON_EMPTY_N : isFinalizing ?
						hoverStop ? Sprites.BUTTON_FORCE_STOP_H : Sprites.BUTTON_FORCE_STOP_N :
						hoverStop ? Sprites.BUTTON_STOP_H : Sprites.BUTTON_STOP_N
		).blit(stack, leftPos + 151, topPos + VIEW_HEIGHT - 21);
		if (allTypes.size() > 0) {
			DisplayedTypes mainType = allTypes.get(0);

			dpClientHelper.renderItemAmount(stack, leftPos + 151, topPos + 5, mainType.type.create(), mainType.getDisplayAmount(pocket), itemRenderer, font);
		}

		//patterns
		for (int row = 0; row < DISPLAY_ROW_COUNT; row++) {
			for (int col = 0; col < 9; col++) {
				int x = leftPos + 5 + col * 18;
				int y = topPos + 25 + row * 18;
				int typeIndex = col + row * 9;
				var pattern = visibleTypes[typeIndex];
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, TEXTURE);
				DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
				(pattern == null ? PatternBorder.NONE : pattern.getBorder()).getSprite(hoveredTypeIndex == typeIndex).blit(stack, x, y);
				if (pattern != null) {
					dpClientHelper.renderItem(stack, x + 1, y + 1, pattern.type.create(), itemRenderer, font);
					dpClientHelper.renderAmount(stack, x + 1, y + 1, pattern.getDisplayAmount(pocket), itemRenderer, font);
				}
			}
		}

		//text
		font.draw(stack, "Process", leftPos + 5, topPos + 9, 0xDDDDDD);
		drawPageText(stack, "/", leftPos + 86, topPos + VIEW_HEIGHT - 17);
		drawPageText(stack, isEmpty ? "-" : "" + (data.pageIndex + 1), leftPos + 76, topPos + VIEW_HEIGHT - 17);
		drawPageText(stack, isEmpty ? "0" : "" + data.pageCount, leftPos + 96, topPos + VIEW_HEIGHT - 17);

		//tooltip
		if (hoverPrev)
			renderTooltip(stack, Component.literal("Previous Page"), mx, my);
		if (hoverNext)
			renderTooltip(stack, Component.literal("Next Page"), mx, my);
		if (hoverStop && !isEmpty)
			renderTooltip(stack, isFinalizing ? Component.literal("Force Stop").withStyle(ChatFormatting.RED) : Component.literal("Stop"), mx, my);
		if (0 <= hoveredTypeIndex && hoveredTypeIndex < visibleTypes.length) {
			DisplayedTypes pattern = visibleTypes[hoveredTypeIndex];
			if (pattern != null)
				renderTooltip(stack, pattern.getTooltip(holdShift), pattern.getIcon(pocket), mx, my);
		}
	}

	private void drawPageText(PoseStack stack, String text, int x, int y) {
		x -= font.width(text) / 2;
		font.draw(stack, text, x, y, 0xFFFFFF);
	}

	private void onClickButton(int index) {
		DeepPocketUtils.playClickSound();
		if (minecraft != null && minecraft.gameMode != null)
			minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index);
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button != InputConstants.MOUSE_BUTTON_LEFT)
			return false;
		updateFields((int)mx, (int)my);
		if (hoverPrev) {
			onClickButton(0);
			return true;
		}
		if (hoverNext) {
			onClickButton(1);
			return true;
		}
		if (hoverStop) {
			onClickButton(isFinalizing ? 3 : 2);
		}
		if (hoverScroll) {
			focusScroll = true; mouseMoved(mx, my);
			return true;
		}
		return false;
	}

	@Override
	public void mouseMoved(double mx, double my) {
		if (focusScroll)
			scroll = Math.max(Math.min((((int)my - topPos - 25) * maxScroll / (DISPLAY_ROW_COUNT * 8) + 1) / 2, maxScroll), 0);
	}

	@Override
	public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
		focusScroll = false;
		return super.mouseReleased(pMouseX, pMouseY, pButton);
	}

	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		if (pDelta > 0)
			scroll--;
		else if (pDelta < 0)
			scroll++;
		return true;
	}

	@Override protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {}

	private enum Sprites {
		OUTLINE_TOP(0, 0, 184, 25),
		OUTLINE_MIDDLE(0, 25, 184, 18),
		OUTLINE_BOTTOM(0, 43 , 184, 25),
		FRAME_TOP(0, 68, 184, 25),
		FRAME_MIDDLE(0, 93, 184, 18),
		FRAME_BOTTOM(0, 111 , 184, 25),

		SCROLL_N(0,136,8,2), SCROLL_H(0,138,8,2),
		BUTTON_EMPTY_N(188, 0, 16, 16), BUTTON_EMPTY_H(204, 0, 16, 16),
		BUTTON_STOP_N(188, 16, 16, 16), BUTTON_STOP_H(204, 16, 16, 16),
		BUTTON_FORCE_STOP_N(188, 32, 16, 16), BUTTON_FORCE_STOP_H(204, 32, 16, 16),
		BUTTON_PAGE_PREV_N(188, 48, 16, 16), BUTTON_PAGE_PREV_H(204, 48, 16, 16),
		BUTTON_PAGE_NEXT_N(188, 64, 16, 16), BUTTON_PAGE_NEXT_H(204, 64, 16, 16),

		PATTERN_BORDER_NONE_N   (220,  0, 18, 18), PATTERN_BORDER_NONE_H   (238,  0, 18, 18),
		PATTERN_BORDER_MISSING_N(220, 18, 18, 18), PATTERN_BORDER_MISSING_H(238, 18, 18, 18),
		PATTERN_BORDER_CRAFT_N  (220, 36, 18, 18), PATTERN_BORDER_CRAFT_H  (238, 36, 18, 18),
		;

		private final int u, v, w, h;

		Sprites(int u, int v, int w, int h) {
			this.u = u;
			this.v = v;
			this.w = w;
			this.h = h;
		}

		private void blit(PoseStack stack, int x, int y) {
			Screen.blit(stack, x, y, u, v, w, h, 256, 256);
		}

		@SuppressWarnings("SameParameterValue")
		private void blit(PoseStack stack, int x, int y, int displayW, int displayH) {
			Screen.blit(stack, x, y, displayW, displayH, u, v, w, h, 256, 256);
		}
	}

	private static final class DisplayedTypes {
		private final ItemType type;
		private @Nullable ProcessUnitClientData.CraftingItem crafting;
		private @Nullable ProcessUnitClientData.IngredientItem ingredient;

		private DisplayedTypes(ItemType type) {
			this.type = type;
		}

		private long getDisplayAmount(@Nullable Pocket pocket) {
			if (ingredient != null)
				return ingredient.required;
			if (crafting == null)
				return 1L;
			CraftingPatternOld pattern = pocket == null ? null : pocket.getPattern(crafting.recipeId);
			return (pattern == null ? 1L : pattern.getOutputCountMap().getOrDefault(type, 1L)) * crafting.leftToCraft;
		}

		private PatternBorder getBorder() {
			return crafting == null ? PatternBorder.MISSING : PatternBorder.CRAFT;
		}

		public Optional<TooltipComponent> getIcon(@Nullable Pocket pocket) {
			//FIXME
//			if (crafting == null || pocket == null)
//				return Optional.empty();
//			CraftingPatternOld pattern = pocket.getPattern(crafting.recipeId);
//			if (pattern == null)
//				return Optional.empty();
//			return Optional.of(new CraftingPatternTooltip(pattern.getInput(), pattern.getOutput()));
			return Optional.empty();
		}

		public List<Component> getTooltip(boolean holdShift) {
			List<Component> tooltip = new ArrayList<>();

			ItemStack itemStack = type.create();
			MutableComponent name = Component.empty().append(itemStack.getHoverName());
			if (itemStack.hasCustomHoverName())
				name = name.withStyle(ChatFormatting.ITALIC);

			tooltip.add(name);
			if (crafting != null)
				tooltip.add(Component.literal("Crafting: ").append(createNumberComponent(holdShift, crafting.leftToCraft).withStyle(ChatFormatting.DARK_AQUA)));
			if (ingredient != null)
				tooltip.add(Component.literal("Required: ").append(createNumberComponent(holdShift, ingredient.required).withStyle(ChatFormatting.DARK_AQUA)));

			return tooltip;
		}

		private MutableComponent createNumberComponent(boolean holdShift, long num) {
//			return Component.literal(holdShift ? num < 0 ? "Inf" : "" + num : DeepPocketUtils.advancedToString(num));
			return Component.literal(advancedToString(num, holdShift ? 19 : 6));
		}
	}

	private enum PatternBorder {
		NONE(Sprites.PATTERN_BORDER_NONE_N, Sprites.PATTERN_BORDER_NONE_H),
		MISSING(Sprites.PATTERN_BORDER_MISSING_N, Sprites.PATTERN_BORDER_MISSING_H),
		CRAFT(Sprites.PATTERN_BORDER_CRAFT_N, Sprites.PATTERN_BORDER_CRAFT_H);

		private final Sprites n, h;

		PatternBorder(Sprites n, Sprites h) {
			this.n = n;
			this.h = h;
		}

		public Sprites getSprite(boolean hover) {
			return hover ? h : n;
		}
	}
}
