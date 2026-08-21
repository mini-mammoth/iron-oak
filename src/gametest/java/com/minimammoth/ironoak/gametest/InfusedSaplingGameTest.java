package com.minimammoth.ironoak.gametest;

import com.minimammoth.ironoak.OreInfusedSaplingBlock;
import com.minimammoth.ironoak.init.ModBlocks;
import com.minimammoth.ironoak.requirements.Requirement;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The in-world proof for #30.
 *
 * <p>{@code TreeMatrixTest} asserts the wiring — that each sapling holds the generator named
 * after it and that generator grows the feature named after it. This asserts the outcome: a
 * planted sapling produces its own metal's log of its own wood. The wiring test would have
 * caught #30 on its own; this is the one that says what a player would have seen.
 *
 * <p>One arm per metal, and specifically the three woods that were rotated: spruce grew dark
 * oak, dark oak grew jungle, jungle grew spruce. Not all eighteen — each of these loads a
 * world and grows a tree, and the cheap test already covers the full matrix.
 */
public class InfusedSaplingGameTest {

    @Requirement("TRE-03")
    @GameTest(skyAccess = true, maxTicks = 200)
    public void ironSpruceSaplingGrowsIronSpruce(GameTestHelper helper) {
        assertGrowsInto(helper, ModBlocks.IRON_SPRUCE_SAPLING, ModBlocks.IRON_SPRUCE_LOG);
    }

    @Requirement("TRE-03")
    @GameTest(skyAccess = true, maxTicks = 200)
    public void copperDarkOakSaplingGrowsCopperDarkOak(GameTestHelper helper) {
        assertGrowsInto(helper, ModBlocks.COPPER_DARK_OAK_SAPLING, ModBlocks.COPPER_DARK_OAK_LOG);
    }

    @Requirement("TRE-03")
    @GameTest(skyAccess = true, maxTicks = 200)
    public void goldJungleSaplingGrowsGoldJungle(GameTestHelper helper) {
        assertGrowsInto(helper, ModBlocks.GOLD_JUNGLE_SAPLING, ModBlocks.GOLD_JUNGLE_LOG);
    }

    /**
     * Plants the sapling and forces it to grow rather than waiting for a random tick — a
     * gametest that waits on randomness is a gametest that gets disabled and then deleted.
     *
     * <p>Growing is not one call. {@code advanceTree} bumps {@code STAGE} from 0 to 1 the
     * first time and only tries to grow after that, and the attempt itself is a dice roll
     * that can simply fail. So it is driven once per tick until a log appears, which asserts
     * on state rather than on elapsed time and cannot be made flaky by an unlucky seed.
     */
    private static void assertGrowsInto(GameTestHelper helper, Block sapling, Block expectedLog) {
        BlockPos pos = new BlockPos(3, 2, 3);
        helper.setBlock(pos.below(), Blocks.DIRT);
        helper.setBlock(pos, sapling);

        ServerLevel level = helper.getLevel();
        BlockPos absolute = helper.absolutePos(pos);

        helper.succeedWhen(() -> {
            BlockState state = level.getBlockState(absolute);
            if (state.getBlock() instanceof OreInfusedSaplingBlock grower) {
                grower.advanceTree(level, absolute, state, level.random);
            }

            helper.assertBlockPresent(expectedLog, pos);
        });
    }
}
