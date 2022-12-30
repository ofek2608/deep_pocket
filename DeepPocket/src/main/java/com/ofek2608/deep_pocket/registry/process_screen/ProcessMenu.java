package com.ofek2608.deep_pocket.registry.process_screen;

import com.ofek2608.deep_pocket.api.pocket.Pocket;
import com.ofek2608.deep_pocket.api.pocket_process.old.PocketProcessManager;
import com.ofek2608.deep_pocket.api.pocket_process.old.PocketProcessRecipe;
import com.ofek2608.deep_pocket.api.pocket_process.old.PocketProcessUnit;
import com.ofek2608.deep_pocket.api.struct.ProcessUnitClientData;
import com.ofek2608.deep_pocket.network.DeepPocketPacketHandler;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import com.ofek2608.deep_pocket.registry.MenuWithPocket;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class ProcessMenu extends AbstractContainerMenu implements MenuWithPocket {
	private @Nullable Pocket pocket;
	private final Player player;
	//server
	private final @Nullable PocketProcessManager processManager;
	private final AtomicInteger processUnitId = new AtomicInteger();
	//client
	public @Nonnull ProcessUnitClientData clientData = ProcessUnitClientData.EMPTY;

	public ProcessMenu(int containerId, Inventory inventory) {
		this(containerId, inventory.player, (PocketProcessManager)null);
	}

	public ProcessMenu(int containerId, Player player, Pocket pocket) {
		this(containerId, player, pocket.getProcesses());
		this.pocket = pocket;
	}

	private ProcessMenu(int containerId, @Nullable Player player, @Nullable PocketProcessManager processManager) {
		super(DeepPocketRegistry.PROCESS_MENU.get(), containerId);
		this.player = player;
		this.processManager = processManager;
	}

	protected ProcessMenu(@Nullable MenuType<?> menuType, int containerId, @Nullable Player player, @Nullable PocketProcessManager processManager) {
		super(menuType, containerId);
		this.player = player;
		this.processManager = processManager;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		if (player.level.isClientSide)
			return true;//on client is always valid
		return pocket != null && pocket.canAccess(player);
	}


	@Override
	public @Nullable Pocket getPocket() {
		return pocket != null && pocket.canAccess(player) ? pocket : null;
	}

	@Override
	public void setPocket(@Nullable Pocket pocket) {
		this.pocket = pocket;
	}

	public void sendUpdate() {
		if (processManager == null || !(player instanceof ServerPlayer serverPlayer) || pocket == null || !pocket.canAccess(player))
			return;

		ProcessUnitClientData clientData = createClientData(processManager, processUnitId);
		DeepPocketPacketHandler.cbSetViewedProcessData(PacketDistributor.PLAYER.with(()->serverPlayer), clientData);
	}

	private static ProcessUnitClientData createClientData(PocketProcessManager processManager, AtomicInteger processUnitId) {
		var units = processManager.getUnits();
		int index = getFirstBiggerId(units, processUnitId.get());
		if (index < 0)//there are no current processes
			return ProcessUnitClientData.EMPTY;
		PocketProcessUnit unit = units.get(index);
		processUnitId.set(unit.getId());

		List<ProcessUnitClientData.CraftingItem> craftingItems = unit.getRecipes().stream()
						.map(recipe -> new ProcessUnitClientData.CraftingItem(recipe.getResult(), recipe.getLeftToCraft(), getRecipePattern(recipe)))
						.toList();
		List<ProcessUnitClientData.IngredientItem> ingredientItems = IntStream.range(0, unit.getTypeCount())
						.mapToObj(i->new ProcessUnitClientData.IngredientItem(unit.getType(i), unit.getLeftToProvide(i)))
						.filter(ing->ing.required > 0)
						.toList();

		return new ProcessUnitClientData(index, units.size(), craftingItems, ingredientItems);
	}

	private static UUID getRecipePattern(PocketProcessRecipe recipe) {
		var crafters = recipe.getCrafters();
		if (crafters.size() == 0)
			return Util.NIL_UUID;
		return crafters.get(0).getPatternId();
	}

	private static int getFirstBiggerId(List<PocketProcessUnit> units, int id) {
		for (int i = 0; i < units.size(); i++)
			if (id <= units.get(i).getId())
				return i;
		return units.size() - 1;
	}

	@Override
	public boolean clickMenuButton(Player player, int buttonId) {
		switch (buttonId) {
			case 0 -> clickButtonPrev();
			case 1 -> clickButtonNext();
			case 2 -> clickButtonStop();
			case 3 -> clickButtonForceStop();
			default -> {return false;}
		}
		return true;
	}

	private void clickButtonPrev() {
		if (processManager == null)
			return;
		var units = processManager.getUnits();
		int index = getFirstBiggerId(units, processUnitId.get());
		if (index <= 0)
			return;
		processUnitId.set(units.get(index - 1).getId());
	}

	private void clickButtonNext() {
		if (processManager == null)
			return;
		var units = processManager.getUnits();
		int index = getFirstBiggerId(units, processUnitId.get());
		if (index < 0 || units.size() <= index + 1)
			return;
		processUnitId.set(units.get(index + 1).getId());
	}

	private void clickButtonStop() {
		if (processManager == null)
			return;
		var units = processManager.getUnits();
		int index = getFirstBiggerId(units, processUnitId.get());
		if (index < 0)
			return;
		units.get(index).stop();
	}

	private void clickButtonForceStop() {
		if (processManager == null)
			return;
		var units = processManager.getUnits();
		int index = getFirstBiggerId(units, processUnitId.get());
		if (index < 0)
			return;
		units.get(index).forceStop();
	}
}
