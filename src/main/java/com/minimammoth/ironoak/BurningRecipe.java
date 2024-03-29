package com.minimammoth.ironoak;

import com.minimammoth.ironoak.init.ModItems;
import com.minimammoth.ironoak.init.ModRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.util.Identifier;

public class BurningRecipe extends AbstractCookingRecipe {
    public static final String KEY = "burning";

    public BurningRecipe(String group, CookingRecipeCategory category, Ingredient input, ItemStack output, float experience, int cookTime) {
        super(ModRecipes.BURNING_RECIPE_TYPE, group, category, input, output, experience, cookTime);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(ModItems.IRON_ASH);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.BURNING_RECIPE_SERIALIZER;
    }
}
