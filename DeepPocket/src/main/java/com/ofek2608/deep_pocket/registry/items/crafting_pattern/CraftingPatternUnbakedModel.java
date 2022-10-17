package com.ofek2608.deep_pocket.registry.items.crafting_pattern;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.ofek2608.deep_pocket.DeepPocketMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.EmptyModel;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class CraftingPatternUnbakedModel implements IUnbakedGeometry<CraftingPatternUnbakedModel> {
	public static final CraftingPatternUnbakedModel INSTANCE = new CraftingPatternUnbakedModel();
	public static final IGeometryLoader<CraftingPatternUnbakedModel> LOADER = (jsonObject, deserializationContext) -> INSTANCE;

	private Material getParticleMaterial() {
		return new Material(TextureAtlas.LOCATION_BLOCKS, DeepPocketMod.loc("item/normal_crafting_pattern"));
	}

	@Override
	public BakedModel bake(IGeometryBakingContext context, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
		BakedModel defaultModel = bakery.bake(DeepPocketMod.loc("item/normal_crafting_pattern"), modelState, spriteGetter);

		return new SimpleBakedModel(
						Collections.emptyList(),
						Collections.emptyMap(),
						false, false, false,
						spriteGetter.apply(getParticleMaterial()),
						ItemTransforms.NO_TRANSFORMS,
						new CraftingPatternItemOverrides(defaultModel == null ? EmptyModel.BAKED : defaultModel),
						RenderTypeGroup.EMPTY
		);
	}

	@Override
	public Collection<Material> getMaterials(IGeometryBakingContext context, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		return Collections.emptyList();
	}

public static class CraftingPatternItemOverrides extends ItemOverrides {
	private final BakedModel defaultModel;
	public CraftingPatternItemOverrides(BakedModel defaultModel) {
		this.defaultModel = defaultModel;
	}

	@Nullable
	@Override
	public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
		if (!Screen.hasShiftDown())
			return defaultModel;
		ItemStack displayedResult = CraftingPatternItem.getCachedDisplayedResult(stack);
		if (displayedResult.isEmpty())
			return defaultModel;
		return Minecraft.getInstance().getItemRenderer().getModel(displayedResult, null, null, 0);
	}
}
}
