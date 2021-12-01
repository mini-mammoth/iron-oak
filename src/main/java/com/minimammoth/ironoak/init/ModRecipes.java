package com.minimammoth.ironoak.init;

import com.minimammoth.ironoak.BurningRecipe;
import com.minimammoth.ironoak.WashingRecipe;
import net.minecraft.recipe.CookingRecipeSerializer;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static com.minimammoth.ironoak.IronOak.MOD_ID;

public class ModRecipes {
    private ModRecipes() {

    }

    public static final RecipeType<BurningRecipe> BURNING_RECIPE_TYPE;
    public static final RecipeSerializer<BurningRecipe> BURNING_RECIPE_SERIALIZER;

    public static final RecipeType<WashingRecipe> WASHING_RECIPE_TYPE;
    public static final RecipeSerializer<WashingRecipe> WASHING_RECIPE_SERIALIZER;

    static {
        BURNING_RECIPE_TYPE = Registry.register(Registry.RECIPE_TYPE, new Identifier(MOD_ID, BurningRecipe.KEY), new RecipeType<BurningRecipe>() {
            @Override
            public String toString() {
                return BurningRecipe.KEY;
            }
        });
        BURNING_RECIPE_SERIALIZER = Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(MOD_ID, BurningRecipe.KEY), new CookingRecipeSerializer<>(BurningRecipe::new, 200));


        WASHING_RECIPE_TYPE = Registry.register(Registry.RECIPE_TYPE, new Identifier(MOD_ID, WashingRecipe.KEY), new RecipeType<WashingRecipe>() {
            @Override
            public String toString() {
                return WashingRecipe.KEY;
            }
        });
        WASHING_RECIPE_SERIALIZER = Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(MOD_ID, WashingRecipe.KEY), new CookingRecipeSerializer<>(WashingRecipe::new, 200));
    }

    public static void onInitialize() {
        // Just required to ensure that recipes are loaded before anything else.
    }
}
