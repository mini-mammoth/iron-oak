package com.minimammoth.ironoak.init;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class ModSaplingGenerators {

    /**
     * The configured feature each generator below was built with, keyed by the generator's
     * own name. {@link TreeGrower} keeps both privately and exposes neither, so this is the
     * only way to ask — from outside — which tree a generator actually grows.
     * <p>
     * That question is #30: {@code IRON_SPRUCE} held the dark oak feature for years while
     * every arm compiled and the generated JSON was internally consistent. The name was the
     * only thing that lied, and nothing could read it back. {@code ModSaplingGeneratorsTest}
     * now does.
     * <p>
     * Declared before the constants on purpose — static initialisers run in textual order,
     * and {@link #generator} writes to this map.
     */
    private static final Map<String, ResourceKey<ConfiguredFeature<?, ?>>> FEATURE_BY_NAME = new LinkedHashMap<>();

    public static final TreeGrower COPPER_OAK = generator("copper_oak", ModConfiguredFeatures.COPPER_OAK_TREE);
    public static final TreeGrower GOLD_OAK = generator("gold_oak", ModConfiguredFeatures.GOLD_OAK_TREE);
    public static final TreeGrower IRON_OAK = generator("iron_oak", ModConfiguredFeatures.IRON_OAK_TREE);

    public static final TreeGrower COPPER_BIRCH = generator("copper_birch", ModConfiguredFeatures.COPPER_BIRCH_TREE);
    public static final TreeGrower GOLD_BIRCH = generator("gold_birch", ModConfiguredFeatures.GOLD_BIRCH_TREE);
    public static final TreeGrower IRON_BIRCH = generator("iron_birch", ModConfiguredFeatures.IRON_BIRCH_TREE);

    public static final TreeGrower COPPER_ACACIA = generator("copper_acacia", ModConfiguredFeatures.COPPER_ACACIA_TREE);
    public static final TreeGrower GOLD_ACACIA = generator("gold_acacia", ModConfiguredFeatures.GOLD_ACACIA_TREE);
    public static final TreeGrower IRON_ACACIA = generator("iron_acacia", ModConfiguredFeatures.IRON_ACACIA_TREE);

    public static final TreeGrower COPPER_SPRUCE = generator("copper_spruce", ModConfiguredFeatures.COPPER_SPRUCE_TREE);
    public static final TreeGrower GOLD_SPRUCE = generator("gold_spruce", ModConfiguredFeatures.GOLD_SPRUCE_TREE);
    public static final TreeGrower IRON_SPRUCE = generator("iron_spruce", ModConfiguredFeatures.IRON_SPRUCE_TREE);

    public static final TreeGrower COPPER_JUNGLE = generator("copper_jungle", ModConfiguredFeatures.COPPER_JUNGLE_TREE);
    public static final TreeGrower GOLD_JUNGLE = generator("gold_jungle", ModConfiguredFeatures.GOLD_JUNGLE_TREE);
    public static final TreeGrower IRON_JUNGLE = generator("iron_jungle", ModConfiguredFeatures.IRON_JUNGLE_TREE);

    public static final TreeGrower COPPER_DARK_OAK = generator("copper_dark_oak", ModConfiguredFeatures.COPPER_DARK_OAK_TREE);
    public static final TreeGrower GOLD_DARK_OAK = generator("gold_dark_oak", ModConfiguredFeatures.GOLD_DARK_OAK_TREE);
    public static final TreeGrower IRON_DARK_OAK = generator("iron_dark_oak", ModConfiguredFeatures.IRON_DARK_OAK_TREE);

    /**
     * The name each generator was built under, mapped to the feature it grows.
     */
    public static Map<String, ResourceKey<ConfiguredFeature<?, ?>>> featureByName() {
        return Collections.unmodifiableMap(FEATURE_BY_NAME);
    }

    private static TreeGrower generator(String id, ResourceKey<ConfiguredFeature<?, ?>> featureRegistryKey) {
        FEATURE_BY_NAME.put(id, featureRegistryKey);
        return new TreeGrower(id, Optional.empty(), Optional.of(featureRegistryKey), Optional.empty());
    }
}
