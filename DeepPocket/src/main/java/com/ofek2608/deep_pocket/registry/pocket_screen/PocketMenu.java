package com.ofek2608.deep_pocket.registry.pocket_screen;

import com.ofek2608.deep_pocket.api.*;
import com.ofek2608.deep_pocket.api.enums.PocketDisplayMode;
import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.ElementTypeStack;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import com.ofek2608.deep_pocket.registry.MenuWithPocket;
import com.ofek2608.deep_pocket.registry.items.crafting_pattern.CraftingPatternItem;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.ofek2608.deep_pocket.utils.AdvancedLongMath.advancedMul;

public class PocketMenu extends AbstractContainerMenu implements MenuWithPocket {
	private static final DeepPocketClientHelper HELPER = DeepPocketClientHelper.get();
	private static final int INVISIBLE_SLOT_Y = -0xFFFFFF;
	
	public final Inventory playerInventory;
	private final CraftingContainer craftSlots = new CraftingContainer(this, 3, 3);
	private final ResultContainer resultSlots = new ResultContainer();
	private final PocketResultSlot resultSlot;
	private @Nullable Pocket pocket;//should not use except in get and set pocket
	public @Nullable Object screen;
	
	private int lastHoveredSlotIndex = -1;

	protected PocketMenu(@Nullable MenuType<?> menuType, int containerId, Inventory playerInventory) {
		super(menuType, containerId);
		this.playerInventory = playerInventory;
		//inventory
		for(int y = 0; y < 3; ++y)
			for(int x = 0; x < 9; ++x)
				this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 19 + x * 16, INVISIBLE_SLOT_Y));
		//hotbar
		for(int x = 0; x < 9; ++x)
			this.addSlot(new Slot(playerInventory, x, 19 + x * 16, INVISIBLE_SLOT_Y));
		//crafting
		for(int y = 0; y < 3; ++y)
			for(int x = 0; x < 3; ++x)
				addSlot(new Slot(craftSlots, x + y * 3, 37 + x * 16, INVISIBLE_SLOT_Y));
		addSlot(resultSlot = new PocketResultSlot(playerInventory.player, craftSlots, resultSlots, 0, 125, INVISIBLE_SLOT_Y, this));
	}

	public PocketMenu(int containerId, Inventory playerInventory) {
		this(DeepPocketRegistry.POCKET_MENU.get(), containerId, playerInventory);
	}

	public PocketMenu(int containerId, Inventory playerInventory, Pocket pocket) {
		this(containerId, playerInventory);
		setPocket(pocket);
	}
	
	public void setHoveredSlotIndex(int index, int mouseY) {
		if (index < 0 || slots.size() <= index)
			index = -1;
		if (index == lastHoveredSlotIndex)
			return;
		
		if (index >= 0)
			slots.get(index).y = mouseY - 8;
		if (lastHoveredSlotIndex >= 0)
			slots.get(lastHoveredSlotIndex).y = INVISIBLE_SLOT_Y;
		
		lastHoveredSlotIndex = index;
	}
	
	public void clearHoverSlotIndex() {
		setHoveredSlotIndex(-1, 0);
	}




	@Override
	public @Nullable Pocket getPocket() {
		return pocket != null && pocket.canAccess(playerInventory.player) ? pocket : null;
	}

	@Override
	public void setPocket(@Nullable Pocket pocket) {
		this.pocket = pocket;
	}

	private void putStackInInventory(Pocket pocket, ItemStack stack) {
		if (!stack.isEmpty()) moveItemStackTo(stack, 27, 36, false);//hotbar
		if (!stack.isEmpty()) moveItemStackTo(stack, 0, 27, false);//inventory
		if (!stack.isEmpty()) pocket.insertElement(ElementType.item(stack), stack.getCount());
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		if (index < 0 || slots.size() <= index)
			return ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		ItemStack stack = slot.getItem();
		if (stack.isEmpty())
			return ItemStack.EMPTY;
		Pocket pocket = getPocket();
		if (pocket == null)
			return stack;

		if (player.level.isClientSide) {
			slot.set(ItemStack.EMPTY);
			slot.setChanged();
			return ItemStack.EMPTY;
		}

		if (index < slots.size() - 1) {
			//not the result slot
			pocket.insertElement(ElementType.item(stack), stack.getCount());
			slot.set(ItemStack.EMPTY);
			slot.setChanged();
			return ItemStack.EMPTY;
		}

		//result slot
		ItemStack pickUp = resultSlot.safeTake(64, 64, player);
		if (pickUp.isEmpty())
			return ItemStack.EMPTY;
		int maxStackSize = pickUp.getMaxStackSize();

		while (resultSlot.hasItem() && ItemHandlerHelper.canItemStacksStack(pickUp, resultSlot.getItem())) {
			int takeCount = maxStackSize - pickUp.getCount();
			if (takeCount < resultSlot.getItem().getCount())
				break;
			ItemStack taken = resultSlot.safeTake(takeCount, takeCount, player);
			if (taken.isEmpty())
				break;
			pickUp.grow(taken.getCount());
		}
		putStackInInventory(pocket, pickUp);
		return ItemStack.EMPTY;



	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void slotsChanged(Container container) {
		reloadCrafting();
	}

	public void reloadCrafting() {
		if (!(playerInventory.player instanceof ServerPlayer player))
			return;
		if (!(player.level instanceof ServerLevel level))
			return;
		Optional<CraftingRecipe> recipe = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftSlots, level);
		ItemStack result = ItemStack.EMPTY;
		if (recipe.isPresent()) {
			CraftingRecipe craftingrecipe = recipe.get();
			if (resultSlots.setRecipeUsed(level, player, craftingrecipe))
				result = craftingrecipe.assemble(craftSlots);
		}

		slots.get(45).set(result);
		setRemoteSlot(45, result);
		player.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), 45, result));
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		if (player.level.isClientSide || !(player instanceof ServerPlayer serverPlayer))
			return;
		clearCraftingTable(serverPlayer);
	}




	public void clearCraftingTableToInventory(ServerPlayer player) {
		clearContainer(player, this.craftSlots);
		reloadCrafting();
	}

	public void clearCraftingTable(ServerPlayer player) {
		Pocket pocket = getPocket();
		if (pocket == null) {
			clearCraftingTableToInventory(player);
			return;
		}
		for (int i = 0; i < craftSlots.getContainerSize(); i++) {
			ItemStack stack = craftSlots.getItem(i);
			pocket.insertElement(ElementType.item(stack), stack.getCount());
			craftSlots.setItem(i, ItemStack.EMPTY);
		}
		resultSlot.set(ItemStack.EMPTY);
	}

	private static ElementType requestIngredientClientBound(ItemStack[] inventoryItems, @Nullable Pocket pocket, Ingredient ingredient, boolean consume) {
		//TODO support for recipes which aren't items
		for (ItemStack stack : inventoryItems) {
			if (stack.isEmpty() || !ingredient.test(stack))
				continue;
			ElementType type = ElementType.item(stack);
			if (consume)
				stack.shrink(1);
			return type;
		}

		DeepPocketClientApi api = DeepPocketClientApi.get();
		Knowledge knowledge = api.getKnowledge();
		if (pocket != null) {
			for (ElementType type : pocket.entries().sorted(HELPER.getSearchComparator()).map(Pocket.Entry::getType).toList())
				if (type instanceof ElementType.TItem item && ingredient.test(item.create()) && (!consume || pocket.extractItem(knowledge, type, 1) == 1))
					return type;
		}
		if (consume)
			return ElementType.empty();
		ItemStack[] ingredientItems = ingredient.getItems();
		return ingredientItems.length > 0 ? ElementType.item(ingredientItems[0]) : ElementType.empty();
	}

	public void requestRecipeClientBound(Recipe<?> recipe) {
		boolean consume;
		if (HELPER.getPocketDisplayMode() == PocketDisplayMode.CREATE_PATTERN) {
			consume = false;
		} else {
			HELPER.setPocketDisplayMode(PocketDisplayMode.CRAFTING);
			consume = true;
		}
		ItemStack[] inventoryItems = slots.stream().map(Slot::getItem).map(ItemStack::copy).toArray(ItemStack[]::new);
		Pocket pocket = getPocket();
		if (pocket != null)
			pocket = pocket.copy();
		ElementType[] requesting = new ElementType[9];

		NonNullList<Ingredient> ingredients = recipe.getIngredients();

		int w = recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : 3;
		if (w <= 0)
			w = 3;

		short index = 0;

		for (Ingredient ingredient : ingredients) {
			while (index % 3 >= w)
				index++;
			if (index == 9)
				return;
			requesting[index] = requestIngredientClientBound(inventoryItems, pocket, ingredient, consume);
			index++;
		}
		for (int i = 0; i < 9; i++)
			if (requesting[i] == null)
				requesting[i] = ElementType.empty();
		if (consume) {
			DeepPocketPacketHandler.sbRequestRecipe(requesting);
			return;
		}
		if (screen instanceof PocketScreen pocketScreen)
			pocketScreen.setPattern(requesting, recipe.getResultItem());
	}

	private boolean requestIngredientServerBound(ElementType.TItem type) {
		for (int slotIndex = 0; slotIndex < 36; slotIndex++) {
			ItemStack stack = playerInventory.getItem(slotIndex);
			if (stack.isEmpty() || !type.is(stack))
				continue;
			stack.shrink(1);
			playerInventory.setItem(slotIndex, stack);
			return true;
		}
		DeepPocketServerApi api = DeepPocketServerApi.get();
		Pocket pocket = getPocket();
		if (api == null || pocket == null)
			return false;
		return pocket.extractItem(api.getKnowledge(playerInventory.player.getUUID()), type, 1) == 1;
	}
	
	public void extractType(ServerPlayer player, ElementType.TItem type, boolean toCarry, byte count) {
		DeepPocketServerApi api = DeepPocketServerApi.get();
		Pocket pocket = getPocket();
		if (count <= 0 || api == null || pocket == null)
			return;
		Knowledge knowledge = api.getKnowledge(player.getUUID());
		if (!knowledge.contains(type))
			return;
		int maxStack = type.create().getMaxStackSize();
		count = (byte)Math.min(count, maxStack);
		
		if (!toCarry) {
			putStackInInventory(pocket, type.create((int)pocket.extractItem(knowledge, type, count)));
			return;
		}
		
		ItemStack carriedStack = getCarried();
		if (!carriedStack.isEmpty() && !type.is(carriedStack))
			return;
		
		int currentCount = carriedStack.isEmpty() ? 0 : carriedStack.getCount();
		int extractRequestCount = Math.min(maxStack - currentCount, count);
		if (extractRequestCount <= 0)
			return;
		
		ItemStack extracted = type.create((int)pocket.extractItem(api.getKnowledge(player.getUUID()), type, extractRequestCount));
		if (extracted.isEmpty())
			return;
		setCarried(type.create(currentCount + extracted.getCount()));
	}
	
	
	public void requestRecipeServerBound(ServerPlayer player, ElementType[] types) {
		clearCraftingTable(player);

		int len = Math.min(types.length, 9);
		for (int i = 0; i < len; i++) {
			ElementType type = types[i];
			if (type instanceof ElementType.TItem item)
				if (requestIngredientServerBound(item))
					craftSlots.setItem(i, item.create());
		}
	}

	public void insertCarry(ServerPlayer player, byte count) {
		ItemStack carried = getCarried();
		if (carried.isEmpty())
			return;
		DeepPocketServerApi api = DeepPocketServerApi.get();
		Pocket pocket = getPocket();
		if (api == null || pocket == null)
			return;
		if (count <= 0 || carried.getCount() <= count) {
			//insert all
			pocket.insertElement(ElementType.item(carried), carried.getCount());
			setCarried(ItemStack.EMPTY);
			return;
		}

		pocket.insertElement(ElementType.item(carried), count);
		carried.shrink(count);
		setCarried(carried);
	}

	public void createPattern(ServerPlayer player, ElementTypeStack[] input, ElementTypeStack[] output, boolean toCarry) {
		if (input.length != 9 || output.length != 9)
			return;
		boolean empty = true;
		for (ElementTypeStack outputItem : output)
			empty = empty && outputItem.isEmpty();
		if (empty)
			return;

		DeepPocketServerApi api = DeepPocketServerApi.get();
		Pocket pocket = getPocket();
		if (api == null || pocket == null)
			return;
		if (toCarry && !getCarried().isEmpty())
			return;
		if (pocket.extractItem(api.getKnowledge(player.getUUID()), ElementType.item(DeepPocketRegistry.EMPTY_CRAFTING_PATTERN_ITEM.get()), 1L) != 1)
			return;
		ItemStack newPattern = CraftingPatternItem.createItem(input, output);
		if (toCarry)
			setCarried(newPattern);
		else
			putStackInInventory(pocket, newPattern);

	}

	public ElementType.TItem[] getCrafting() {
		return IntStream.range(0, 9)
				.mapToObj(craftSlots::getItem)
				.map(ElementType::item)
				.map(type -> type instanceof ElementType.TItem item ? item : null)
				.filter(Objects::nonNull)
				.toArray(ElementType.TItem[]::new);
	}

	public void bulkCrafting(ServerPlayer player, long count) {
		DeepPocketServerApi api = DeepPocketServerApi.get();
		Pocket pocket = getPocket();
		if (api == null || pocket == null) return;

		Optional<CraftingRecipe> recipe = player.server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftSlots, player.level);
		if (recipe.isEmpty()) return;
		CraftingRecipe craftingrecipe = recipe.get();
		if (!resultSlots.setRecipeUsed(player.level, player, craftingrecipe)) return;
		ItemStack result = craftingrecipe.assemble(craftSlots);
		if (result.isEmpty()) return;
		ForgeHooks.setCraftingPlayer(player);
		NonNullList<ItemStack> remainingItems = player.level.getRecipeManager().getRemainingItemsFor(RecipeType.CRAFTING, this.craftSlots, player.level);
		ForgeHooks.setCraftingPlayer(null);

		Knowledge knowledge = api.getKnowledge(player.getUUID());
		ElementType.TItem[] crafting = getCrafting();
		long max = pocket.getMaxExtract(knowledge, crafting);
		if (0 <= max && max < count)
			count = max;
		if (count == 0)
			return;
		for (ElementType.TItem type : crafting)
			pocket.extractItem(knowledge, type, count);
		pocket.insertElement(ElementType.item(result), advancedMul(result.getCount(), count));
		for (ItemStack remainingItem : remainingItems)
			pocket.insertElement(ElementType.item(remainingItem), advancedMul(remainingItem.getCount(), count));

		resultSlot.checkTakeAchievements(result);
	}
}
