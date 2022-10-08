package com.ofek2608.deep_pocket.integration;

import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import com.ofek2608.deep_pocket.registry.pocket_screen.PocketMenu;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.Optional;


public final class DeepPocketJEI {
	private DeepPocketJEI() {}
	private static final String MODID = "jei";

	public static boolean hasMod() {
		return ModList.get().isLoaded(MODID);
	}

	public static @Nullable String getSearch() {
		return hasMod() ? JEIPlugin.getSearch() : null;
	}

	public static void setSearch(String search) {
		if (hasMod())
			JEIPlugin.setSearch(search);
	}

	@JeiPlugin
	public static final class JEIPlugin implements IModPlugin {
		private static final ResourceLocation PLUGIN_UID = DeepPocketMod.loc("main");
		private static @Nullable IJeiRuntime runtime;

		private static @Nullable String getSearch() {
			return runtime == null ? null : runtime.getIngredientFilter().getFilterText();
		}

		private static void setSearch(String search) {
			if (runtime != null)
				runtime.getIngredientFilter().setFilterText(search);
		}

		@Override
		public ResourceLocation getPluginUid() {
			return PLUGIN_UID;
		}

		@Override
		public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
			registration.addUniversalRecipeTransferHandler(new PocketRecipeTransferHandler(registration.getTransferHelper()));
		}

		@Override
		public void onRuntimeAvailable(IJeiRuntime runtime) {
			JEIPlugin.runtime = runtime;
		}
	}


	private static final class PocketRecipeTransferHandler implements IRecipeTransferHandler<PocketMenu, Recipe<?>> {
		private final IRecipeTransferHandlerHelper transferHelper;

		private PocketRecipeTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
			this.transferHelper = transferHelper;
		}

		@Override
		public Class<PocketMenu> getContainerClass() {
			return PocketMenu.class;
		}

		@Override
		public Optional<MenuType<PocketMenu>> getMenuType() {
			return DeepPocketRegistry.POCKET_MENU.isPresent() ? Optional.of(DeepPocketRegistry.POCKET_MENU.get()) : Optional.empty();
		}

		@SuppressWarnings("unchecked")
		@Override
		public RecipeType<Recipe<?>> getRecipeType() {
			return (RecipeType<Recipe<?>>)(RecipeType<?>)RecipeTypes.CRAFTING;
		}

		@Override
		public @Nullable IRecipeTransferError transferRecipe(PocketMenu container, Recipe<?> recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
			if (!recipe.canCraftInDimensions(3, 3))
				return transferHelper.createUserErrorWithTooltip(Component.literal("Can't craft in 3x3"));
			if (!doTransfer || !player.level.isClientSide)
				return null;
			DeepPocketClientApi.get().setDisplayCrafting(true);
			container.requestRecipeClientBound(recipe);
			return null;
		}
	}
}
