package com.minimammoth.ironoak.init;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.size.ThreeLayersFeatureSize;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.AcaciaFoliagePlacer;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.foliage.DarkOakFoliagePlacer;
import net.minecraft.world.gen.foliage.SpruceFoliagePlacer;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.treedecorator.CocoaBeansTreeDecorator;
import net.minecraft.world.gen.treedecorator.LeavesVineTreeDecorator;
import net.minecraft.world.gen.treedecorator.TrunkVineTreeDecorator;
import net.minecraft.world.gen.trunk.DarkOakTrunkPlacer;
import net.minecraft.world.gen.trunk.ForkingTrunkPlacer;
import net.minecraft.world.gen.trunk.StraightTrunkPlacer;

import java.util.List;
import java.util.OptionalInt;

import static com.minimammoth.ironoak.IronOak.MOD_ID;

public class ModConfiguredFeatures {

    private ModConfiguredFeatures() {
    }

    public static final RegistryKey<ConfiguredFeature<?, ?>> COPPER_OAK_TREE = registerKey("copper_oak_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> GOLD_OAK_TREE = registerKey("gold_oak_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> IRON_OAK_TREE = registerKey("iron_oak_tree");

    public static final RegistryKey<ConfiguredFeature<?, ?>> COPPER_BIRCH_TREE = registerKey("copper_birch_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> GOLD_BIRCH_TREE = registerKey("gold_birch_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> IRON_BIRCH_TREE = registerKey("iron_birch_tree");

    public static final RegistryKey<ConfiguredFeature<?, ?>> COPPER_ACACIA_TREE = registerKey("copper_acacia_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> GOLD_ACACIA_TREE = registerKey("gold_acacia_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> IRON_ACACIA_TREE = registerKey("iron_acacia_tree");

    public static final RegistryKey<ConfiguredFeature<?, ?>> COPPER_JUNGLE_TREE = registerKey("copper_spruce_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> GOLD_JUNGLE_TREE = registerKey("gold_spruce_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> IRON_JUNGLE_TREE = registerKey("iron_spruce_tree");

    public static final RegistryKey<ConfiguredFeature<?, ?>> COPPER_DARK_OAK_TREE = registerKey("copper_jungle_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> GOLD_DARK_OAK_TREE = registerKey("gold_jungle_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> IRON_DARK_OAK_TREE = registerKey("iron_jungle_tree");

    public static final RegistryKey<ConfiguredFeature<?, ?>> COPPER_SPRUCE_TREE = registerKey("copper_dark_oak_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> GOLD_SPRUCE_TREE = registerKey("gold_dark_oak_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> IRON_SPRUCE_TREE = registerKey("iron_dark_oak_tree");

    private static TreeFeatureConfig.Builder oreOak(Block oreLog) {
        return new TreeFeatureConfig.Builder(BlockStateProvider.of(oreLog), new StraightTrunkPlacer(4, 2, 0), BlockStateProvider.of(Blocks.OAK_LEAVES), new BlobFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0), 3), new TwoLayersFeatureSize(1, 0, 1)).ignoreVines();
    }

    private static TreeFeatureConfig.Builder oreBirch(Block oreLog) {
        return new TreeFeatureConfig.Builder(BlockStateProvider.of(oreLog), new StraightTrunkPlacer(5, 2, 0), BlockStateProvider.of(Blocks.BIRCH_LEAVES), new BlobFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0), 3), new TwoLayersFeatureSize(1, 0, 1)).ignoreVines();
    }

    private static TreeFeatureConfig.Builder oreAcacia(Block oreLog) {
        return new TreeFeatureConfig.Builder(BlockStateProvider.of(oreLog), new ForkingTrunkPlacer(5, 2, 2), BlockStateProvider.of(Blocks.ACACIA_LEAVES), new AcaciaFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0)), new TwoLayersFeatureSize(1, 0, 2)).ignoreVines();
    }

    private static TreeFeatureConfig.Builder oreSpruce(Block oreLog) {
        return new TreeFeatureConfig.Builder(BlockStateProvider.of(oreLog), new StraightTrunkPlacer(5, 2, 1), BlockStateProvider.of(Blocks.SPRUCE_LEAVES), new SpruceFoliagePlacer(UniformIntProvider.create(2, 3), UniformIntProvider.create(0, 2), UniformIntProvider.create(1, 2)), new TwoLayersFeatureSize(2, 0, 2)).ignoreVines();
    }

