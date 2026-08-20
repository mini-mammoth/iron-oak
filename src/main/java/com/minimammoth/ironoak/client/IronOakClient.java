package com.minimammoth.ironoak.client;

import com.minimammoth.ironoak.FireBowlRenderer;
import com.minimammoth.ironoak.init.ModBlocks;
import com.minimammoth.ironoak.init.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

@Environment(EnvType.CLIENT)
public class IronOakClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Saplings and the fire bowl need the cutout layer, otherwise their transparent
        // pixels render black.
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT,
                ModBlocks.COPPER_OAK_SAPLING,
                ModBlocks.GOLD_OAK_SAPLING,
                ModBlocks.IRON_OAK_SAPLING,

                ModBlocks.COPPER_ACACIA_SAPLING,
                ModBlocks.GOLD_ACACIA_SAPLING,
                ModBlocks.IRON_ACACIA_SAPLING,

                ModBlocks.COPPER_JUNGLE_SAPLING,
                ModBlocks.GOLD_JUNGLE_SAPLING,
                ModBlocks.IRON_JUNGLE_SAPLING,

                ModBlocks.COPPER_BIRCH_SAPLING,
                ModBlocks.GOLD_BIRCH_SAPLING,
                ModBlocks.IRON_BIRCH_SAPLING,

                ModBlocks.COPPER_SPRUCE_SAPLING,
                ModBlocks.GOLD_SPRUCE_SAPLING,
                ModBlocks.IRON_SPRUCE_SAPLING,

                ModBlocks.COPPER_DARK_OAK_SAPLING,
                ModBlocks.GOLD_DARK_OAK_SAPLING,
                ModBlocks.IRON_DARK_OAK_SAPLING,

                ModBlocks.FIRE_BOWL);

        BlockEntityRendererRegistry.register(ModEntityTypes.FIRE_BOWL_ENTITY, FireBowlRenderer::new);
    }
}
