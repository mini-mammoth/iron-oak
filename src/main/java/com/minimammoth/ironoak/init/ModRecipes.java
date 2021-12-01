package com.minimammoth.ironoak.init;

import com.minimammoth.ironoak.BurningRecipe;
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

    static {
        BURNING_RECIPE_TYPE = Registry.register(Registry.RECIPE_TYPE, new Identifier(MOD_ID, "burning"), new RecipeType<BurningRecipe>() {
            @Override
            public String toString() {
                return "burning";
            }
        });

        BURNING_RECIPE_SERIALIZER = Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(MOD_ID, "burning"), new CookingRecipeSerializer<>(BurningRecipe::new, 200));
    }
}
