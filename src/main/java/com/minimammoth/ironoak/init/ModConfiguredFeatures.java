package com.minimammoth.ironoak.init;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.trunk.StraightTrunkPlacer;

import static com.minimammoth.ironoak.IronOak.MOD_ID;

public class ModConfiguredFeatures {
    private ModConfiguredFeatures() {
    }

    public static RegistryEntry<ConfiguredFeature<TreeFeatureConfig, ?>> COPPER_OAK_TREE;
    public static RegistryEntry<ConfiguredFeature<TreeFeatureConfig, ?>> GOLD_OAK_TREE;
    public static RegistryEntry<ConfiguredFeature<TreeFeatureConfig, ?>> IRON_OAK_TREE;

    private static TreeFeatureConfig.Builder oreOak(Block copperOakLog) {
        return new TreeFeatureConfig.Builder(
                BlockStateProvider.of(copperOakLog), // Trunk block provider
                new StraightTrunkPlacer(4, 2, 0), // places a straight trunk
                BlockStateProvider.of(Blocks.OAK_LEAVES), // Foliage block provider
                new BlobFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0), 3), // places leaves as a blob (radius, offset from trunk, height)
                new TwoLayersFeatureSize(1, 0, 1) // The width of the tree at different layers; used to see how tall the tree can be without clipping into blocks
        ).ignoreVines();
    }

    public static void onInitialize() {
        COPPER_OAK_TREE = ConfiguredFeatures.register(new Identifier(MOD_ID, "copper_oak_tree").toString(), Feature.TREE, oreOak(ModBlocks.COPPER_OAK_LOG).build());
        GOLD_OAK_TREE = ConfiguredFeatures.register(new Identifier(MOD_ID, "gold_oak_tree").toString(), Feature.TREE, oreOak(ModBlocks.GOLD_OAK_LOG).build());
        IRON_OAK_TREE = ConfiguredFeatures.register(new Identifier(MOD_ID, "iron_oak_tree").toString(), Feature.TREE, oreOak(ModBlocks.IRON_OAK_LOG).build());
    }
}
