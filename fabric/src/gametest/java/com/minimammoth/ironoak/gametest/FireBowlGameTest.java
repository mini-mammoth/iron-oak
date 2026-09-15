package com.minimammoth.ironoak.gametest;

import com.minimammoth.ironoak.FireBowlBlock;
import com.minimammoth.ironoak.FireBowlEntity;
import com.minimammoth.ironoak.init.ModBlocks;
import com.minimammoth.ironoak.init.ModItems;
import com.minimammoth.ironoak.init.ModRecipes;
import com.minimammoth.ironoak.requirements.Requirement;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * The fire bowl in a real world: the things {@code FireBowlEntityTest} cannot see because
 * they need a tick, an entity or a second inserter.
 *
 * <p>Every one of these is a regression guard with an issue behind it. #27 and #28 were
 * found by a human launching the game, four of them in a day, and every one was invisible to
 * a green build on both Linux and Windows.
 */
public class FireBowlGameTest {

    private static final BlockPos BOWL = new BlockPos(1, 2, 1);

    /**
     * The cook completes on a tick counter and the result lands in the output slot. There is
     * no way to observe that without ticking a world, which is why the layer-1 test can only
     * pin the duration and not what the duration does.
     */
    @Requirement("BRN-03")
    @GameTest(maxTicks = ModRecipes.DEFAULT_COOKING_TIME + 100)
    public void aLitBowlBurnsALogIntoAsh(GameTestHelper helper) {
        FireBowlEntity bowl = litBowlWith(helper, new ItemStack(ModItems.IRON_OAK_LOG));

        helper.succeedWhen(() -> {
            if (!bowl.getInput().isEmpty()) {
                throw helper.assertionException("the log is still in the bowl after %d ticks",
                        ModRecipes.DEFAULT_COOKING_TIME);
            }
            if (!bowl.getOutput().is(ModItems.IRON_ASH)) {
                throw helper.assertionException("the bowl produced %s instead of iron ash", bowl.getOutput());
            }
        });
    }

    /**
     * #28 defect 1: the guard in {@code onProjectileHit} required {@code LIT} to already be
     * true before setting it to true, so a burning arrow could never light the bowl. The
     * condition now matches {@code CampfireBlock}'s — unlit and not waterlogged.
     */
    @Requirement("BRN-07")
    @GameTest(maxTicks = 100)
    public void aBurningArrowLightsAnUnlitBowl(GameTestHelper helper) {
        helper.setBlock(BOWL.below(), Blocks.STONE);
        helper.setBlock(BOWL, ModBlocks.FIRE_BOWL);

        EntityType<?> arrowType = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("minecraft:arrow"));
        var arrow = helper.spawn(arrowType, BOWL.above(2));
        arrow.igniteForSeconds(10);
        // Straight down onto the bowl. Deliberately not a shot from an angle: what is being
        // tested is the block's reaction to a burning projectile, not the arrow's flight.
        arrow.setDeltaMovement(0.0, -0.6, 0.0);

        helper.succeedWhen(() -> helper.assertBlockProperty(BOWL, BlockStateProperties.LIT, true));
    }

    /**
     * #28 defect 3: the player path was handed the cook duration while the hopper path went
     * through {@code setItem} and never was, so the same log burned for different lengths of
     * time depending on who inserted it. Both funnel through {@code setItem} now and the
     * duration is derived — but the bug lived at the seam between two real inserters, so the
     * regression test needs a real hopper.
     */
    @Requirement("BRN-06")
    @GameTest(maxTicks = 100)
    public void aHopperFeedsTheBowl(GameTestHelper helper) {
        helper.setBlock(BOWL.below(), Blocks.STONE);
        helper.setBlock(BOWL, ModBlocks.FIRE_BOWL);

        BlockPos hopperPos = BOWL.above();
        helper.setBlock(hopperPos, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        helper.getBlockEntity(hopperPos, HopperBlockEntity.class)
                .setItem(0, new ItemStack(ModItems.IRON_OAK_LOG));

        FireBowlEntity bowl = helper.getBlockEntity(BOWL, FireBowlEntity.class);
        helper.succeedWhen(() -> {
            if (!bowl.getInput().is(ModItems.IRON_OAK_LOG)) {
                throw helper.assertionException("the hopper did not push the log in");
            }
        });
    }

    /**
     * A hopper below must be able to pull the ash out, and must not be able to steal the log
     * back out of the input slot. {@code getSlotsForFace} says so at layer 1; this proves a
     * hopper agrees.
     */
    @Requirement("BRN-06")
    @GameTest(maxTicks = 100)
    public void aHopperBelowTakesOnlyTheOutput(GameTestHelper helper) {
        helper.setBlock(BOWL.below(2), Blocks.STONE);
        helper.setBlock(BOWL.below(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        helper.setBlock(BOWL, ModBlocks.FIRE_BOWL);

        FireBowlEntity bowl = helper.getBlockEntity(BOWL, FireBowlEntity.class);
        bowl.setInput(new ItemStack(ModItems.IRON_OAK_LOG));
        bowl.setItem(1, new ItemStack(ModItems.IRON_ASH));

        HopperBlockEntity hopper = helper.getBlockEntity(BOWL.below(), HopperBlockEntity.class);
        helper.succeedWhen(() -> {
            if (!hopper.getItem(0).is(ModItems.IRON_ASH)) {
                throw helper.assertionException("the hopper did not pull the ash out");
            }
            if (!bowl.getInput().is(ModItems.IRON_OAK_LOG)) {
                throw helper.assertionException("the hopper stole the log out of the input slot");
            }
        });
    }

    /*
     * The drop-on-break rules are deliberately NOT tested here, and that is a finding
     * rather than an omission.
     *
     * docs/strategy/testing.md asks for two gametests: breaking an unlit bowl drops its
     * contents, and breaking a lit one destroys them — the second being intended, "written
     * down as a test so nobody fixes it by accident". Neither is true any more.
     *
     * Since 1.21.11, LevelChunk.setBlockState calls BlockEntity.preRemoveSideEffects
     * whenever a block entity is removed, and its default body is:
     *
     *     if (this instanceof Container container && this.level != null) {
     *         Containers.dropContents(this.level, blockPos, container);
     *     }
     *
     * FireBowlEntity is a Container, so vanilla now drops the contents on every removal
     * path. Two consequences: FireBowlBlock.playerWillDestroy's own spawnContainingItems()
     * is redundant (it empties the container first, so nothing drops twice), and a lit bowl
     * no longer destroys what is inside it.
     *
     * Verified, not inferred: a gametest that broke a lit bowl and asserted the log was gone
     * failed with "Did not expect item of type Iron Infused Oak Log at (1, 2, 1)".
     *
     * Whether the lit-bowl exception should be restored is a gameplay decision with a
     * requirement behind it, and restoring it means overriding preRemoveSideEffects. That is
     * a behaviour change, out of scope for a test-harness ticket. Reported instead — and no
     * test is shipped for it, because a test written against behaviour nobody has decided on
     * would freeze it in place.
     */

    private static FireBowlEntity litBowlWith(GameTestHelper helper, ItemStack input) {
        helper.setBlock(BOWL.below(), Blocks.STONE);
        helper.setBlock(BOWL, ModBlocks.FIRE_BOWL.defaultBlockState().setValue(FireBowlBlock.LIT, true));

        FireBowlEntity bowl = helper.getBlockEntity(BOWL, FireBowlEntity.class);
        bowl.setInput(input);
        return bowl;
    }
}
