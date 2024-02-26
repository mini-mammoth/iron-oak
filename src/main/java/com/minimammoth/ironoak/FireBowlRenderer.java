package com.minimammoth.ironoak;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class FireBowlRenderer implements BlockEntityRenderer<FireBowlEntity> {
    private BlockEntityRendererFactory.Context context;

    public FireBowlRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;
    }

    @Override
    public void render(FireBowlEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var renderer = MinecraftClient.getInstance().getItemRenderer();

        var input = entity.getInput();
        if (!input.isEmpty()) {
            matrices.push();

            matrices.translate(0.5, 0.2, 0.5);
            matrices.scale(2.0f, 2.0f, 2.0f);

            renderer.renderItem(input, ModelTransformationMode.GROUND, light, overlay, matrices, vertexConsumers, entity.getWorld(), 100);
            matrices.pop();
        }

        var output = entity.getOutput();
        if (!output.isEmpty() && !entity.getCachedState().get(FireBowlBlock.LIT)) {
            matrices.push();

            matrices.translate(0.5, 0.4, 0.5);
            matrices.scale(1.3f, 1.3f, 1.3f);

            // Rotate around the Y Axis
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation((tickDelta + entity.getWorld().getTime()) * 0.06F % 360F));

            renderer.renderItem(output, ModelTransformationMode.GROUND, light, overlay, matrices, vertexConsumers, entity.getWorld(), 100);
            matrices.pop();
        }
    }
}
