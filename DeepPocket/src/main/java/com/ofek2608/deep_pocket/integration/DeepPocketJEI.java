package com.ofek2608.deep_pocket.integration;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.logging.LogUtils;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.DeepPocketClientApi;
import com.ofek2608.deep_pocket.api.events.DeepPocketConversionsUpdatedEvent;
import com.ofek2608.deep_pocket.api.struct.ElementConversions;
import com.ofek2608.deep_pocket.api.struct.ElementType;
import com.ofek2608.deep_pocket.api.struct.client.ClientElementIndices;
import com.ofek2608.deep_pocket.registry.DeepPocketRegistry;
import com.ofek2608.deep_pocket.registry.pocket_screen.PocketMenu;
import com.ofek2608.deep_pocket.registry.pocket_screen.PocketScreen;
import com.ofek2608.deep_pocket.utils.AdvancedLongMath;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;


public final class DeepPocketJEI {
	private static final Logger LOGGER = LogUtils.getLogger();
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

	@Mod.EventBusSubscriber(modid = DeepPocketMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	private static final class ForgeEvents {
		@SubscribeEvent
		public static void event(DeepPocketConversionsUpdatedEvent event) {
			if (hasMod())
				JEIPlugin.setConversionsKeys(event.getConversions().getKeys(), event.getApi().getElementIndices());
		}
	}

	@JeiPlugin
	public static final class JEIPlugin implements IModPlugin {
		private static final ResourceLocation PLUGIN_UID = DeepPocketMod.loc("main");
		private static @Nullable IJeiRuntime runtime;
		private static final Set<ElementType> existingRecipes = new HashSet<>();
		private static final Set<ElementType> displayedRecipes = new HashSet<>();

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
		public void registerCategories(IRecipeCategoryRegistration registration) {
			registration.addRecipeCategories(new ElementConversionRecipeCategory(registration.getJeiHelpers()));
		}

		@Override
		public void registerRecipes(IRecipeRegistration registration) {

		}

		@Override
		public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
			registration.addUniversalRecipeTransferHandler(new PocketRecipeTransferHandler(registration.getTransferHelper()));
		}

		@Override
		public void registerGuiHandlers(IGuiHandlerRegistration registration) {
			registration.addGhostIngredientHandler(PocketScreen.class, new PocketScreenGhostIngredientHandler());
		}

		@Override
		public void onRuntimeAvailable(IJeiRuntime runtime) {
			JEIPlugin.runtime = runtime;
			existingRecipes.clear();
			displayedRecipes.clear();
			DeepPocketClientApi api = DeepPocketClientApi.get();
			setConversionsKeys(api.getConversions().getKeys(), api.getElementIndices());
		}

		public static void setConversionsKeys(Set<Integer> keys, ClientElementIndices indices) {
			if (runtime == null)
				return;
			IRecipeManager manager = runtime.getRecipeManager();
			//Add new recipes
			manager.addRecipes(ElementConversionRecipeCategory.RECIPE_TYPE, keys.stream().map(indices::getType).filter(type->!existingRecipes.contains(type)).toList());
			//UnHide recipe that had been added
			manager.unhideRecipes(ElementConversionRecipeCategory.RECIPE_TYPE, keys.stream().map(indices::getType).filter(existingRecipes::contains).toList());
			//Hide recipes that were before and shouldn't exist
			manager.hideRecipes(ElementConversionRecipeCategory.RECIPE_TYPE, displayedRecipes.stream().filter(type->!keys.contains(indices.getIndex(type))).toList());
			
			displayedRecipes.clear();
			for (Integer key : keys) {
				ElementType type = indices.getType(key);
				existingRecipes.add(type);
				displayedRecipes.add(type);
			}
		}
	}

	private static final class ElementConversionRecipeCategory implements IRecipeCategory<ElementType> {
		private static final RecipeType<ElementType> RECIPE_TYPE = new RecipeType<>(DeepPocketMod.loc("pocket_item_conversions"), ElementType.class);
		private static final ResourceLocation BACKGROUND_TEXTURE_LOC = DeepPocketMod.loc("textures/gui/jei/pocket_item_conversions.png");

		private final IDrawable background;
		private final IDrawable icon;

		private ElementConversionRecipeCategory(IJeiHelpers jeiHelpers) {
			this.background = jeiHelpers.getGuiHelper().createDrawable(BACKGROUND_TEXTURE_LOC, 0, 0, 175, 50);
			this.icon = jeiHelpers.getGuiHelper().createDrawableItemStack(new ItemStack(DeepPocketRegistry.POCKET_LINK_ITEM.get()));
		}

		@Override
		public RecipeType<ElementType> getRecipeType() {
			return RECIPE_TYPE;
		}

		@Override
		public Component getTitle() {
			return Component.literal("Pocket Item Conversions");
		}

		@Override
		public IDrawable getBackground() {
			return background;
		}

		@Override
		public IDrawable getIcon() {
			return icon;
		}

