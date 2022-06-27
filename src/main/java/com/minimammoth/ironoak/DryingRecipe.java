package com.minimammoth.ironoak;

import com.minimammoth.ironoak.init.ModBlocks;
import com.minimammoth.ironoak.init.ModRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

public class DryingRecipe extends AbstractCookingRecipe {
    public static final String KEY = "drying";

    public DryingRecipe(Identifier id, String group, Ingredient input, ItemStack output, float experience, int cookTime) {
        super(ModRecipes.DRYING_RECIPE_TYPE, id, group, input, output, experience, cookTime);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(ModBlocks.DRY_RACK.asItem());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.BURNING_RECIPE_SERIALIZER;
    }
}
