package com.minimammoth.ironoak.client;

import com.minimammoth.ironoak.FireBowlRenderer;
import com.minimammoth.ironoak.init.ModBlocks;
import com.minimammoth.ironoak.init.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class IronOakClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This is required to see the sapling transparent like all other saplings. If not set the background will
        // appear black.
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.COPPER_OAK_SAPLING, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GOLD_OAK_SAPLING, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.IRON_OAK_SAPLING, RenderLayer.getCutout());

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.FIRE_BOWL, RenderLayer.getCutout());

        BlockEntityRendererRegistry.register(ModEntityTypes.FIRE_BOWL_ENTITY, FireBowlRenderer::new);
    }
}
