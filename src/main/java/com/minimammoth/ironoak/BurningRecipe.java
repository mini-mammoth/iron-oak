package com.minimammoth.ironoak;

import com.minimammoth.ironoak.init.ModItems;
import com.minimammoth.ironoak.init.ModRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class BurningRecipe extends AbstractCookingRecipe {
    public static final String KEY = "burning";

    public BurningRecipe(String group, CookingBookCategory category, Ingredient input, ItemStack output, float experience, int cookTime) {
        super(ModRecipes.BURNING_RECIPE_TYPE, group, category, input, output, experience, cookTime);
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModItems.IRON_ASH);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.BURNING_RECIPE_SERIALIZER;
    }
}
