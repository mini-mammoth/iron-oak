package com.minimammoth.ironoak;

import com.minimammoth.ironoak.init.ModItems;
import com.minimammoth.ironoak.init.ModRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

public class WashingRecipe extends AbstractCookingRecipe {
    public static String KEY = "washing";

    public static final int DEFAULT_COOKING_TOTAL_TIME = 200;

    public WashingRecipe(Identifier id, String group, Ingredient input, ItemStack output, float experience, int cookTime) {
        super(ModRecipes.WASHING_RECIPE_TYPE, id, group, input, output, experience, cookTime);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(ModItems.IRON_SHRED);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.WASHING_RECIPE_SERIALIZER;
    }
}
