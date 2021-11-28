package com.minimammoth.ironoak;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class FireBowlRenderer implements BlockEntityRenderer<FireBowlEntity> {
    private BlockEntityRendererFactory.Context context;

    public FireBowlRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;
    }

    @Override
    public void render(FireBowlEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var renderer = MinecraftClient.getInstance().getItemRenderer();

        matrices.push();

        var input = entity.getInput();
        if (!input.isEmpty()) {
            matrices.translate(0.5, 0.2, 0.5);
            matrices.scale(2.0f, 2.0f, 2.0f);

            renderer.renderItem(input, ModelTransformation.Mode.GROUND, light, overlay, matrices, vertexConsumers, 100);
        }

        matrices.pop();
    }
}
