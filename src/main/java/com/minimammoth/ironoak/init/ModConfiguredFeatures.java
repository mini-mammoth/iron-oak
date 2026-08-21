package com.minimammoth.ironoak.init;

import java.util.List;
import java.util.OptionalInt;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.ThreeLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.AcaciaFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.DarkOakFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.SpruceFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.CocoaDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.DarkOakTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;

import static com.minimammoth.ironoak.IronOak.MOD_ID;

public class ModConfiguredFeatures {

    private ModConfiguredFeatures() {
    }

    public static final ResourceKey<ConfiguredFeature<?, ?>> COPPER_OAK_TREE = registerKey("copper_oak_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GOLD_OAK_TREE = registerKey("gold_oak_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> IRON_OAK_TREE = registerKey("iron_oak_tree");

    public static final ResourceKey<ConfiguredFeature<?, ?>> COPPER_BIRCH_TREE = registerKey("copper_birch_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GOLD_BIRCH_TREE = registerKey("gold_birch_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> IRON_BIRCH_TREE = registerKey("iron_birch_tree");

    public static final ResourceKey<ConfiguredFeature<?, ?>> COPPER_ACACIA_TREE = registerKey("copper_acacia_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GOLD_ACACIA_TREE = registerKey("gold_acacia_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> IRON_ACACIA_TREE = registerKey("iron_acacia_tree");

    public static final ResourceKey<ConfiguredFeature<?, ?>> COPPER_SPRUCE_TREE = registerKey("copper_spruce_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GOLD_SPRUCE_TREE = registerKey("gold_spruce_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> IRON_SPRUCE_TREE = registerKey("iron_spruce_tree");

    public static final ResourceKey<ConfiguredFeature<?, ?>> COPPER_JUNGLE_TREE = registerKey("copper_jungle_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GOLD_JUNGLE_TREE = registerKey("gold_jungle_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> IRON_JUNGLE_TREE = registerKey("iron_jungle_tree");

    public static final ResourceKey<ConfiguredFeature<?, ?>> COPPER_DARK_OAK_TREE = registerKey("copper_dark_oak_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GOLD_DARK_OAK_TREE = registerKey("gold_dark_oak_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> IRON_DARK_OAK_TREE = registerKey("iron_dark_oak_tree");

    private static TreeConfiguration.TreeConfigurationBuilder oreOak(Block oreLog) {
        return new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(oreLog), new StraightTrunkPlacer(4, 2, 0), BlockStateProvider.simple(Blocks.OAK_LEAVES), new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3), new TwoLayersFeatureSize(1, 0, 1)).ignoreVines();
    }

    private static TreeConfiguration.TreeConfigurationBuilder oreBirch(Block oreLog) {
        return new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(oreLog), new StraightTrunkPlacer(5, 2, 0), BlockStateProvider.simple(Blocks.BIRCH_LEAVES), new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3), new TwoLayersFeatureSize(1, 0, 1)).ignoreVines();
    }

    private static TreeConfiguration.TreeConfigurationBuilder oreAcacia(Block oreLog) {
        return new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(oreLog), new ForkingTrunkPlacer(5, 2, 2), BlockStateProvider.simple(Blocks.ACACIA_LEAVES), new AcaciaFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0)), new TwoLayersFeatureSize(1, 0, 2)).ignoreVines();
    }

    private static TreeConfiguration.TreeConfigurationBuilder oreSpruce(Block oreLog) {
        return new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(oreLog), new StraightTrunkPlacer(5, 2, 1), BlockStateProvider.simple(Blocks.SPRUCE_LEAVES), new SpruceFoliagePlacer(UniformInt.of(2, 3), UniformInt.of(0, 2), UniformInt.of(1, 2)), new TwoLayersFeatureSize(2, 0, 2)).ignoreVines();
    }

    private static TreeConfiguration.TreeConfigurationBuilder oreDarkOak(Block oreLog) {
        return new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(oreLog), new DarkOakTrunkPlacer(6, 2, 1), BlockStateProvider.simple(Blocks.DARK_OAK_LEAVES), new DarkOakFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0)), new ThreeLayersFeatureSize(1, 1, 0, 1, 2, OptionalInt.empty())).ignoreVines();
    }

    private static TreeConfiguration.TreeConfigurationBuilder oreJungle(Block oreLog) {
        return new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(oreLog), // Trunk block provider
                new StraightTrunkPlacer(4, 8, 0), // places a straight trunk
                BlockStateProvider.simple(Blocks.JUNGLE_LEAVES), // Foliage block provider
                new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3), // places leaves as a blob (radius, offset from trunk, height)
                new TwoLayersFeatureSize(1, 0, 1) // The width of the tree at different layers; used to see how tall the tree can be without clipping into blocks
        ).decorators(List.of(new CocoaDecorator(0.2F), TrunkVineDecorator.INSTANCE, new LeaveVineDecorator(0.25F))).ignoreVines();
    }


    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        FeatureUtils.register(context, COPPER_OAK_TREE, Feature.TREE, oreOak(ModBlocks.COPPER_OAK_LOG).build());
        FeatureUtils.register(context, GOLD_OAK_TREE, Feature.TREE, oreOak(ModBlocks.GOLD_OAK_LOG).build());
        FeatureUtils.register(context, IRON_OAK_TREE, Feature.TREE, oreOak(ModBlocks.IRON_OAK_LOG).build());

        FeatureUtils.register(context, COPPER_BIRCH_TREE, Feature.TREE, oreBirch(ModBlocks.COPPER_BIRCH_LOG).build());
        FeatureUtils.register(context, GOLD_BIRCH_TREE, Feature.TREE, oreBirch(ModBlocks.GOLD_BIRCH_LOG).build());
        FeatureUtils.register(context, IRON_BIRCH_TREE, Feature.TREE, oreBirch(ModBlocks.IRON_BIRCH_LOG).build());

        FeatureUtils.register(context, COPPER_ACACIA_TREE, Feature.TREE, oreAcacia(ModBlocks.COPPER_ACACIA_LOG).build());
        FeatureUtils.register(context, GOLD_ACACIA_TREE, Feature.TREE, oreAcacia(ModBlocks.GOLD_ACACIA_LOG).build());
        FeatureUtils.register(context, IRON_ACACIA_TREE, Feature.TREE, oreAcacia(ModBlocks.IRON_ACACIA_LOG).build());

        FeatureUtils.register(context, COPPER_SPRUCE_TREE, Feature.TREE, oreSpruce(ModBlocks.COPPER_SPRUCE_LOG).build());
        FeatureUtils.register(context, GOLD_SPRUCE_TREE, Feature.TREE, oreSpruce(ModBlocks.GOLD_SPRUCE_LOG).build());
        FeatureUtils.register(context, IRON_SPRUCE_TREE, Feature.TREE, oreSpruce(ModBlocks.IRON_SPRUCE_LOG).build());

        FeatureUtils.register(context, COPPER_JUNGLE_TREE, Feature.TREE, oreJungle(ModBlocks.COPPER_JUNGLE_LOG).build());
        FeatureUtils.register(context, GOLD_JUNGLE_TREE, Feature.TREE, oreJungle(ModBlocks.GOLD_JUNGLE_LOG).build());
        FeatureUtils.register(context, IRON_JUNGLE_TREE, Feature.TREE, oreJungle(ModBlocks.IRON_JUNGLE_LOG).build());

        FeatureUtils.register(context, COPPER_DARK_OAK_TREE, Feature.TREE, oreDarkOak(ModBlocks.COPPER_DARK_OAK_LOG).build());
        FeatureUtils.register(context, GOLD_DARK_OAK_TREE, Feature.TREE, oreDarkOak(ModBlocks.GOLD_DARK_OAK_LOG).build());
        FeatureUtils.register(context, IRON_DARK_OAK_TREE, Feature.TREE, oreDarkOak(ModBlocks.IRON_DARK_OAK_LOG).build());
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(MOD_ID, name));
    }
}
