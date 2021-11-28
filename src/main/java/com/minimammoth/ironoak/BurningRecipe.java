package com.minimammoth.ironoak;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

public class BurningRecipe extends AbstractCookingRecipe {
    public BurningRecipe(Identifier id, String group, Ingredient input, ItemStack output, float experience, int cookTime) {
        super(IronOak.BURNING_RECIPE_TYPE, id, group, input, output, experience, cookTime);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(IronOak.IRON_ASH);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return IronOak.BURNING_RECIPE_SERIALIZER;
    }
}
