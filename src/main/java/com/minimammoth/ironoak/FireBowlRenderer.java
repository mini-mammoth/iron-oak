package com.minimammoth.ironoak;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * See related {@code FireBowlBlock} and {@code FireBowlEntity}.
 * <p>
 * Since 1.21.9 block entity rendering is split in two: {@link #extractRenderState} copies
 * everything the renderer needs off the block entity on the main thread, and
 * {@link #submit} draws purely from that snapshot. Nothing here may touch the block
 * entity or the level.
 */
@Environment(EnvType.CLIENT)
public class FireBowlRenderer implements BlockEntityRenderer<FireBowlEntity, FireBowlRenderer.FireBowlRenderState> {
    private final ItemModelResolver itemModelResolver;

    public FireBowlRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public FireBowlRenderState createRenderState() {
        return new FireBowlRenderState();
    }

    @Override
    public void extractRenderState(FireBowlEntity entity, FireBowlRenderState state, float partialTick,
                                   Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTick, cameraPos, breakProgress);

        int seed = (int) entity.getBlockPos().asLong();

        state.hasInput = !entity.getInput().isEmpty();
        if (state.hasInput) {
            itemModelResolver.updateForTopItem(state.input, entity.getInput(), ItemDisplayContext.GROUND,
                    entity.getLevel(), null, seed);
        }

        // The output only shows once the fire has gone out.
        state.showOutput = !entity.getOutput().isEmpty()
                && !entity.getBlockState().getValue(FireBowlBlock.LIT);
        if (state.showOutput) {
            itemModelResolver.updateForTopItem(state.output, entity.getOutput(), ItemDisplayContext.GROUND,
                    entity.getLevel(), null, seed + 1);
        }

        state.spinDegrees = entity.getLevel() == null
                ? 0f
                : (partialTick + entity.getLevel().getGameTime()) * 0.06f % 360f;
    }

    @Override
    public void submit(FireBowlRenderState state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.hasInput && !state.input.isEmpty()) {
            matrices.pushPose();

            matrices.translate(0.5f, 0.2f, 0.5f);
            matrices.scale(2.0f, 2.0f, 2.0f);

            state.input.submit(matrices, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            matrices.popPose();
        }

        if (state.showOutput && !state.output.isEmpty()) {
            matrices.pushPose();

            matrices.translate(0.5f, 0.4f, 0.5f);
            matrices.scale(1.3f, 1.3f, 1.3f);

            // Rotate around the Y axis
            matrices.mulPose(Axis.YP.rotation(state.spinDegrees));

            state.output.submit(matrices, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            matrices.popPose();
        }
    }

    public static class FireBowlRenderState extends BlockEntityRenderState {
        public final ItemStackRenderState input = new ItemStackRenderState();
        public final ItemStackRenderState output = new ItemStackRenderState();
        public boolean hasInput;
        public boolean showOutput;
        public float spinDegrees;
    }
}
