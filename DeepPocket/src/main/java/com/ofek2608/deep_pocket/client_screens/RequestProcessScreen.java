package com.ofek2608.deep_pocket.client_screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.DeepPocketClientHelper;
import com.ofek2608.deep_pocket.api.Pocket;
import com.ofek2608.deep_pocket.api.struct.CraftingPattern;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import com.ofek2608.deep_pocket.api.struct.RecipeRequest;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import com.ofek2608.deep_pocket.registry.items.crafting_pattern.CraftingPatternTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

class RequestProcessScreen extends Screen {
	private static final ResourceLocation TEXTURE = DeepPocketMod.loc("textures/gui/request_process.png");
	private final DeepPocketClientHelper dpClientHelper = DeepPocketClientHelper.get();
	private static final int DISPLAY_ROW_COUNT = 9;
	private static final int VIEW_WIDTH = 172;
	private static final int VIEW_HEIGHT = 50 + DISPLAY_ROW_COUNT * 18;
	private final @Nullable Screen backScreen;
	private final Pocket pocket;
	private final ItemType requestedType;
	private long requestedAmount;

	//Update Fields
	private int leftPos;
	private int topPos;
	private boolean holdShift;
	private boolean hoverCount;
	private boolean hoverCancel;
	private boolean hoverRequest;
	private boolean hoverScroll;
	private int hoveredPatternIndex;
	private int maxScroll;
	private int scroll;
	private final List<DisplayedPattern> allPatterns = new ArrayList<>();
	private final DisplayedPattern[] visiblePatterns = new DisplayedPattern[DISPLAY_ROW_COUNT * 9];
	private boolean errorNeedMainCraft;
	private boolean errorMissingIngredients;
	private boolean errorDependencyLoop;
	//focus fields
	private boolean focusScroll;
	private final Map<ItemType, Optional<CraftingPattern>> selectedPatterns = new HashMap<>();


	RequestProcessScreen(@Nullable Screen backScreen, Pocket pocket, ItemType requestedType, long requestedAmount) {
		super(Component.empty());
		this.backScreen = backScreen;
		this.pocket = pocket;
		this.requestedType = requestedType;
		this.requestedAmount = requestedAmount;
	}

	private void updateFields(int mx, int my) {
		leftPos = (width - VIEW_WIDTH) >> 1;
		topPos = (height - VIEW_HEIGHT) >> 1;
		mx -= leftPos;
		my -= topPos;

		//hover checks
		holdShift = Screen.hasShiftDown();
		hoverCount = 5 <= my && my <= 20 && 151 <= mx && mx <= 166;
		hoverCancel = VIEW_HEIGHT - 21 <= my && my <= VIEW_HEIGHT - 6 && 131 <= mx && mx <= 146;
		hoverRequest = VIEW_HEIGHT - 21 <= my && my <= VIEW_HEIGHT - 6 && 151 <= mx && mx <= 166;
		hoverScroll = 171 <= mx && mx <= 178 && 25 <= my && my <= 24 + 18 * DISPLAY_ROW_COUNT;
		if (5 <= mx && mx <= 166 && 25 <= my && my <= 24 + 18 * DISPLAY_ROW_COUNT)
			hoveredPatternIndex = (mx - 5) / 18 + (my - 25) / 18 * 9;
		else
			hoveredPatternIndex = -1;

		updatePatterns();
		//scroll
		maxScroll = Math.max((allPatterns.size() - 1) / 9 + 1 - DISPLAY_ROW_COUNT, 0);
		scroll = Math.max(Math.min(scroll, maxScroll), 0);
		int patternIndex = scroll * 9;
		for (int i = 0; i < visiblePatterns.length; i++)
			visiblePatterns[i] = patternIndex < allPatterns.size() ? allPatterns.get(patternIndex++) : null;
		errorNeedMainCraft = selectedPatterns.getOrDefault(requestedType, Optional.empty()).isEmpty();
		errorMissingIngredients = allPatterns.stream().anyMatch(p->p.border == PatternBorder.MISSING);
		errorDependencyLoop = allPatterns.stream().anyMatch(p->p.border == PatternBorder.LOOP);
	}

