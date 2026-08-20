package com.minimammoth.ironoak;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The item side of the 6x3 matrix: which infused sapling each bone meal turns a vanilla
 * sapling into.
 *
 * <p>{@code ModItems} hand-builds three of these maps with {@code Map.of(...)}, six entries
 * each. That is eighteen pairings written out by hand with no compiler checking that the
 * metal on the left matches the metal on the right — the same shape as #30, one layer up.
 */
class OreInfusedBoneMealTest {

    static List<String> metals() {
        return Matrix.METALS;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("metals")
    void boneMealInfusesEveryWoodWithItsOwnMetal(String metal) {
        BootstrappedGame.ensure();

        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse("iron_oak:" + metal + "_bone_meal"));
        assertNotNull(item, () -> metal + " bone meal is not registered");

        Map<Block, Block> infusions =
                assertInstanceOf(OreInfusedBoneMeal.class, item, metal + " bone meal is not infused bone meal").infusionMap();

        assertEquals(Matrix.WOODS.size(), infusions.size(),
                () -> metal + " bone meal covers " + infusions.size() + " of " + Matrix.WOODS.size() + " wood types");

        for (String wood : Matrix.WOODS) {
            Block vanilla = block("minecraft:" + wood + "_sapling");
            Block expected = block("iron_oak:" + metal + "_" + wood + "_sapling");

            assertEquals(expected, infusions.get(vanilla),
                    () -> metal + " bone meal turns a " + wood + " sapling into the wrong sapling");
        }
    }

    private static Block block(String id) {
        Block block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
        assertNotNull(block, () -> id + " is not registered");
        return block;
    }
}
