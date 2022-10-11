package com.ofek2608.deep_pocket_conversions.api;

import com.ofek2608.deep_pocket.api.struct.ItemType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;

public class BasicRecipeLoader<R extends Recipe<?>> implements IRecipeLoader<R> {
	protected final ResourceLocation id;
	protected @Nullable ForgeConfigSpec.BooleanValue configValue;

	public BasicRecipeLoader(ResourceLocation id) {
		this(id, null);
	}

	public BasicRecipeLoader(ResourceLocation id, @Nullable ForgeConfigSpec.BooleanValue configValue) {
		this.id = id;
		this.configValue = configValue;
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	public boolean checkConfig() {
		return configValue == null || configValue.get();
	}

	@Override
	public void load(Consumer<SimpleRecipe> recipeRegister, R recipe) {
		if (checkConfig())
			load(recipeRegister, recipe.getResultItem(), recipe.getIngredients());
	}

	private void load(Consumer<SimpleRecipe> recipeRegister, ItemStack result, Collection<Ingredient> ingredients) {
		recipeRegister.accept(new SimpleRecipe(
						new SimpleRecipe.ItemCount(new ItemType(result.getItem()), result.getCount()),
						ingredients.stream().map(ing->new SimpleRecipe.IngredientCount(ing, 1)).toArray(SimpleRecipe.IngredientCount[]::new)
		));
	}


}
