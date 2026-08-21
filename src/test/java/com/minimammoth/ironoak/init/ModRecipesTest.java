package com.minimammoth.ironoak.init;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minimammoth.ironoak.BootstrappedGame;
import com.minimammoth.ironoak.BurningRecipe;
import com.minimammoth.ironoak.Matrix;
import com.minimammoth.ironoak.Resources;
import com.minimammoth.ironoak.TagBinding;
import com.minimammoth.ironoak.WashingRecipe;
import com.minimammoth.ironoak.requirements.Requirement;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The committed recipe JSON, run through the serializer that will read it in game.
 *
 * <p>{@code BurningRecipe} and {@code WashingRecipe} are thin subclasses of
 * {@code AbstractCookingRecipe} — all the behaviour is in the serializer and the JSON, so
 * that is what gets tested. No level, no fire.
 *
 * <p>The number that matters is the cook duration, and it reaches the player through three
 * hops: the JSON, which omits {@code cookingtime} for every burning and washing recipe;
 * {@link ModRecipes#DEFAULT_COOKING_TIME}, which the serializer therefore supplies; and
 * {@code FireBowlEntity}, which falls back to the same constant when an input matches no
 * recipe at all. Break any hop and a log burns for a different length of time with nothing
 * to say so.
 */
class ModRecipesTest {

    // Resolved in @BeforeAll, never in a static initialiser: a static field runs when JUnit
    // loads the class, which is before any extension has bootstrapped the game, and touching
    // BuiltInRegistries that early poisons its class initialiser for the whole JVM.
    private static RegistryAccess.Frozen registries;
    private static RegistryOps<JsonElement> ops;

    /**
     * A burning recipe's ingredient is a tag, and an unbound tag does not decode — outside a
     * running server nothing has loaded one. Binding the mod's own tags out of the committed
     * JSON is not a workaround: it is the step that proves those files name items that
     * actually exist, which is the half a JSON-shape check cannot see.
     */
    @BeforeAll
    static void bindTheModsTags() {
        BootstrappedGame.ensure();
        TagBinding.bindInfusedLogTags();

        registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        ops = TagBinding.ops();
    }

    static List<String> metals() {
        return Matrix.METALS;
    }

    @Requirement("BRN-03")
    @Requirement("WSH-01")
    @Test
    void bothRecipeTypesAreRegistered() {
        for (String key : List.of("burning", "washing")) {
            Identifier id = Identifier.parse("iron_oak:" + key);
            assertTrue(BuiltInRegistries.RECIPE_TYPE.containsKey(id), () -> "no recipe type " + id);
            assertTrue(BuiltInRegistries.RECIPE_SERIALIZER.containsKey(id), () -> "no recipe serializer " + id);
        }
    }

    @Requirement("BRN-03")
    @ParameterizedTest(name = "{0}")
    @MethodSource("metals")
    void burningInfusedLogsYieldsThatMetalsAsh(String metal) {
        BurningRecipe recipe = decode(ModRecipes.BURNING_RECIPE_SERIALIZER,
                "data/iron_oak/recipe/burning_" + metal + "_ash.json");

        assertEquals(ModRecipes.DEFAULT_COOKING_TIME, recipe.cookingTime(),
                () -> "burning_" + metal + "_ash no longer takes the default cook time");
        assertEquals("iron_oak:" + metal + "_ash", resultId(recipe));
    }

    /**
     * The recipe takes a tag, not six items, so the tag is what decides which logs can be
     * burned — and it has to be exact in both directions. A missing entry is a wood type that
     * silently cannot be turned into ash; an extra one is a gold log that burns into iron ash.
     * Neither crashes, so neither is visible without this.
     */
    @Requirement("MAT-03")
    @ParameterizedTest(name = "{0}")
    @MethodSource("metals")
    void burningTakesExactlyThatMetalsSixLogs(String metal) {
        BurningRecipe recipe = decode(ModRecipes.BURNING_RECIPE_SERIALIZER,
                "data/iron_oak/recipe/burning_" + metal + "_ash.json");

        List<String> expected = Matrix.WOODS.stream()
                .map(wood -> "iron_oak:" + metal + "_" + wood + "_log")
                .sorted()
                .toList();
        List<String> accepted = recipe.input().items()
                .map(holder -> BuiltInRegistries.ITEM.getKey(holder.value()).toString())
                .sorted()
                .toList();

        assertEquals(expected, accepted, () -> "burning_" + metal + "_ash burns the wrong set of logs");
    }

    @Requirement("WSH-01")
    @ParameterizedTest(name = "{0}")
    @MethodSource("metals")
    void washingAshYieldsThatMetalsShred(String metal) {
        WashingRecipe recipe = decode(ModRecipes.WASHING_RECIPE_SERIALIZER,
                "data/iron_oak/recipe/washing_" + metal + "_shred.json");

        assertEquals(ModRecipes.DEFAULT_COOKING_TIME, recipe.cookingTime());
        assertEquals("iron_oak:" + metal + "_shred", resultId(recipe));
        assertTrue(recipe.input().test(itemStack("iron_oak:" + metal + "_ash")),
                () -> "washing_" + metal + "_shred does not accept " + metal + " ash");
    }

    private static <T extends AbstractCookingRecipe> T decode(RecipeSerializer<T> serializer, String path) {
        JsonObject json = Resources.jsonOrFail(path);
        return serializer.codec()
                .codec()
                .parse(ops, json)
                .getOrThrow(message -> new AssertionError(path + " does not decode: " + message));
    }

    private static String resultId(AbstractCookingRecipe recipe) {
        // In 26.2, assemble() requires components to be bound, which they aren't in
        // a test environment. Instead, use the public accessor.
        ItemStackTemplate template;
        if (recipe instanceof BurningRecipe burningRecipe) {
            template = burningRecipe.getResultTemplate();
        } else if (recipe instanceof WashingRecipe washingRecipe) {
            template = washingRecipe.getResultTemplate();
        } else {
            throw new IllegalArgumentException("Unknown recipe type: " + recipe.getClass());
        }
        return BuiltInRegistries.ITEM.getKey(template.item().value()).toString();
    }

    private static Item item(String id) {
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
        assertNotNull(item, () -> id + " is not registered");
        return item;
    }

    private static ItemStack itemStack(String id) {
        // In 26.2, ItemStack requires components to be bound to holders.
        // We need to bind components to the holder before creating the ItemStack.
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
        assertNotNull(item, () -> id + " is not registered");
        // Get the holder and bind empty components if not already bound
        net.minecraft.core.Holder<Item> holder = item.builtInRegistryHolder();
        if (holder instanceof net.minecraft.core.Holder.Reference<Item> refHolder && !refHolder.areComponentsBound()) {
            refHolder.bindComponents(net.minecraft.core.component.DataComponentMap.EMPTY);
        }
        return new ItemStack(holder);
    }
}
