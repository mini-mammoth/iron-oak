package com.minimammoth.ironoak;

import com.minimammoth.ironoak.init.ModItems;
import com.minimammoth.ironoak.init.ModRecipes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Burning an ore infused log inside a fire bowl produces ore infused ash.
 */
public class BurningRecipe extends AbstractCookingRecipe {
    public static final String KEY = "burning";

    public BurningRecipe(Recipe.CommonInfo commonInfo, AbstractCookingRecipe.CookingBookInfo bookInfo, Ingredient input, ItemStackTemplate output, float experience, int cookTime) {
        super(commonInfo, bookInfo, input, output, experience, cookTime);
    }

    @Override
    protected Item furnaceIcon() {
        return ModItems.IRON_ASH;
    }

    @Override
    public RecipeSerializer<BurningRecipe> getSerializer() {
        return ModRecipes.BURNING_RECIPE_SERIALIZER;
    }

    @Override
    public RecipeType<BurningRecipe> getType() {
        return ModRecipes.BURNING_RECIPE_TYPE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        // The fire bowl has no recipe book of its own; furnace/misc is the closest
        // vanilla bucket for "smelting a non-food item".
        return RecipeBookCategories.FURNACE_MISC;
    }

    /**
     * Public accessor for the result template, used by tests.
     */
    public ItemStackTemplate getResultTemplate() {
        return result();
    }
}
