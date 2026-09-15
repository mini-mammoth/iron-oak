package com.minimammoth.ironoak.init;

import com.google.gson.JsonObject;
import com.minimammoth.ironoak.BootstrappedGame;
import com.minimammoth.ironoak.Matrix;
import com.minimammoth.ironoak.OreInfusedSaplingBlock;
import com.minimammoth.ironoak.Resources;
import com.minimammoth.ironoak.requirements.Requirement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The permanent version of the throwaway script that found #30.
 *
 * <p>Spruce saplings grew dark oak, dark oak grew jungle and jungle grew spruce — for all
 * three metals, shipped for years, with a green build the whole time. Nothing was
 * <em>inconsistent</em>: the constants in {@code ModConfiguredFeatures} were named after one
 * wood type and held the key of another, {@code ModSaplingGenerators} inherited the lie by
 * taking the constant, and {@code ModBlocks} trusted the constant's name. Every arm
 * compiled and {@code runDatagen} produced no diff.
 *
 * <p>So this test never touches those constants. It builds each arm's ids out of the metal
 * and wood strings in {@link Matrix} and walks the chain the game walks:
 *
 * <pre>
 * block iron_oak:&lt;metal&gt;_&lt;wood&gt;_sapling
 *   -&gt; its TreeGrower, whose name must be &lt;metal&gt;_&lt;wood&gt;
 *     -&gt; the configured feature that generator was built with, iron_oak:&lt;metal&gt;_&lt;wood&gt;_tree
 *       -&gt; the committed feature JSON, whose trunk must be that metal's log of that wood
 *          and whose foliage must be that wood's leaves
 * </pre>
 *
 * <p>Against the broken tree the script reported 54 inconsistencies. Against the fix, 18 of
 * 18 clean.
 */
@ExtendWith(BootstrappedGame.class)
class TreeMatrixTest {

    static List<Matrix.Arm> arms() {
        return Matrix.arms();
    }

    @Requirement("TRE-03")
    @Requirement("TRE-04")
    @ParameterizedTest(name = "{0}")
    @MethodSource("arms")
    void saplingGrowsItsOwnMetalAndWood(Matrix.Arm arm) {
        Block sapling = BuiltInRegistries.BLOCK.getValue(Identifier.parse(arm.saplingId()));
        assertNotNull(sapling, () -> arm.saplingId() + " is not registered");

        OreInfusedSaplingBlock infused =
                assertInstanceOf(OreInfusedSaplingBlock.class, sapling, arm.saplingId() + " is not an infused sapling");

        // Hop 1 — the sapling's generator must be named after the sapling. This is the hop
        // #30 broke: iron_spruce_sapling was handed the generator named "iron_dark_oak".
        assertEquals(arm.prefix(), growerName(infused.treeGrower()),
                () -> arm.saplingId() + " grows with the wrong generator");

        // Hop 2 — the generator must hold the feature named after it.
        ResourceKey<ConfiguredFeature<?, ?>> feature = ModSaplingGenerators.featureByName().get(arm.prefix());
        assertNotNull(feature, () -> "no generator named " + arm.prefix());
        assertEquals(arm.treeFeatureId(), feature.identifier().toString(),
                () -> "generator " + arm.prefix() + " grows the wrong feature");

        // Hop 3 — the feature that actually ships must place this arm's log, under this
        // wood's leaves. The generated JSON is output, not authority, but it is what the
        // server reads, and it is where the metal and the wood finally meet.
        JsonObject config = Resources
                .jsonOrFail("data/iron_oak/worldgen/configured_feature/" + arm.prefix() + "_tree.json")
                .getAsJsonObject("config");

        assertEquals(arm.logId(), stateName(config, "trunk_provider"),
                () -> arm.prefix() + "_tree is built out of the wrong log");
        assertEquals("minecraft:" + arm.wood() + "_leaves", stateName(config, "foliage_provider"),
                () -> arm.prefix() + "_tree has the wrong leaves — it is shaped like another wood type");
    }

    @Requirement("MAT-01")
    @ParameterizedTest(name = "{0}")
    @MethodSource("arms")
    void logIsRegistered(Matrix.Arm arm) {
        assertNotNull(BuiltInRegistries.BLOCK.getValue(Identifier.parse(arm.logId())),
                () -> arm.logId() + " is not registered");
    }

    /**
     * A half-filled matrix crashes on the missing entry, so completeness is its own
     * assertion: eighteen generators, no more and no fewer. An extra one means a rename left
     * an orphan behind; a missing one means an arm was forgotten.
     */
    @Requirement("MAT-01")
    @Test
    void thereAreExactlyEighteenGenerators() {
        assertEquals(Matrix.arms().size(), ModSaplingGenerators.featureByName().size(),
                () -> "generators: " + ModSaplingGenerators.featureByName().keySet());
    }

    /**
     * Every feature the generators point at has to have been emitted. A key with no file is
     * a sapling that silently refuses to grow.
     */
    @Requirement("MAT-02")
    @Test
    void everyFeatureKeyHasCommittedJson() {
        ModSaplingGenerators.featureByName().forEach((name, key) -> {
            String path = "data/" + key.identifier().getNamespace()
                    + "/worldgen/configured_feature/" + key.identifier().getPath() + ".json";
            assertTrue(Resources.exists(path), () -> "generator " + name + " points at a feature with no JSON: " + path);
        });
    }

    /**
     * {@code TreeGrower} keeps its name private and offers no getter, but its codec is
     * {@code Codec.stringResolver(g -> g.name, ...)} — encoding one yields exactly that
     * name. No reflection, no access widener.
     */
    private static String growerName(TreeGrower grower) {
        return TreeGrower.CODEC.encodeStart(JsonOps.INSTANCE, grower).getOrThrow().getAsString();
    }

    private static String stateName(JsonObject config, String provider) {
        return config.getAsJsonObject(provider).getAsJsonObject("state").get("Name").getAsString();
    }
}