	private void updatePatterns() {
		allPatterns.clear();
		Map<ItemType,Integer> indexMap = new HashMap<>();
		{ // initialize allPatterns and indexMap
			int currentIndex = 0;
			allPatterns.add(new DisplayedPattern(requestedType));
			indexMap.put(requestedType, 0);
			while (currentIndex < allPatterns.size()) {
				DisplayedPattern display = allPatterns.get(currentIndex++);
				Optional<CraftingPattern> selectedPatternOpt = getPatternFor(display.type);
				if (selectedPatternOpt.isEmpty())
					continue;
				display.border = PatternBorder.LOOP;
				CraftingPattern selectedPattern = selectedPatternOpt.get();
				display.pattern = selectedPattern;
				for (ItemTypeAmount input : selectedPattern.getInput()) {
					if (input.isEmpty())
						continue;
					indexMap.computeIfAbsent(input.getItemType(), type -> {
						allPatterns.add(new DisplayedPattern(type));
						return indexMap.size();
					});
				}
			}
		}
		int patternCount = allPatterns.size();
		List<Integer> craftingOrder = new ArrayList<>();
		{ // finding crafting order, loops won't enter the crafting order

			List<List<Integer>> dependentOn = new ArrayList<>();
			for (int i = 0; i < patternCount; i++)
				dependentOn.add(new ArrayList<>());

			int[] leftToCraft = new int[patternCount];
			// filling left to craft, craftingOrder, and dependentOn
			for (int i = 0; i < patternCount; i++) {
				CraftingPattern pattern = allPatterns.get(i).pattern;
				if (pattern == null) {
					leftToCraft[i] = 0;
					craftingOrder.add(i);
					continue;
				}
				Set<ItemType> inputs = pattern.getInputCountMap().keySet();
				int requiredCount = inputs.size();
				leftToCraft[i] = requiredCount;
				if (requiredCount == 0)
					craftingOrder.add(i);
				for (ItemType input : inputs)
					dependentOn.get(indexMap.get(input)).add(i);
			}
			// tracking which item can be crafted and reducing the leftToCraft[dependentOn.get(i)]
			int currentCheck = 0;
			while (currentCheck < craftingOrder.size())
				for (int dependent : dependentOn.get(craftingOrder.get(currentCheck++)))
					if (--leftToCraft[dependent] == 0)
						craftingOrder.add(dependent);
		}
		// update border for craftable
		for (int craftable : craftingOrder) {
			DisplayedPattern display = allPatterns.get(craftable);
			if (display.pattern != null)
				display.border = PatternBorder.CRAFT;
		}
		// setting the required count
		{
			long[] craftAmount = new long[patternCount];
			long[] requiredAmount = new long[patternCount];
			requiredAmount[0] = requestedAmount;
			for (int i = craftingOrder.size() - 1; i >= 0; i--) {
				int patternIndex = craftingOrder.get(i);
				DisplayedPattern display = allPatterns.get(patternIndex);
				long thisRequiredAmount = requiredAmount[patternIndex];
				long thisCraftAmount = craftAmount[patternIndex];
				long thisExistingAmount = patternIndex == 0 ? 0 : pocket.getItemCount(display.type);
				long total = DeepPocketUtils.advancedSum(thisCraftAmount, thisExistingAmount);
				long thisNeededAmount = total < 0 ? 0 : thisRequiredAmount < 0 ? -1 : thisRequiredAmount <= total ? 0 : thisRequiredAmount - total;
				display.requiredAmount = thisRequiredAmount;
				display.existingAmount = thisExistingAmount;
				if (display.pattern == null)
					continue;
				if (thisNeededAmount == 0)
					continue;
				long patternOutputCount = display.pattern.getOutputCountMap().get(display.type);
				long craftingTimes = patternOutputCount < 0 ? 1 : thisNeededAmount < 0 ? -1 : (thisNeededAmount - 1) / patternOutputCount + 1;
				display.craftingTimes = craftingTimes;

				addItemsToLongArrayByIndexMap(display.pattern.getInput(), craftingTimes, requiredAmount, indexMap);
				addItemsToLongArrayByIndexMap(display.pattern.getOutput(), craftingTimes, craftAmount, indexMap);
			}
			//set border to enough
			for (int i = 0; i < patternCount; i++) {
				DisplayedPattern display = allPatterns.get(i);
				long thisRequiredAmount = requiredAmount[i];
				long thisExistingAmount = DeepPocketUtils.advancedSum(craftAmount[i], display.existingAmount);
				boolean enough = thisExistingAmount < 0 || thisRequiredAmount == thisExistingAmount || 0 <= thisRequiredAmount && thisRequiredAmount <= thisExistingAmount;
				if (enough && display.pattern == null)
					display.border = PatternBorder.ENOUGH;
			}
		}
	}

