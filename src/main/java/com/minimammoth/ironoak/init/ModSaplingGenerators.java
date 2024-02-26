package com.minimammoth.ironoak.init;

import net.minecraft.block.SaplingGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.gen.feature.ConfiguredFeature;

import java.util.Optional;

public class ModSaplingGenerators {
    public static final SaplingGenerator COPPER_OAK = generator("copper_oak", ModConfiguredFeatures.COPPER_OAK_TREE);
    public static final SaplingGenerator GOLD_OAK = generator("gold_oak", ModConfiguredFeatures.GOLD_OAK_TREE);
    public static final SaplingGenerator IRON_OAK = generator("iron_oak", ModConfiguredFeatures.IRON_OAK_TREE);

    public static final SaplingGenerator COPPER_BIRCH = generator("copper_birch", ModConfiguredFeatures.COPPER_BIRCH_TREE);
    public static final SaplingGenerator GOLD_BIRCH = generator("gold_birch", ModConfiguredFeatures.GOLD_BIRCH_TREE);
    public static final SaplingGenerator IRON_BIRCH = generator("iron_birch", ModConfiguredFeatures.IRON_BIRCH_TREE);

    public static final SaplingGenerator COPPER_ACACIA = generator("copper_acacia", ModConfiguredFeatures.COPPER_ACACIA_TREE);
    public static final SaplingGenerator GOLD_ACACIA = generator("gold_acacia", ModConfiguredFeatures.GOLD_ACACIA_TREE);
    public static final SaplingGenerator IRON_ACACIA = generator("iron_acacia", ModConfiguredFeatures.IRON_ACACIA_TREE);

    public static final SaplingGenerator COPPER_JUNGLE = generator("copper_spruce", ModConfiguredFeatures.COPPER_JUNGLE_TREE);
    public static final SaplingGenerator GOLD_JUNGLE = generator("gold_spruce", ModConfiguredFeatures.GOLD_JUNGLE_TREE);
    public static final SaplingGenerator IRON_JUNGLE = generator("iron_spruce", ModConfiguredFeatures.IRON_JUNGLE_TREE);

    public static final SaplingGenerator COPPER_DARK_OAK = generator("copper_jungle", ModConfiguredFeatures.COPPER_DARK_OAK_TREE);
    public static final SaplingGenerator GOLD_DARK_OAK = generator("gold_jungle", ModConfiguredFeatures.GOLD_DARK_OAK_TREE);
    public static final SaplingGenerator IRON_DARK_OAK = generator("iron_jungle", ModConfiguredFeatures.IRON_DARK_OAK_TREE);

    public static final SaplingGenerator COPPER_SPRUCE = generator("copper_dark_oak", ModConfiguredFeatures.COPPER_SPRUCE_TREE);
    public static final SaplingGenerator GOLD_SPRUCE = generator("gold_dark_oak", ModConfiguredFeatures.GOLD_SPRUCE_TREE);
    public static final SaplingGenerator IRON_SPRUCE = generator("iron_dark_oak", ModConfiguredFeatures.IRON_SPRUCE_TREE);

    private static SaplingGenerator generator(String id, RegistryKey<ConfiguredFeature<?, ?>> featureRegistryKey) {
        return new SaplingGenerator(id, Optional.empty(), Optional.of(featureRegistryKey), Optional.empty());
    }
}
