package com.ofek2608.deep_pocket.registry.pocket_screen;

import com.ofek2608.deep_pocket.DeepPocketUtils;
import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.DeepPocketServerApi;
import com.ofek2608.deep_pocket.api.struct.Pocket;
import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class PocketMenu extends AbstractContainerMenu {
	public static final MenuProvider MENU_PROVIDER = new SimpleMenuProvider(PocketMenu::new, Component.empty());
	public final Inventory playerInventory;
	private final CraftingContainer craftSlots = new CraftingContainer(this, 3, 3);
	private final ResultContainer resultSlots = new ResultContainer();
	private @Nullable PocketResultSlot resultSlot;
	private @Nullable Pocket pocket;//should not use except in get and set pocket

	private int lastResetYOffset;
	private boolean lastResetShowCrafting;

	protected PocketMenu(@Nullable MenuType<?> menuType, int containerId, Inventory playerInventory) {
		super(menuType, containerId);
		this.playerInventory = playerInventory;
		resetSlots(0, true);
	}

	public PocketMenu(int containerId, Inventory playerInventory) {
		this(DeepPocketRegistry.POCKET_MENU.get(), containerId, playerInventory);
	}

	public PocketMenu(int containerId, Inventory playerInventory, Player player) {
		this(containerId, playerInventory);
	}

	private static final int CRAFTING_HEIGHT = 48 + 4;
	public void resetSlots(int yOffset, boolean showCrafting) {
		if (lastResetYOffset == yOffset && lastResetShowCrafting == showCrafting)
			return;
		lastResetYOffset = yOffset;
		lastResetShowCrafting = showCrafting;
		slots.clear();
		if (showCrafting)
			yOffset += CRAFTING_HEIGHT;
		//inventory
		for(int y = 0; y < 3; ++y) {
			for(int x = 0; x < 9; ++x) {
				this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 19 + x * 16, yOffset + y * 16));
			}
		}
		//hotbar
		for(int x = 0; x < 9; ++x) {
			this.addSlot(new Slot(playerInventory, x, 19 + x * 16, yOffset + 48 + 4));
		}
		//crafting
		if (showCrafting)
			yOffset -= CRAFTING_HEIGHT;
		else
			yOffset -= 0xFFFFFF;//Outside the screen
		for(int y = 0; y < 3; ++y)
			for(int x = 0; x < 3; ++x)
				addSlot(new Slot(craftSlots, x + y * 3, 37 + x * 16, yOffset + y * 16));
		addSlot(resultSlot = new PocketResultSlot(playerInventory.player, craftSlots, resultSlots, 0, 125, yOffset + 16, this));
	}




	public @Nullable Pocket getPocket() {
		return pocket != null && pocket.canAccess(playerInventory.player) ? pocket : null;
	}

	public void setPocket(@Nullable Pocket pocket) {
		this.pocket = pocket;
	}








	private void putStackInInventory(DeepPocketServerApi api, Pocket pocket, ItemStack stack) {
		if (!stack.isEmpty()) moveItemStackTo(stack, 27, 36, false);//hotbar
		if (!stack.isEmpty()) moveItemStackTo(stack, 0, 27, false);//inventory
		if (!stack.isEmpty()) api.insertItem(pocket, stack);
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

		DeepPocketServerApi api = DeepPocketServerApi.get();
		if (api == null) {
			slot.setChanged();
			return stack;
		}

		if (index < slots.size() - 1) {
			//not the result slot
			api.insertItem(pocket, stack);
			slot.set(ItemStack.EMPTY);
			slot.setChanged();
			return ItemStack.EMPTY;
		}

		//result slot
		if (resultSlot == null)
			return stack;
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
		putStackInInventory(api, pocket, pickUp);
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
		DeepPocketServerApi api = DeepPocketServerApi.get();
		Pocket pocket = getPocket();
		if (api == null || pocket == null) {
			clearCraftingTableToInventory(player);
			return;
		}
		for (int i = 0; i < craftSlots.getContainerSize(); i++) {
			api.insertItem(pocket, craftSlots.getItem(i));
			craftSlots.setItem(i, ItemStack.EMPTY);
		}
		if (resultSlot != null)
			resultSlot.set(ItemStack.EMPTY);
	}

	private static @Nullable ItemType requestIngredientClientBound(ItemStack[] inventoryItems, @Nullable Pocket pocket, Ingredient ingredient) {
		for (ItemStack stack : inventoryItems) {
			if (stack.isEmpty() || !ingredient.test(stack))
				continue;
			ItemType type = new ItemType(stack);
			stack.shrink(1);
			return type;
		}

		DeepPocketClientApi api = DeepPocketClientApi.get();
		if (pocket == null)
			return null;
		for (ItemType type : api.getSortedKnowledge(pocket).map(Map.Entry::getKey).toList())
			if (ingredient.test(type.create()) && !api.extractItem(pocket, type.create()).isEmpty())
				return type;
		return null;
	}

	public void requestRecipeClientBound(Recipe<?> recipe) {
		ItemStack[] inventoryItems = slots.stream().map(Slot::getItem).map(ItemStack::copy).toArray(ItemStack[]::new);
		Pocket pocket = getPocket();
		if (pocket != null)
			pocket = pocket.copy();
		ItemType[] requesting = new ItemType[9];

		NonNullList<Ingredient> ingredients = recipe.getIngredients();

		int w = recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : 3;
		if (w <= 0)
			w = 3;

		short index = 0;

		for (Ingredient ingredient : ingredients) {
			while (index % 3 >= w)
				index++;
			if (index >= 9)
				return;
			requesting[index] = requestIngredientClientBound(inventoryItems, pocket, ingredient);
			index++;
		}
		for (int i = 0; i < 9; i++)
			if (requesting[i] == null)
				requesting[i] = ItemType.EMPTY;
		DeepPocketPacketHandler.sbRequestRecipe(requesting);
	}

	private boolean requestIngredientServerBound(ItemType type) {
		if (type.isEmpty())
			return false;
		for (int slotIndex = 0; slotIndex < 36; slotIndex++) {
			ItemStack stack = playerInventory.getItem(slotIndex);
			if (stack.isEmpty() || !new ItemType(stack).equals(type))
				continue;
			stack.shrink(1);
			playerInventory.setItem(slotIndex, stack);
			return true;
		}
		DeepPocketServerApi api = DeepPocketServerApi.get();
		Pocket pocket = getPocket();
		if (api == null || pocket == null)
			return false;
		return !api.extractItem(pocket, type.create(1)).isEmpty();
	}


	public void requestRecipeServerBound(ServerPlayer player, ItemType[] types) {
		clearCraftingTable(player);

		int len = Math.min(types.length, 9);
		for (int i = 0; i < len; i++)
			if (requestIngredientServerBound(types[i]))
				craftSlots.setItem(i, types[i].create());
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
			api.insertItem(pocket, carried);
			setCarried(ItemStack.EMPTY);
			return;
		}

		ItemStack insert = carried.copy();
		insert.setCount(count);
		api.insertItem(pocket, insert);

		carried.shrink(count);
		setCarried(carried);
	}

	public void extractType(ServerPlayer player, ItemType type, boolean toCarry, byte count) {
		DeepPocketServerApi api = DeepPocketServerApi.get();
		Pocket pocket = getPocket();
		if (count <= 0 || api == null || pocket == null || !api.getKnowledge(player.getUUID()).contains(type))
			return;
		int maxStack = type.create().getMaxStackSize();
		count = (byte)Math.min(count, maxStack);

		if (!toCarry) {
			putStackInInventory(api, pocket, api.extractItem(pocket, type.create(count)));
			return;
		}

		ItemStack carriedStack = getCarried();
		if (!carriedStack.isEmpty() && !new ItemType(carriedStack).equals(type))
			return;

		int currentCount = carriedStack.isEmpty() ? 0 : carriedStack.getCount();
		int extractRequestCount = Math.min(maxStack - currentCount, count);
		if (extractRequestCount <= 0)
			return;
		ItemStack extracted = api.extractItem(pocket, type.create(extractRequestCount));
		if (extracted.isEmpty())
			return;
		setCarried(type.create(currentCount + extracted.getCount()));
	}

	public ItemType[] getCrafting() {
		ItemType[] result = new ItemType[9];
		for (int i = 0; i < 9; i++)
			result[i] = new ItemType(craftSlots.getItem(i));
		return result;
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

		long max = pocket.getMaxExtract(getCrafting());
		if (0 <= max && max < count)
			count = max;
		if (count == 0)
			return;
		for (ItemType type : getCrafting())
			pocket.extractItem(type, count);
		api.insertItem(pocket, new ItemType(result), DeepPocketUtils.advancedMul(result.getCount(), count));
		for (ItemStack remainingItem : remainingItems)
			api.insertItem(pocket, new ItemType(remainingItem), DeepPocketUtils.advancedMul(remainingItem.getCount(), count));

		if (resultSlot != null)
			resultSlot.checkTakeAchievements(result);
	}
}
