package com.ofek2608.deep_pocket.registry;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ofek2608.deep_pocket.DeepPocketMod;
import com.ofek2608.deep_pocket.api.struct.ItemTypeAmount;
import com.ofek2608.deep_pocket.registry.items.crafting_pattern.CraftingPatternItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.ModelEvent;

public final class DeepPocketBEWLR extends BlockEntityWithoutLevelRenderer {
	public static final DeepPocketBEWLR INSTANCE = new DeepPocketBEWLR(
					Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels()
	);
	private static final ResourceLocation MODEL_LOC_NORMAL_CRAFTING_PATTERN = DeepPocketMod.loc("item/normal_crafting_pattern");
	private final BlockEntityRenderDispatcher dispatcher;
	private final EntityModelSet modelSet;

	public DeepPocketBEWLR(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		this.dispatcher = dispatcher;
		this.modelSet = modelSet;
	}

	static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
		event.register(MODEL_LOC_NORMAL_CRAFTING_PATTERN);
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {

	}

	@Override
	public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		if (stack.getItem() == DeepPocketRegistry.CRAFTING_PATTERN_ITEM.get()) {
			renderCraftingPattern(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
		}
	}

	private void renderCraftingPattern(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		ItemStack renderStack = CraftingPatternItem.getCachedDisplayedResult(stack);
		if (!Screen.hasShiftDown())
			renderStack = ItemStack.EMPTY;
		if (transformType != ItemTransforms.TransformType.GUI)
			renderStack = ItemStack.EMPTY;


		BakedModel renderModel;

		if (renderStack.isEmpty()) {
			renderStack = new ItemStack(Items.NETHER_STAR);//Gives enchant effect
			renderModel = Minecraft.getInstance().getModelManager().getModel(MODEL_LOC_NORMAL_CRAFTING_PATTERN);
		} else {
			renderModel = Minecraft.getInstance().getItemRenderer().getModel(renderStack, null, null, 0);
		}

		boolean renderItem = transformType == ItemTransforms.TransformType.GUI && !renderModel.usesBlockLight();
		if (renderItem)
			Lighting.setupForFlatItems();

		poseStack.pushPose();
		bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
		boolean leftHand = transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND || transformType == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND;
		poseStack.translate(0.5F, 0.5F, 0.5F);
		Minecraft.getInstance().getItemRenderer().render(
						renderStack,
						transformType,
						leftHand,
						poseStack,
						bufferSource,
						packedLight,
						packedOverlay,
						renderModel
		);
		poseStack.popPose();

		if (renderItem) {
			((MultiBufferSource.BufferSource)bufferSource).endBatch();
			Lighting.setupFor3DItems();
		}
	}
}
