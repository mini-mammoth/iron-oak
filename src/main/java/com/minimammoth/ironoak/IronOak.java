package com.minimammoth.ironoak;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.PillarBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
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

public class IronOak implements ModInitializer {
    private static final String MOD_ID = "iron_oak";

    public static final Block FIRE_BOWL = new FireBowlBlock(FabricBlockSettings.copyOf(Blocks.CAULDRON));

    public static final Block IRON_OAK_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.OAK_LOG));

    public static final ConfiguredFeature<?, ?> IRON_OAK_TREE = Feature.TREE
            // Configure the feature using the builder
            .configure(new TreeFeatureConfig.Builder(
                    BlockStateProvider.of(IRON_OAK_LOG), // Trunk block provider
                    new StraightTrunkPlacer(4, 2, 0), // places a straight trunk
                    BlockStateProvider.of(Blocks.OAK_LEAVES), // Foliage block provider
                    new BlobFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0), 3), // places leaves as a blob (radius, offset from trunk, height)
                    new TwoLayersFeatureSize(1, 0, 1) // The width of the tree at different layers; used to see how tall the tree can be without clipping into blocks
            ).ignoreVines().build());

    public static final Block IRON_OAK_SAPLING = new IronOakSaplingBlock(FabricBlockSettings.copyOf(Blocks.OAK_SAPLING));

    public static final Item IRON_ASH = new IronAsh(new FabricItemSettings().group(ItemGroup.MISC));
    public static final Item IRON_SHRED = new Item(new FabricItemSettings().group(ItemGroup.MISC));

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "fire_bowl"), FIRE_BOWL);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "iron_oak_log"), IRON_OAK_LOG);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "iron_oak_sapling"), IRON_OAK_SAPLING);

        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "fire_bowl"), new BlockItem(FIRE_BOWL, new FabricItemSettings().group(ItemGroup.MISC)));
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "iron_ash"), IRON_ASH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "iron_bone_meal"), new IronBoneMeal(new FabricItemSettings().group(ItemGroup.MISC)));
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "iron_oak_log"), new BlockItem(IRON_OAK_LOG, new FabricItemSettings().group(ItemGroup.MISC)));
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "iron_oak_sapling"), new BlockItem(IRON_OAK_SAPLING, new FabricItemSettings().group(ItemGroup.MISC)));
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "iron_shred"), IRON_SHRED);

        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new Identifier(MOD_ID, "iron_oak_tree"), IRON_OAK_TREE);
    }
}
