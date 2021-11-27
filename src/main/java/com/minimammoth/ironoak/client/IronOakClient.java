package com.minimammoth.ironoak.client;

import com.minimammoth.ironoak.IronOak;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class IronOakClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This is required to see the sapling transparent like all other saplings. If not set the background will
        // appear black.
        BlockRenderLayerMap.INSTANCE.putBlock(IronOak.IRON_OAK_SAPLING, RenderLayer.getCutout());
    }
}