	private static void addItemsToLongArrayByIndexMap(ItemTypeAmount[] items, long multiply, long[] counts, Map<ItemType,Integer> indexMap) {
		for (ItemTypeAmount item : items) {
			int index = indexMap.getOrDefault(item.getItemType(), -1);
			if (index < 0)
				return;
			counts[index] = DeepPocketUtils.advancedSum(counts[index], DeepPocketUtils.advancedMul(multiply, item.getAmount()));
		}
	}

	private Optional<CraftingPattern> getPatternFor(ItemType itemType) {
		return selectedPatterns.computeIfAbsent(itemType, type -> {
			//pocket default
			if (pocket.getDefaultPatternsMap().containsKey(type)) {
				Optional<UUID> defaultPatternId = pocket.getDefaultPattern(type);
				if (defaultPatternId.isEmpty())
					return Optional.empty();
				CraftingPattern pattern = pocket.getPattern(defaultPatternId.get());
				if (pattern != null)
					return Optional.of(pattern);
			}
			//find recipe
			return pocket.getPatternsMap().values().stream().filter(pattern->{
				var outputArr = pattern.getOutput();
				for (ItemTypeAmount output : outputArr)
					if (!output.isEmpty() && type.equals(output.getItemType()))
						return true;
				return false;
			}).findAny();
		});
	}

	@Override
	public void render(PoseStack stack, int mx, int my, float partialTick) {
		updateFields(mx, my);

		renderBackground(stack);

		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);

		//outline
		DeepPocketUtils.setRenderShaderColor(pocket.getColor());
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
		(hoverCount ? Sprites.BUTTON_COUNT_H : Sprites.BUTTON_COUNT_N).blit(stack, leftPos + 151, topPos + 5);
		(hoverCancel ? Sprites.BUTTON_CANCEL_H : Sprites.BUTTON_CANCEL_N).blit(stack, leftPos + 131, topPos + VIEW_HEIGHT - 21);
		(errorNeedMainCraft || errorDependencyLoop ?
						(hoverRequest ? Sprites.BUTTON_ERROR_H : Sprites.BUTTON_ERROR_N) :
						(hoverRequest ? Sprites.BUTTON_REQUEST_H : Sprites.BUTTON_REQUEST_N)
		).blit(stack, leftPos + 151, topPos + VIEW_HEIGHT - 21);
		dpClientHelper.renderItemAmount(stack, leftPos + 151, topPos + 5, requestedType.create(), requestedAmount, itemRenderer, font);

