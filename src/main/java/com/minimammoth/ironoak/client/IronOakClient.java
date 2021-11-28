package com.minimammoth.ironoak.client;

import com.minimammoth.ironoak.FireBowlEntity;
import com.minimammoth.ironoak.FireBowlRenderer;
import com.minimammoth.ironoak.IronOak;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class IronOakClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This is required to see the sapling transparent like all other saplings. If not set the background will
        // appear black.
        BlockRenderLayerMap.INSTANCE.putBlock(IronOak.IRON_OAK_SAPLING, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(IronOak.FIRE_BOWL, RenderLayer.getCutout());

        BlockEntityRendererRegistry.register(IronOak.FIRE_BOWL_ENTITY, FireBowlRenderer::new);
    }
}
