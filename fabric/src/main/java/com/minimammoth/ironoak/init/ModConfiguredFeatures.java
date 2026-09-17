package com.minimammoth.ironoak.init;

import java.util.List;
import java.util.OptionalInt;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
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

    public static final ResourceKey<Feature> COPPER_OAK_TREE = registerKey("copper_oak_tree");
    public static final ResourceKey<Feature> GOLD_OAK_TREE = registerKey("gold_oak_tree");
    public static final ResourceKey<Feature> IRON_OAK_TREE = registerKey("iron_oak_tree");

    public static final ResourceKey<Feature> COPPER_BIRCH_TREE = registerKey("copper_birch_tree");
    public static final ResourceKey<Feature> GOLD_BIRCH_TREE = registerKey("gold_birch_tree");
    public static final ResourceKey<Feature> IRON_BIRCH_TREE = registerKey("iron_birch_tree");

    public static final ResourceKey<Feature> COPPER_ACACIA_TREE = registerKey("copper_acacia_tree");
    public static final ResourceKey<Feature> GOLD_ACACIA_TREE = registerKey("gold_acacia_tree");
    public static final ResourceKey<Feature> IRON_ACACIA_TREE = registerKey("iron_acacia_tree");

    public static final ResourceKey<Feature> COPPER_SPRUCE_TREE = registerKey("copper_spruce_tree");
    public static final ResourceKey<Feature> GOLD_SPRUCE_TREE = registerKey("gold_spruce_tree");
    public static final ResourceKey<Feature> IRON_SPRUCE_TREE = registerKey("iron_spruce_tree");

    public static final ResourceKey<Feature> COPPER_JUNGLE_TREE = registerKey("copper_jungle_tree");
    public static final ResourceKey<Feature> GOLD_JUNGLE_TREE = registerKey("gold_jungle_tree");
    public static final ResourceKey<Feature> IRON_JUNGLE_TREE = registerKey("iron_jungle_tree");

    public static final ResourceKey<Feature> COPPER_DARK_OAK_TREE = registerKey("copper_dark_oak_tree");
    public static final ResourceKey<Feature> GOLD_DARK_OAK_TREE = registerKey("gold_dark_oak_tree");
    public static final ResourceKey<Feature> IRON_DARK_OAK_TREE = registerKey("iron_dark_oak_tree");

    private static TreeFeature.Builder oreOak(Block oreLog) {
        return new TreeFeature.Builder(BlockStateProvider.of(oreLog), new StraightTrunkPlacer(4, 2, 0), BlockStateProvider.of(Blocks.OAK_LEAVES), new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3), new TwoLayersFeatureSize(1, 0, 1), BlockStateProvider.holderOf(Blocks.DIRT)).ignoreVines();
    }

    private static TreeFeature.Builder oreBirch(Block oreLog) {
        return new TreeFeature.Builder(BlockStateProvider.of(oreLog), new StraightTrunkPlacer(5, 2, 0), BlockStateProvider.of(Blocks.BIRCH_LEAVES), new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3), new TwoLayersFeatureSize(1, 0, 1), BlockStateProvider.holderOf(Blocks.DIRT)).ignoreVines();
    }

    private static TreeFeature.Builder oreAcacia(Block oreLog) {
        return new TreeFeature.Builder(BlockStateProvider.of(oreLog), new ForkingTrunkPlacer(5, 2, 2), BlockStateProvider.of(Blocks.ACACIA_LEAVES), new AcaciaFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0)), new TwoLayersFeatureSize(1, 0, 2), BlockStateProvider.holderOf(Blocks.DIRT)).ignoreVines();
    }

    private static TreeFeature.Builder oreSpruce(Block oreLog) {
        return new TreeFeature.Builder(BlockStateProvider.of(oreLog), new StraightTrunkPlacer(5, 2, 1), BlockStateProvider.of(Blocks.SPRUCE_LEAVES), new SpruceFoliagePlacer(UniformInt.of(2, 3), UniformInt.of(0, 2), UniformInt.of(1, 2)), new TwoLayersFeatureSize(2, 0, 2), BlockStateProvider.holderOf(Blocks.DIRT)).ignoreVines();
    }

    private static TreeFeature.Builder oreDarkOak(Block oreLog) {
        return new TreeFeature.Builder(BlockStateProvider.of(oreLog), new DarkOakTrunkPlacer(6, 2, 1), BlockStateProvider.of(Blocks.DARK_OAK_LEAVES), new DarkOakFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0)), new ThreeLayersFeatureSize(1, 1, 0, 1, 2, OptionalInt.empty()), BlockStateProvider.holderOf(Blocks.DIRT)).ignoreVines();
    }

    private static TreeFeature.Builder oreJungle(Block oreLog) {
        return new TreeFeature.Builder(BlockStateProvider.of(oreLog), // Trunk block provider
                new StraightTrunkPlacer(4, 8, 0), // places a straight trunk
                BlockStateProvider.of(Blocks.JUNGLE_LEAVES), // Foliage block provider
                new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3), // places leaves as a blob (radius, offset from trunk, height)
                new TwoLayersFeatureSize(1, 0, 1), // The width of the tree at different layers; used to see how tall the tree can be without clipping into blocks
                BlockStateProvider.holderOf(Blocks.DIRT)
        ).decorators(List.of(new CocoaDecorator(0.2F), TrunkVineDecorator.INSTANCE, new LeaveVineDecorator(0.25F))).ignoreVines();
    }


    public static void bootstrap(BootstrapContext<Feature> context) {
        context.register(COPPER_OAK_TREE, oreOak(ModBlocks.COPPER_OAK_LOG).build());
        context.register(GOLD_OAK_TREE, oreOak(ModBlocks.GOLD_OAK_LOG).build());
        context.register(IRON_OAK_TREE, oreOak(ModBlocks.IRON_OAK_LOG).build());

        context.register(COPPER_BIRCH_TREE, oreBirch(ModBlocks.COPPER_BIRCH_LOG).build());
        context.register(GOLD_BIRCH_TREE, oreBirch(ModBlocks.GOLD_BIRCH_LOG).build());
        context.register(IRON_BIRCH_TREE, oreBirch(ModBlocks.IRON_BIRCH_LOG).build());

        context.register(COPPER_ACACIA_TREE, oreAcacia(ModBlocks.COPPER_ACACIA_LOG).build());
        context.register(GOLD_ACACIA_TREE, oreAcacia(ModBlocks.GOLD_ACACIA_LOG).build());
        context.register(IRON_ACACIA_TREE, oreAcacia(ModBlocks.IRON_ACACIA_LOG).build());

        context.register(COPPER_SPRUCE_TREE, oreSpruce(ModBlocks.COPPER_SPRUCE_LOG).build());
        context.register(GOLD_SPRUCE_TREE, oreSpruce(ModBlocks.GOLD_SPRUCE_LOG).build());
        context.register(IRON_SPRUCE_TREE, oreSpruce(ModBlocks.IRON_SPRUCE_LOG).build());

        context.register(COPPER_JUNGLE_TREE, oreJungle(ModBlocks.COPPER_JUNGLE_LOG).build());
        context.register(GOLD_JUNGLE_TREE, oreJungle(ModBlocks.GOLD_JUNGLE_LOG).build());
        context.register(IRON_JUNGLE_TREE, oreJungle(ModBlocks.IRON_JUNGLE_LOG).build());

        context.register(COPPER_DARK_OAK_TREE, oreDarkOak(ModBlocks.COPPER_DARK_OAK_LOG).build());
        context.register(GOLD_DARK_OAK_TREE, oreDarkOak(ModBlocks.GOLD_DARK_OAK_LOG).build());
        context.register(IRON_DARK_OAK_TREE, oreDarkOak(ModBlocks.IRON_DARK_OAK_LOG).build());
    }

    public static ResourceKey<Feature> registerKey(String name) {
        return ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(MOD_ID, name));
    }
}