    private static TreeFeatureConfig.Builder oreDarkOak(Block oreLog) {
        return new TreeFeatureConfig.Builder(BlockStateProvider.of(oreLog), new DarkOakTrunkPlacer(6, 2, 1), BlockStateProvider.of(Blocks.DARK_OAK_LEAVES), new DarkOakFoliagePlacer(ConstantIntProvider.create(0), ConstantIntProvider.create(0)), new ThreeLayersFeatureSize(1, 1, 0, 1, 2, OptionalInt.empty())).ignoreVines();
    }

    private static TreeFeatureConfig.Builder oreJungle(Block oreLog) {
        return new TreeFeatureConfig.Builder(BlockStateProvider.of(oreLog), // Trunk block provider
                new StraightTrunkPlacer(4, 8, 0), // places a straight trunk
                BlockStateProvider.of(Blocks.JUNGLE_LEAVES), // Foliage block provider
                new BlobFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0), 3), // places leaves as a blob (radius, offset from trunk, height)
                new TwoLayersFeatureSize(1, 0, 1) // The width of the tree at different layers; used to see how tall the tree can be without clipping into blocks
        ).decorators(List.of(new CocoaBeansTreeDecorator(0.2F), TrunkVineTreeDecorator.INSTANCE, new LeavesVineTreeDecorator(0.25F))).ignoreVines();
    }


    public static void bootstrap(Registerable<ConfiguredFeature<?, ?>> context) {
        ConfiguredFeatures.register(context, COPPER_OAK_TREE, Feature.TREE, oreOak(ModBlocks.COPPER_OAK_LOG).build());
        ConfiguredFeatures.register(context, GOLD_OAK_TREE, Feature.TREE, oreOak(ModBlocks.GOLD_OAK_LOG).build());
        ConfiguredFeatures.register(context, IRON_OAK_TREE, Feature.TREE, oreOak(ModBlocks.IRON_OAK_LOG).build());

        ConfiguredFeatures.register(context, COPPER_BIRCH_TREE, Feature.TREE, oreBirch(ModBlocks.COPPER_BIRCH_LOG).build());
        ConfiguredFeatures.register(context, GOLD_BIRCH_TREE, Feature.TREE, oreBirch(ModBlocks.GOLD_BIRCH_LOG).build());
        ConfiguredFeatures.register(context, IRON_BIRCH_TREE, Feature.TREE, oreBirch(ModBlocks.IRON_BIRCH_LOG).build());

        ConfiguredFeatures.register(context, COPPER_ACACIA_TREE, Feature.TREE, oreAcacia(ModBlocks.COPPER_ACACIA_LOG).build());
        ConfiguredFeatures.register(context, GOLD_ACACIA_TREE, Feature.TREE, oreAcacia(ModBlocks.GOLD_ACACIA_LOG).build());
        ConfiguredFeatures.register(context, IRON_ACACIA_TREE, Feature.TREE, oreAcacia(ModBlocks.IRON_ACACIA_LOG).build());

        ConfiguredFeatures.register(context, COPPER_JUNGLE_TREE, Feature.TREE, oreSpruce(ModBlocks.COPPER_SPRUCE_LOG).build());
        ConfiguredFeatures.register(context, GOLD_JUNGLE_TREE, Feature.TREE, oreSpruce(ModBlocks.GOLD_SPRUCE_LOG).build());
        ConfiguredFeatures.register(context, IRON_JUNGLE_TREE, Feature.TREE, oreSpruce(ModBlocks.IRON_SPRUCE_LOG).build());

        ConfiguredFeatures.register(context, COPPER_DARK_OAK_TREE, Feature.TREE, oreJungle(ModBlocks.COPPER_JUNGLE_LOG).build());
        ConfiguredFeatures.register(context, GOLD_DARK_OAK_TREE, Feature.TREE, oreJungle(ModBlocks.GOLD_JUNGLE_LOG).build());
        ConfiguredFeatures.register(context, IRON_DARK_OAK_TREE, Feature.TREE, oreJungle(ModBlocks.IRON_JUNGLE_LOG).build());

        ConfiguredFeatures.register(context, COPPER_SPRUCE_TREE, Feature.TREE, oreDarkOak(ModBlocks.COPPER_DARK_OAK_LOG).build());
        ConfiguredFeatures.register(context, GOLD_SPRUCE_TREE, Feature.TREE, oreDarkOak(ModBlocks.GOLD_DARK_OAK_LOG).build());
        ConfiguredFeatures.register(context, IRON_SPRUCE_TREE, Feature.TREE, oreDarkOak(ModBlocks.IRON_DARK_OAK_LOG).build());
    }

    public static RegistryKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, new Identifier(MOD_ID, name));
    }
}
