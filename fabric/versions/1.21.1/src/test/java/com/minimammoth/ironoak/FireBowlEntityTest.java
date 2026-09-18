package com.minimammoth.ironoak;

import com.minimammoth.ironoak.init.ModBlocks;
import com.minimammoth.ironoak.init.ModItems;
import com.minimammoth.ironoak.init.ModRecipes;
import com.minimammoth.ironoak.requirements.Requirement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The parts of the fire bowl that can be checked without a world.
 *
 * <p>Ticking, hopper insertion, the burning arrow and the drop-on-break rules are all
 * layer 2 — they need a real level and a real inserter, and pushing them down to here would
 * only test a mock of somebody's idea of a level. What is left is still the contract with
 * hopper automation, which {@code README.md} documents as a supported way to play, and the
 * save format, which is where #28 defect 2 lived.
 */
@ExtendWith(BootstrappedGame.class)
class FireBowlEntityTest {
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    private static FireBowlEntity fireBowl() {
        return new FireBowlEntity(BlockPos.ZERO, ModBlocks.FIRE_BOWL.defaultBlockState());
    }

    // ---- the hopper contract: a pure switch over Direction -------------------------

    @Requirement("BRN-06")
    @Test
    void hoppersBelowMayOnlyReachTheOutput() {
        assertArrayEquals(new int[]{OUTPUT_SLOT}, fireBowl().getSlotsForFace(Direction.DOWN));
    }

    @Requirement("BRN-06")
    @Test
    void hoppersAboveMayOnlyReachTheInput() {
        assertArrayEquals(new int[]{INPUT_SLOT}, fireBowl().getSlotsForFace(Direction.UP));
    }

    @Requirement("BRN-06")
    @ParameterizedTest
    @EnumSource(value = Direction.class, names = {"NORTH", "SOUTH", "WEST", "EAST"})
    void hoppersAtTheSideReachBothSlots(Direction side) {
        assertArrayEquals(new int[]{INPUT_SLOT, OUTPUT_SLOT}, fireBowl().getSlotsForFace(side));
    }

    @Requirement("BRN-06")
    @Test
    void onlyTheOutputCanBePulledOut() {
        FireBowlEntity bowl = fireBowl();
        assertTrue(bowl.canTakeItemThroughFace(OUTPUT_SLOT, new ItemStack(ModItems.IRON_ASH), Direction.DOWN));
        assertFalse(bowl.canTakeItemThroughFace(INPUT_SLOT, new ItemStack(ModItems.IRON_OAK_LOG), Direction.DOWN),
                "a hopper must not steal the log back out of the input slot");
    }

    /**
     * {@code canPlaceItemThroughFace} is the neighbour of the method above and looks like it
     * belongs here, but it resolves a recipe and so needs a {@code ServerLevel}. Without one
     * it must refuse rather than throw — that refusal is the only half of it this layer can
     * see; the accepting half is a gametest.
     */
    @Requirement("BRN-06")
    @Test
    void nothingCanBePutInWithoutAServer() {
        assertFalse(fireBowl().canPlaceItemThroughFace(INPUT_SLOT, new ItemStack(ModItems.IRON_OAK_LOG), Direction.UP));
    }

    // ---- cook duration: #28 defect 3 -----------------------------------------------

    /**
     * All three burning recipes omit {@code cookingtime}, so the serializer hands them
     * {@link ModRecipes#DEFAULT_COOKING_TIME}; an input that matches no recipe at all falls
     * back to the same constant here. #28 collapsed two disagreeing defaults into that one
     * number, and nothing but this pins them together.
     */
    @Requirement("BRN-03")
    @Test
    void anInputWithNoRecipeBurnsForTheDefaultTime() {
        assertEquals(ModRecipes.DEFAULT_COOKING_TIME, FireBowlEntity.cookingTotalTime(Optional.empty()));
    }

    @Requirement("BRN-03")
    @Test
    void aMatchedRecipeSuppliesItsOwnCookTime() {
        assertEquals(42, FireBowlEntity.cookingTotalTime(Optional.of(burningRecipeTaking(42))),
                "the duration has to come from the recipe, not from a field on the entity");
    }

