package com.minimammoth.ironoak.init;

import com.minimammoth.ironoak.BurningRecipe;
import com.minimammoth.ironoak.WashingRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import static com.minimammoth.ironoak.IronOak.MOD_ID;

public class ModRecipes {
    private ModRecipes() {

    }

    /**
     * Cook duration a recipe gets when its JSON omits {@code cookingtime} — which all
     * three burning recipes currently do. Named because {@link com.minimammoth.ironoak.FireBowlEntity}
     * falls back to it when the input has no recipe at all, and the two must not drift.
     */
    public static final int DEFAULT_COOKING_TIME = 200;

    public static final RecipeType<BurningRecipe> BURNING_RECIPE_TYPE;
    public static final RecipeSerializer<BurningRecipe> BURNING_RECIPE_SERIALIZER;

    public static final RecipeType<WashingRecipe> WASHING_RECIPE_TYPE;
    public static final RecipeSerializer<WashingRecipe> WASHING_RECIPE_SERIALIZER;

    static {
        BURNING_RECIPE_TYPE = registerType(BurningRecipe.KEY);
        BURNING_RECIPE_SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(MOD_ID, BurningRecipe.KEY),
                new AbstractCookingRecipe.Serializer<>(BurningRecipe::new, DEFAULT_COOKING_TIME));

        WASHING_RECIPE_TYPE = registerType(WashingRecipe.KEY);
        WASHING_RECIPE_SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(MOD_ID, WashingRecipe.KEY),
                new AbstractCookingRecipe.Serializer<>(WashingRecipe::new, DEFAULT_COOKING_TIME));
    }

    private static <T extends AbstractCookingRecipe> RecipeType<T> registerType(String key) {
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, key), new RecipeType<T>() {
            @Override
            public String toString() {
                return key;
            }
        });
    }

    public static void onInitialize() {
        // Just required to ensure that recipes are loaded before anything else.
    }
}
