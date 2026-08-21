package com.minimammoth.ironoak;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * See related {@code FireBowlBlock} and {@code FireBowlEntity}.
 * <p>
 * The two-phase render-state split ({@code extractRenderState}/{@code submit}) is a
 * 1.21.9+ redesign; this version renders in one pass straight from the block entity.
 */
@Environment(EnvType.CLIENT)
public class FireBowlRenderer implements BlockEntityRenderer<FireBowlEntity> {
    private final ItemRenderer itemRenderer;

    public FireBowlRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(FireBowlEntity entity, float partialTick, PoseStack matrices, MultiBufferSource bufferSource, int light, int overlay) {
        ItemStack input = entity.getInput();
        if (!input.isEmpty()) {
            matrices.pushPose();

            matrices.translate(0.5f, 0.2f, 0.5f);
            matrices.scale(2.0f, 2.0f, 2.0f);

            itemRenderer.renderStatic(input, ItemDisplayContext.GROUND, light, overlay, matrices, bufferSource, entity.getLevel(), 0);
            matrices.popPose();
        }

        // The output only shows once the fire has gone out.
        ItemStack output = entity.getOutput();
        if (!output.isEmpty() && !entity.getBlockState().getValue(FireBowlBlock.LIT)) {
            matrices.pushPose();

            matrices.translate(0.5f, 0.4f, 0.5f);
            matrices.scale(1.3f, 1.3f, 1.3f);

            // Rotate around the Y axis
            float spinDegrees = (partialTick + entity.getLevel().getGameTime()) * 0.06f % 360f;
            matrices.mulPose(Axis.YP.rotation(spinDegrees));

            itemRenderer.renderStatic(output, ItemDisplayContext.GROUND, light, overlay, matrices, bufferSource, entity.getLevel(), 0);
            matrices.popPose();
        }
    }
}
