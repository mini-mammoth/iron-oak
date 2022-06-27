package com.minimammoth.ironoak;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.Quaternion;

@Environment(EnvType.CLIENT)
public class DryRackRenderer implements BlockEntityRenderer<DryRackEntity> {
    private BlockEntityRendererFactory.Context context;

    public DryRackRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;
    }

    @Override
    public void render(DryRackEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var renderer = MinecraftClient.getInstance().getItemRenderer();

        var input = entity.getInput();
        if (!input.isEmpty()) {
            matrices.push();

            matrices.translate(0.5, 0.3, 0.5);

            if (entity.getCachedState().get(DryRackBlock.TYPE) == SlabType.TOP) {
                matrices.translate(0, .5, 0);
            }

            matrices.multiply(Quaternion.fromEulerXyz((float) (Math.PI / 2f), 0f, 0f));

            if (input.getItem() instanceof BlockItem) {
                // Stretch Leave Blocks to look more natural
                matrices.scale(1.1f, 1.1f, 0.3f);
            } else {
                matrices.scale(0.6f, 0.6f, 0.6f);
            }

            renderer.renderItem(input, ModelTransformation.Mode.FIXED, light, overlay, matrices, vertexConsumers, 100);
            matrices.pop();
        }

        var output = entity.getOutput();
        if (!output.isEmpty()) {
            matrices.push();

            matrices.translate(0.5, 0.3, 0.5);
            
            if (entity.getCachedState().get(DryRackBlock.TYPE) == SlabType.TOP) {
                matrices.translate(0, .5, 0);
            }

            matrices.scale(0.6f, 0.6f, 0.6f);
            matrices.multiply(Quaternion.fromEulerXyz((float) (Math.PI / 2f), 0f, 0f));

            renderer.renderItem(output, ModelTransformation.Mode.FIXED, light, overlay, matrices, vertexConsumers, 100);
            matrices.pop();
        }
    }
}
