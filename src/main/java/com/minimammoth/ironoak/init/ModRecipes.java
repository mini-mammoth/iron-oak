package com.minimammoth.ironoak.init;

import com.minimammoth.ironoak.BurningRecipe;
import com.minimammoth.ironoak.WashingRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;

import static com.minimammoth.ironoak.IronOak.MOD_ID;

public class ModRecipes {
    private ModRecipes() {

    }

    public static final RecipeType<BurningRecipe> BURNING_RECIPE_TYPE;
    public static final RecipeSerializer<BurningRecipe> BURNING_RECIPE_SERIALIZER;

    public static final RecipeType<WashingRecipe> WASHING_RECIPE_TYPE;
    public static final RecipeSerializer<WashingRecipe> WASHING_RECIPE_SERIALIZER;

    static {
        BURNING_RECIPE_TYPE = Registry.register(BuiltInRegistries.RECIPE_TYPE, new ResourceLocation(MOD_ID, BurningRecipe.KEY), new RecipeType<BurningRecipe>() {
            @Override
            public String toString() {
                return BurningRecipe.KEY;
            }
        });
        BURNING_RECIPE_SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, new ResourceLocation(MOD_ID, BurningRecipe.KEY), new SimpleCookingSerializer<>(BurningRecipe::new, 200));


        WASHING_RECIPE_TYPE = Registry.register(BuiltInRegistries.RECIPE_TYPE, new ResourceLocation(MOD_ID, WashingRecipe.KEY), new RecipeType<WashingRecipe>() {
            @Override
            public String toString() {
                return WashingRecipe.KEY;
            }
        });
        WASHING_RECIPE_SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, new ResourceLocation(MOD_ID, WashingRecipe.KEY), new SimpleCookingSerializer<>(WashingRecipe::new, 200));
    }

    public static void onInitialize() {
        // Just required to ensure that recipes are loaded before anything else.
    }
}
