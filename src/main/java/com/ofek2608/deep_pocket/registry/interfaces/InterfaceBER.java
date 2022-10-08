package com.ofek2608.deep_pocket.registry.interfaces;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class InterfaceBER<BE extends BlockEntityWithPocket> implements BlockEntityRenderer<BE> {
	private final BlockEntityRendererProvider.Context ctx;

	public InterfaceBER(BlockEntityRendererProvider.Context ctx) {
		this.ctx = ctx;
	}

	@Override
	public void render(BE blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		float time = ctx.getBlockEntityRenderDispatcher().level.getGameTime() + partialTick;
		ItemStack displayItem = blockEntity instanceof BlockEntityWithPocketFilter filter ? filter.getFilter().create() : ItemStack.EMPTY;

		this.renderCube(poseStack.last().pose(), bufferSource.getBuffer(RenderType.endPortal()));
		if (!displayItem.isEmpty())
			this.renderItem(displayItem, time, poseStack, bufferSource, packedLight);
	}











	public void renderItem(ItemStack itemStack, float time, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		poseStack.pushPose();
		poseStack.translate(0.5, 0.3125, 0.5);
		poseStack.mulPose(Vector3f.YP.rotation(time / 20.0F));

		BakedModel model = ctx.getItemRenderer().getModel(itemStack, ctx.getBlockEntityRenderDispatcher().level, null, 0);
		ctx.getItemRenderer().render(itemStack, ItemTransforms.TransformType.GROUND, false, poseStack, bufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY, model);
		poseStack.popPose();
	}








	private void renderCube(Matrix4f pPose, VertexConsumer pConsumer) {
		float c0 = 1;
		float c1 = 0;
		this.renderFace(pPose, pConsumer, c0, c1, c0, c1, c1, c1, c1, c1, Direction.SOUTH);
		this.renderFace(pPose, pConsumer, c0, c1, c1, c0, c0, c0, c0, c0, Direction.NORTH);
		this.renderFace(pPose, pConsumer, c1, c1, c1, c0, c0, c1, c1, c0, Direction.EAST);
		this.renderFace(pPose, pConsumer, c0, c0, c0, c1, c0, c1, c1, c0, Direction.WEST);
		this.renderFace(pPose, pConsumer, c0, c1, c0, c0, c0, c0, c1, c1, Direction.DOWN);
		this.renderFace(pPose, pConsumer, c0, c1, c1, c1, c1, c1, c0, c0, Direction.UP);
	}

	private void renderFace(Matrix4f pPose, VertexConsumer pConsumer, float pX0, float pX1, float pY0, float pY1, float pZ0, float pZ1, float pZ2, float pZ3, Direction pDirection) {
		pConsumer.vertex(pPose, pX0, pY0, pZ0).endVertex();
		pConsumer.vertex(pPose, pX1, pY0, pZ1).endVertex();
		pConsumer.vertex(pPose, pX1, pY1, pZ2).endVertex();
		pConsumer.vertex(pPose, pX0, pY1, pZ3).endVertex();
	}
}