		private int getSlotX(int slotIndex) {
			return 1 + (slotIndex % 9) * 16;
		}

		private int getSlotY(int slotIndex) {
			return 1 + (slotIndex / 9) * 16;
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, ElementType recipe, IFocusGroup focuses) {
			DeepPocketClientApi api = DeepPocketClientApi.get();
			ElementConversions conversions = api.getConversions();
			ClientElementIndices elementIndices = api.getElementIndices();
			long[] value = conversions.getValue(elementIndices.getIndex(recipe));
			if (value == null) {
				LOGGER.warn("Jei displays empty recipe");
				return;
			}
			int slotIndex = 0;
			for (int i = 0; i < value.length; i++) {
				if (value[i] == 0)
					continue;
				//TODO custom ingredient type
//				ElementType.TConvertible type = conversions.getBaseElement(i);
//				if (slotIndex >= 27) {
//					builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
//									.addItemStack(stack);
//				} else {
//					builder.addSlot(RecipeIngredientRole.INPUT, getSlotX(slotIndex), getSlotY(slotIndex))
//									.addItemStack(stack)
//									.setOverlay(new ItemCountOverlay(value[i]), 0, 0);
//				}
				slotIndex++;
			}
			{
				IRecipeSlotBuilder slotBuilder = builder.addSlot(RecipeIngredientRole.OUTPUT, 158, 33);
				if (recipe instanceof ElementType.TItem item)
					slotBuilder.addItemStack(item.create());
				else if (recipe instanceof ElementType.TFluid fluid)
					slotBuilder.addFluidStack(fluid.getFluid(), 1);
				//TODO more types
			}
			
			//TODO if there are 27 or more items, show in a tooltip
		}

		private static final class ItemCountOverlay implements IDrawable {
			private final long count;

			private ItemCountOverlay(long count) {
				this.count = count;
			}

			@Override
			public int getWidth() {
				return 16;
			}

			@Override
			public int getHeight() {
				return 16;
			}

			@Override
			public void draw(PoseStack poseStack, int xOffset, int yOffset) {
				if (count == 1)
					return;
				Font font = Minecraft.getInstance().font;

				poseStack.pushPose();
				String displayText = AdvancedLongMath.advancedToString(count, 6);
				poseStack.translate(0.0D, 0.0D, Minecraft.getInstance().getItemRenderer().blitOffset + 200.0F);
				poseStack.scale(0.5f, 0.5f, 1f);
				MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
				font.drawInBatch(displayText, (float)(xOffset * 2 + 32 - font.width(displayText)), (float)(yOffset * 2 + 24), 0xFFFFFF, true, poseStack.last().pose(), bufferSource, false, 0, 0xF000F0);
				bufferSource.endBatch();
				poseStack.popPose();
			}
		}
	}


	private static final class PocketRecipeTransferHandler implements IRecipeTransferHandler<PocketMenu, Object> {
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
		public RecipeType<Object> getRecipeType() {
			return (RecipeType<Object>)(RecipeType<?>)RecipeTypes.CRAFTING;
		}

		@Override
		public @Nullable IRecipeTransferError transferRecipe(PocketMenu container, Object recipeObj, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
			if (!(recipeObj instanceof Recipe<?> recipe))
				return transferHelper.createInternalError();
			if (!recipe.canCraftInDimensions(3, 3))
				return transferHelper.createUserErrorWithTooltip(Component.literal("Can't craft in 3x3"));
			if (!doTransfer || !player.level.isClientSide)
				return null;
			container.requestRecipeClientBound(recipe);
			return null;
		}
	}

	private static final class PocketScreenGhostIngredientHandler implements IGhostIngredientHandler<PocketScreen> {

		@Contract(pure = true)
		@Unmodifiable
		@Override
		public <I> List<Target<I>> getTargets(PocketScreen gui, I ingredient, boolean doStart) {
			if (getAsItemStack(ingredient).isEmpty())
				return Collections.emptyList();
			int count = gui.getJEITargetCount();
			List<Target<I>> result = new ArrayList<>();
			for (int i = 0; i < count; i++) {
				int finalI = i;
				result.add(new Target<>() {
					@Override
					public Rect2i getArea() {
						return gui.getJEITargetArea(finalI);
					}

					@Override
					public void accept(I ingredient) {
						getAsItemStack(ingredient).ifPresent(stack->gui.acceptJEIGhostIngredient(finalI, stack));
					}
				});
			}
			return result;
		}

		private static Optional<ItemStack> getAsItemStack(Object ingredient) {
			return ingredient instanceof ItemStack stack ? Optional.of(stack) :
							ingredient instanceof ITypedIngredient<?> typedIngredient && typedIngredient.getIngredient() instanceof ItemStack stack ? Optional.of(stack) :
											Optional.empty();
		}

		@Override
		public void onComplete() {}

		@Override
		public boolean shouldHighlightTargets() {
			return false;
		}
	}
}
