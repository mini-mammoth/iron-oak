package com.minimammoth.ironoak.client;

import com.minimammoth.ironoak.FireBowlRenderer;
import com.minimammoth.ironoak.init.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class IronOakClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Saplings and the fire bowl use cutout rendering. In 26.2 the render layer is
        // declared in the block model JSON via "render_type": "cutout", so no code
        // registration is needed.
        BlockEntityRendererRegistry.register(ModEntityTypes.FIRE_BOWL_ENTITY, FireBowlRenderer::new);
    }
}
