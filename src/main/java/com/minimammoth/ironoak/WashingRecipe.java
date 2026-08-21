package com.minimammoth.ironoak;

import com.minimammoth.ironoak.init.ModItems;
import com.minimammoth.ironoak.init.ModRecipes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Washing ore infused ash in water produces ore shreds.
 */
public class WashingRecipe extends AbstractCookingRecipe {
    public static final String KEY = "washing";

    public WashingRecipe(String group, CookingBookCategory category, Ingredient input, ItemStack output, float experience, int cookTime) {
        super(group, category, input, output, experience, cookTime);
    }

    @Override
    protected Item furnaceIcon() {
        return ModItems.IRON_SHRED;
    }

    @Override
    public RecipeSerializer<WashingRecipe> getSerializer() {
        return ModRecipes.WASHING_RECIPE_SERIALIZER;
    }

    @Override
    public RecipeType<WashingRecipe> getType() {
        return ModRecipes.WASHING_RECIPE_TYPE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.FURNACE_MISC;
    }
}
