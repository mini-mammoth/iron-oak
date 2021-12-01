package com.minimammoth.ironoak;

import com.minimammoth.ironoak.init.ModBlocks;
import com.minimammoth.ironoak.init.ModConfiguredFeatures;
import com.minimammoth.ironoak.init.ModEntityTypes;
import com.minimammoth.ironoak.init.ModItems;
import com.minimammoth.ironoak.init.ModRecipes;
import net.fabricmc.api.ModInitializer;

public class IronOak implements ModInitializer {
    public static final String MOD_ID = "iron_oak";

    @Override
    public void onInitialize() {
        ModRecipes.onInitialize();

        ModBlocks.onInitialize();
        ModEntityTypes.onInitialize();
        ModItems.onInitialize();
        ModConfiguredFeatures.onInitialize();
    }
}
