package com.ofek2608.deep_pocket_conversions.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BasicRecipeLoader<R extends Recipe<?>> implements IRecipeLoader<R> {
	protected final ResourceLocation id;
	protected final @Nullable ForgeConfigSpec.BooleanValue configValue;

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
	public void load(Consumer<ICompilableRecipe> recipeRegister, R recipe) {
		if (checkConfig())
			recipeRegister.accept(new SimpleCompilableRecipe(recipe));
	}
}