		//patterns
		for (int row = 0; row < DISPLAY_ROW_COUNT; row++) {
			for (int col = 0; col < 9; col++) {
				int x = leftPos + 5 + col * 18;
				int y = topPos + 25 + row * 18;
				int patternIndex = col + row * 9;
				var pattern = visiblePatterns[patternIndex];
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, TEXTURE);
				DeepPocketUtils.setRenderShaderColor(0xFFFFFF);
				(pattern == null ? PatternBorder.NONE : pattern.border).getSprite(hoveredPatternIndex == patternIndex).blit(stack, x, y);
				if (pattern != null) {
					dpClientHelper.renderItem(stack, x + 1, y + 1, pattern.type.create(), itemRenderer, font);
					dpClientHelper.renderAmount(stack, x + 1, y + 1, pattern.requiredAmount, itemRenderer, font);
				}
			}
		}

		//text
		font.draw(stack, "Requesting", leftPos + 5, topPos + 9, 0xDDDDDD);

		//tooltip
		if (hoverCount)
			renderTooltip(stack, Component.literal("Change Amount"), mx, my);
		if (hoverCancel)
			renderTooltip(stack, Component.literal("Cancel"), mx, my);
		if (hoverRequest) {
			Component note = null;
			if (errorNeedMainCraft)
				note = Component.literal("Error: Please select at least one crafting recipe.").withStyle(ChatFormatting.DARK_RED);
			else if (errorDependencyLoop)
				note = Component.literal("Error: Recipes are dependent on each other.").withStyle(ChatFormatting.DARK_PURPLE);
			else if (errorMissingIngredients)
				note = Component.literal("Warning: some ingredients are missing.").withStyle(ChatFormatting.YELLOW);

			renderTooltip(stack, note == null ?
											List.of(Component.literal("Confirm")) :
											List.of(Component.literal("Confirm"), note),
							Optional.empty(), mx, my
			);
		}
		if (0 <= hoveredPatternIndex && hoveredPatternIndex < visiblePatterns.length) {
			DisplayedPattern pattern = visiblePatterns[hoveredPatternIndex];
			if (pattern != null) {
				List<Component> tooltip = new ArrayList<>();
				tooltip.add(getItemName(pattern.type.create()));
				tooltip.add(Component.literal("Required: ").append(createNumberComponent(pattern.requiredAmount).withStyle(ChatFormatting.DARK_AQUA)));
				tooltip.add(Component.literal("Existing: ").append(createNumberComponent(pattern.existingAmount).withStyle(ChatFormatting.DARK_AQUA)));
				if (pattern.border == PatternBorder.CRAFT)
					tooltip.add(Component.literal("Crafting: ").append(Component.literal("x").append(createNumberComponent(pattern.craftingTimes)).withStyle(ChatFormatting.DARK_AQUA)));
				tooltip.add(Component.empty());
				tooltip.add(Component.literal("Click To Select Recipe").withStyle(holdShift ? ChatFormatting.GRAY : ChatFormatting.GREEN));
				if (pattern.pattern != null)
					tooltip.add(Component.literal("ShiftClick To Remove Recipe").withStyle(holdShift ? ChatFormatting.GREEN : ChatFormatting.GRAY));

				Optional<TooltipComponent> recipeIcon = getPatternTooltip(pattern.pattern);
				renderTooltip(stack, tooltip, recipeIcon, mx, my);
			}
		}
	}

	private MutableComponent getItemName(ItemStack itemStack) {
		MutableComponent name = Component.empty().append(itemStack.getHoverName());
		return itemStack.hasCustomHoverName() ? name.withStyle(ChatFormatting.ITALIC) : name;
	}

	private MutableComponent createNumberComponent(long num) {
		return Component.literal(holdShift ? num < 0 ? "Inf" : "" + num : DeepPocketUtils.advancedToString(num));
	}

	private static Optional<TooltipComponent> getPatternTooltip(@Nullable CraftingPattern pattern) {
		if (pattern == null)
			return Optional.empty();
		return Optional.of(new CraftingPatternTooltip(pattern.getInput(), pattern.getOutput()));
	}

	private void clickCount() {
		ClientScreens.selectNumber(Component.literal("Select Amount"), pocket.getColor(), requestedAmount, selectedAmount->{
			Minecraft.getInstance().setScreen(this);
			if (selectedAmount != 0)
				requestedAmount = selectedAmount;
		});
	}

	private void clickCancel() {
		onClose();
	}

	private void clickRequest() {
		if (errorNeedMainCraft || errorDependencyLoop)
			return;
		selectedPatterns.keySet().retainAll(allPatterns.stream().map(pattern->pattern.type).toList());

		List<RecipeRequest> requests = new ArrayList<>();
		for (DisplayedPattern display : allPatterns) {
			UUID[] patterns = getSimilarPatterns(display.pattern);
			if (patterns.length > 0)
				requests.add(new RecipeRequest(display.type, display.craftingTimes, patterns));
		}

		Map<ItemType,Optional<UUID>> setDefaultPatterns = new HashMap<>();
		for (var entry : selectedPatterns.entrySet())
			setDefaultPatterns.put(entry.getKey(), entry.getValue().map(CraftingPattern::getPatternId));

		DeepPocketPacketHandler.sbRequestProcess(requests.toArray(RecipeRequest[]::new), setDefaultPatterns);
		onClose();
	}

	private UUID[] getSimilarPatterns(@Nullable CraftingPattern pattern) {
		if (pattern == null)
			return new UUID[0];
		var inputMap = pattern.getInputCountMap();
		var outputMap = pattern.getOutputCountMap();
		return pocket.getPatternsMap().values().stream()
						.filter(other -> other.getInputCountMap().equals(inputMap))
						.filter(other -> other.getOutputCountMap().equals(outputMap))
						.map(CraftingPattern::getPatternId)
						.toArray(UUID[]::new);
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button != InputConstants.MOUSE_BUTTON_LEFT)
			return false;
		updateFields((int)mx, (int)my);
		if (hoverCount) {
			DeepPocketUtils.playClickSound();
			clickCount();
			return true;
		}
		if (hoverCancel) {
			DeepPocketUtils.playClickSound();
			clickCancel();
			return true;
		}
		if (hoverRequest) {
			DeepPocketUtils.playClickSound();
			clickRequest();
			return true;
		}
		if (hoverScroll) {
			focusScroll = true; mouseMoved(mx, my);
			return true;
		}
		if (0 <= hoveredPatternIndex && hoveredPatternIndex < visiblePatterns.length) {
			var pattern = visiblePatterns[hoveredPatternIndex];
			if (pattern != null) {
				if (holdShift)
					selectedPatterns.put(pattern.type, Optional.empty());
				else {
					ClientScreens.selectRecipe(pocket, pattern.type, selectedPattern -> {
						if (minecraft != null)
							minecraft.setScreen(this);
						if (selectedPattern != null)
							selectedPatterns.put(pattern.type, Optional.of(selectedPattern));
					});
				}
			}
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
	public boolean keyPressed(int keyCode, int scanCode, int Modifiers) {
		if (keyCode == InputConstants.KEY_ESCAPE) {
			clickCancel();
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		if (pDelta > 0)
			scroll--;
		else if (pDelta < 0)
			scroll++;
		return true;
	}

	@Override
	public void onClose() {
		if (minecraft != null)
			minecraft.setScreen(backScreen);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private enum Sprites {
		OUTLINE_TOP(0, 0, 184, 25),
		OUTLINE_MIDDLE(0, 25, 184, 18),
		OUTLINE_BOTTOM(0, 43 , 184, 25),
		FRAME_TOP(0, 68, 184, 25),
		FRAME_MIDDLE(0, 93, 184, 18),
		FRAME_BOTTOM(0, 111 , 184, 25),

		SCROLL_N(0,136,8,2), SCROLL_H(0,138,8,2),
		BUTTON_COUNT_N(188, 0, 16, 16), BUTTON_COUNT_H(204, 0, 16, 16),
		BUTTON_CANCEL_N(188, 16, 16, 16), BUTTON_CANCEL_H(204, 16, 16, 16),
		BUTTON_REQUEST_N(188, 32, 16, 16), BUTTON_REQUEST_H(204, 32, 16, 16),
		BUTTON_ERROR_N(188, 48, 16, 16), BUTTON_ERROR_H(204, 48, 16, 16),

		PATTERN_BORDER_NONE_N   (220,  0, 18, 18), PATTERN_BORDER_NONE_H   (238,  0, 18, 18),
		PATTERN_BORDER_MISSING_N(220, 18, 18, 18), PATTERN_BORDER_MISSING_H(238, 18, 18, 18),
		PATTERN_BORDER_ENOUGH_N (220, 36, 18, 18), PATTERN_BORDER_ENOUGH_H (238, 36, 18, 18),
		PATTERN_BORDER_CRAFT_N  (220, 54, 18, 18), PATTERN_BORDER_CRAFT_H  (238, 54, 18, 18),
		PATTERN_BORDER_LOOP_N   (220, 72, 18, 18), PATTERN_BORDER_LOOP_H   (238, 72, 18, 18),
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

	private static final class DisplayedPattern {
		private final ItemType type;
		private long requiredAmount = 0, existingAmount = 0, craftingTimes = 0;
		private PatternBorder border = PatternBorder.MISSING;
		private @Nullable CraftingPattern pattern = null;

		private DisplayedPattern(ItemType type) {
			this.type = type;
		}
	}

	private enum PatternBorder {
		NONE(Sprites.PATTERN_BORDER_NONE_N, Sprites.PATTERN_BORDER_NONE_H),
		MISSING(Sprites.PATTERN_BORDER_MISSING_N, Sprites.PATTERN_BORDER_MISSING_H),
		ENOUGH(Sprites.PATTERN_BORDER_ENOUGH_N, Sprites.PATTERN_BORDER_ENOUGH_H),
		CRAFT(Sprites.PATTERN_BORDER_CRAFT_N, Sprites.PATTERN_BORDER_CRAFT_H),
		LOOP(Sprites.PATTERN_BORDER_LOOP_N, Sprites.PATTERN_BORDER_LOOP_H);

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
