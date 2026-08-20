package com.minimammoth.ironoak;

import com.minimammoth.ironoak.init.ModItems;
import com.minimammoth.ironoak.init.ModRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class WashingRecipe extends AbstractCookingRecipe {
    public static String KEY = "washing";

    public WashingRecipe(String group, CookingBookCategory category, Ingredient input, ItemStack output, float experience, int cookTime) {
        super(ModRecipes.WASHING_RECIPE_TYPE, group, category, input, output, experience, cookTime);
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModItems.IRON_SHRED);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.WASHING_RECIPE_SERIALIZER;
    }
}