    // ---- the save format: #28 defect 2 ---------------------------------------------

    /**
     * The idle countdown used to restart on every world load, because {@code unlitTime} was
     * read but never written: a bowl that had been sitting empty got a fresh 100 ticks every
     * time its chunk came back. The key it is persisted under is {@code unlit_time}, and a
     * saved world depends on that spelling.
     */
    @Requirement("BRN-04")
    @Test
    void theIdleCountdownSurvivesASave() {
        CompoundTag saved = saveOf(loadedFrom(state(17, 37)));

        assertEquals(37, saved.getInt("unlit_time"), "unlit_time was not written");
        assertEquals(17, saved.getInt("cooking_time"), "cooking_time was not written");
    }

    /**
     * A bowl saved before #28 has no {@code unlit_time} key at all, and must load as a bowl
     * whose countdown starts fresh rather than as one that throws.
     */
    @Requirement("BRN-04")
    @Test
    void aPre28SaveLoadsWithTheCountdownAtZero() {
        CompoundTag old = state(17, 37);
        old.remove("unlit_time");

        assertEquals(0, saveOf(loadedFrom(old)).getInt("unlit_time"),
                "a bowl with no unlit_time key must load — and then save — with the countdown at zero");
    }

    @Requirement("BRN-03")
    @Test
    void bothSlotsSurviveASave() {
        FireBowlEntity bowl = fireBowl();
        bowl.setInput(new ItemStack(ModItems.IRON_OAK_LOG));
        bowl.setItem(OUTPUT_SLOT, new ItemStack(ModItems.IRON_ASH, 3));

        FireBowlEntity reloaded = loadedFrom(saveOf(bowl));

        assertTrue(ItemStack.matches(new ItemStack(ModItems.IRON_OAK_LOG), reloaded.getInput()),
                () -> "input came back as " + reloaded.getInput());
        assertTrue(ItemStack.matches(new ItemStack(ModItems.IRON_ASH, 3), reloaded.getOutput()),
                () -> "output came back as " + reloaded.getOutput());
    }

    /**
     * A new log starts from zero progress whoever put it there. Before #28 only the player
     * path reset it, so a hopper-inserted log inherited whatever the previous one had left.
     * The reset now lives in {@code setItem}, which both paths go through.
     */
    @Requirement("BRN-03")
    @Test
    void aFreshInputResetsTheProgress() {
        FireBowlEntity bowl = loadedFrom(state(150, 0));
        bowl.setInput(new ItemStack(ModItems.IRON_OAK_LOG));

        assertEquals(0, saveOf(bowl).getInt("cooking_time"),
                "the incoming log inherited the previous log's progress");
    }

    // ---- helpers -------------------------------------------------------------------

    /**
     * Resolved on demand, never in a static initialiser: a static field runs when JUnit loads
     * the class, which is before {@link BootstrappedGame} has run, and touching
     * {@code BuiltInRegistries} that early poisons its class initialiser for the whole JVM.
     */
    private static RegistryAccess.Frozen registries() {
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    private static CompoundTag state(int cookingTime, int unlitTime) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("cooking_time", cookingTime);
        tag.putInt("unlit_time", unlitTime);
        return tag;
    }

    private static FireBowlEntity loadedFrom(CompoundTag tag) {
        FireBowlEntity bowl = fireBowl();
        bowl.loadAdditional(tag, registries());
        return bowl;
    }

    private static CompoundTag saveOf(FireBowlEntity bowl) {
        CompoundTag tag = new CompoundTag();
        bowl.saveAdditional(tag, registries());
        return tag;
    }

    private static RecipeHolder<BurningRecipe> burningRecipeTaking(int cookTime) {
        BurningRecipe recipe = new BurningRecipe("", CookingBookCategory.MISC,
                Ingredient.of(ModItems.IRON_OAK_LOG), new ItemStack(ModItems.IRON_ASH), 0.2f, cookTime);
        return new RecipeHolder<>(ResourceLocation.parse("iron_oak:test_burning"), recipe);
    }
}
