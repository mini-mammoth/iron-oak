package com.minimammoth.ironoak.init;

import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.ConfiguredFeature;
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

    public static final ConfiguredFeature<?, ?> IRON_OAK_TREE = Feature.TREE
            // Configure the feature using the builder
            .configure(new TreeFeatureConfig.Builder(
                    BlockStateProvider.of(ModBlocks.IRON_OAK_LOG), // Trunk block provider
                    new StraightTrunkPlacer(4, 2, 0), // places a straight trunk
                    BlockStateProvider.of(Blocks.OAK_LEAVES), // Foliage block provider
                    new BlobFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0), 3), // places leaves as a blob (radius, offset from trunk, height)
                    new TwoLayersFeatureSize(1, 0, 1) // The width of the tree at different layers; used to see how tall the tree can be without clipping into blocks
            ).ignoreVines().build());

    public static void onInitialize() {
        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new Identifier(MOD_ID, "iron_oak_tree"), IRON_OAK_TREE);
    }
}
