package com.minimammoth.ironoak;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class FireBowlRenderer implements BlockEntityRenderer<FireBowlEntity> {
    private BlockEntityRendererProvider.Context context;

    public FireBowlRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(FireBowlEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        var renderer = Minecraft.getInstance().getItemRenderer();

        var input = entity.getInput();
        if (!input.isEmpty()) {
            matrices.pushPose();

            matrices.translate(0.5, 0.2, 0.5);
            matrices.scale(2.0f, 2.0f, 2.0f);

            renderer.renderStatic(input, ItemDisplayContext.GROUND, light, overlay, matrices, vertexConsumers, entity.getLevel(), 100);
            matrices.popPose();
        }

        var output = entity.getOutput();
        if (!output.isEmpty() && !entity.getBlockState().getValue(FireBowlBlock.LIT)) {
            matrices.pushPose();

            matrices.translate(0.5, 0.4, 0.5);
            matrices.scale(1.3f, 1.3f, 1.3f);

            // Rotate around the Y Axis
            matrices.mulPose(Axis.YP.rotation((tickDelta + entity.getLevel().getGameTime()) * 0.06F % 360F));

            renderer.renderStatic(output, ItemDisplayContext.GROUND, light, overlay, matrices, vertexConsumers, entity.getLevel(), 100);
            matrices.popPose();
        }
    }
}
