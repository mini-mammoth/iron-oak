package com.minimammoth.ironoak;

import com.minimammoth.ironoak.init.ModBlocks;
import com.minimammoth.ironoak.init.ModEntityTypes;
import com.minimammoth.ironoak.init.ModItems;
import com.minimammoth.ironoak.init.ModRecipes;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IronOak implements ModInitializer {
    public static final String MOD_ID = "iron_oak";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModRecipes.onInitialize();

        ModBlocks.onInitialize();
        ModEntityTypes.onInitialize();
        ModItems.onInitialize();
//        ModConfiguredFeatures.bootstrap();
    }
}
