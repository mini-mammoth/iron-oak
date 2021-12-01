package com.minimammoth.ironoak;

import com.minimammoth.ironoak.init.ModBlocks;
import com.minimammoth.ironoak.init.ModConfiguredFeatures;
import com.minimammoth.ironoak.init.ModEntityTypes;
import com.minimammoth.ironoak.init.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.recipe.CookingRecipeSerializer;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class IronOak implements ModInitializer {
    public static final String MOD_ID = "iron_oak";



    @Override
    public void onInitialize() {
        ModBlocks.onInitialize();
        ModEntityTypes.onInitialize();
        ModItems.onInitialize();
        ModConfiguredFeatures.onInitialize();
    }

    static {

    }
}
