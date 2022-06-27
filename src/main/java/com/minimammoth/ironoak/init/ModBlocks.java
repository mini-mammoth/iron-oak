package com.minimammoth.ironoak.init;

import com.minimammoth.ironoak.DryRackBlock;
import com.minimammoth.ironoak.FireBowlBlock;
import com.minimammoth.ironoak.OreInfusedSaplingBlock;
import com.minimammoth.ironoak.SeparatorBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.Material;
import net.minecraft.block.PillarBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;

import static com.minimammoth.ironoak.IronOak.MOD_ID;

public class ModBlocks {
    private ModBlocks() {
    }

    public static final Block FIRE_BOWL = new FireBowlBlock(FabricBlockSettings.copyOf(Blocks.CAULDRON));
    public static final Block DRY_RACK = new DryRackBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS));
    public static final Block SEPARATOR = new SeparatorBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS));

    public static final Block COPPER_OAK_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.OAK_LOG));
    public static final Block COPPER_OAK_SAPLING = new OreInfusedSaplingBlock(() -> ModConfiguredFeatures.COPPER_OAK_TREE, FabricBlockSettings.copyOf(Blocks.OAK_SAPLING));

    public static final Block GOLD_OAK_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.OAK_LOG));
    public static final Block GOLD_OAK_SAPLING = new OreInfusedSaplingBlock(() -> ModConfiguredFeatures.GOLD_OAK_TREE, FabricBlockSettings.copyOf(Blocks.OAK_SAPLING));

    public static final Block IRON_OAK_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.OAK_LOG));
    public static final Block IRON_OAK_SAPLING = new OreInfusedSaplingBlock(() -> ModConfiguredFeatures.IRON_OAK_TREE, FabricBlockSettings.copyOf(Blocks.OAK_SAPLING));

    public static final Block REDSTONE_OAK_LEAVES = createLeavesBlock(BlockSoundGroup.GRASS);

    public static final Block COPPER_ACACIA_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.ACACIA_LOG));
    public static final Block COPPER_ACACIA_SAPLING = new OreInfusedSaplingBlock(() -> ModConfiguredFeatures.COPPER_ACACIA_TREE, FabricBlockSettings.copyOf(Blocks.ACACIA_SAPLING));

    public static final Block GOLD_ACACIA_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.ACACIA_LOG));
    public static final Block GOLD_ACACIA_SAPLING = new OreInfusedSaplingBlock(() -> ModConfiguredFeatures.GOLD_ACACIA_TREE, FabricBlockSettings.copyOf(Blocks.ACACIA_SAPLING));

    public static final Block IRON_ACACIA_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.ACACIA_LOG));
    public static final Block IRON_ACACIA_SAPLING = new OreInfusedSaplingBlock(() -> ModConfiguredFeatures.IRON_ACACIA_TREE, FabricBlockSettings.copyOf(Blocks.ACACIA_SAPLING));

    public static final Block COPPER_SPRUCE_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.SPRUCE_LOG));
    public static final Block COPPER_SPRUCE_SAPLING = new OreInfusedSaplingBlock(() -> ModConfiguredFeatures.COPPER_SPRUCE_TREE, FabricBlockSettings.copyOf(Blocks.SPRUCE_SAPLING));

    public static final Block GOLD_SPRUCE_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.SPRUCE_LOG));
    public static final Block GOLD_SPRUCE_SAPLING = new OreInfusedSaplingBlock(() -> ModConfiguredFeatures.GOLD_SPRUCE_TREE, FabricBlockSettings.copyOf(Blocks.SPRUCE_SAPLING));

    public static final Block IRON_SPRUCE_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.SPRUCE_LOG));
    public static final Block IRON_SPRUCE_SAPLING = new OreInfusedSaplingBlock(() -> ModConfiguredFeatures.IRON_SPRUCE_TREE, FabricBlockSettings.copyOf(Blocks.SPRUCE_SAPLING));

    public static final Block COPPER_JUNGLE_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.JUNGLE_LOG));
    public static final Block COPPER_JUNGLE_SAPLING = new OreInfusedSaplingBlock(() -> ModConfiguredFeatures.COPPER_JUNGLE_TREE, FabricBlockSettings.copyOf(Blocks.JUNGLE_SAPLING));

    public static final Block GOLD_JUNGLE_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.JUNGLE_LOG));
    public static final Block GOLD_JUNGLE_SAPLING = new OreInfusedSaplingBlock(() -> ModConfiguredFeatures.GOLD_JUNGLE_TREE, FabricBlockSettings.copyOf(Blocks.JUNGLE_SAPLING));

    public static final Block IRON_JUNGLE_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.JUNGLE_LOG));
    public static final Block IRON_JUNGLE_SAPLING = new OreInfusedSaplingBlock(() -> ModConfiguredFeatures.IRON_JUNGLE_TREE, FabricBlockSettings.copyOf(Blocks.JUNGLE_SAPLING));

    public static final Block COPPER_BIRCH_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.BIRCH_LOG));
    public static final Block COPPER_BIRCH_SAPLING = new OreInfusedSaplingBlock(() -> ModConfiguredFeatures.COPPER_BIRCH_TREE, FabricBlockSettings.copyOf(Blocks.BIRCH_SAPLING));

    public static final Block GOLD_BIRCH_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.BIRCH_LOG));
    public static final Block GOLD_BIRCH_SAPLING = new OreInfusedSaplingBlock(() -> ModConfiguredFeatures.GOLD_BIRCH_TREE, FabricBlockSettings.copyOf(Blocks.BIRCH_SAPLING));

    public static final Block IRON_BIRCH_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.BIRCH_LOG));
    public static final Block IRON_BIRCH_SAPLING = new OreInfusedSaplingBlock(() -> ModConfiguredFeatures.IRON_BIRCH_TREE, FabricBlockSettings.copyOf(Blocks.BIRCH_SAPLING));

    public static final Block COPPER_DARK_OAK_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.DARK_OAK_LOG));
    public static final Block COPPER_DARK_OAK_SAPLING = new OreInfusedSaplingBlock(() -> ModConfiguredFeatures.COPPER_DARK_OAK_TREE, FabricBlockSettings.copyOf(Blocks.DARK_OAK_SAPLING));

    public static final Block GOLD_DARK_OAK_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.DARK_OAK_LOG));
    public static final Block GOLD_DARK_OAK_SAPLING = new OreInfusedSaplingBlock(() -> ModConfiguredFeatures.GOLD_DARK_OAK_TREE, FabricBlockSettings.copyOf(Blocks.DARK_OAK_SAPLING));

    public static final Block IRON_DARK_OAK_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.DARK_OAK_LOG));
    public static final Block IRON_DARK_OAK_SAPLING = new OreInfusedSaplingBlock(() -> ModConfiguredFeatures.IRON_DARK_OAK_TREE, FabricBlockSettings.copyOf(Blocks.DARK_OAK_SAPLING));

    public static void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "fire_bowl"), FIRE_BOWL);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "dry_rack"), DRY_RACK);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "separator"), SEPARATOR);

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "copper_oak_log"), COPPER_OAK_LOG);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "copper_oak_sapling"), COPPER_OAK_SAPLING);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "gold_oak_log"), GOLD_OAK_LOG);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "gold_oak_sapling"), GOLD_OAK_SAPLING);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "iron_oak_log"), IRON_OAK_LOG);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "iron_oak_sapling"), IRON_OAK_SAPLING);

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "copper_acacia_log"), COPPER_ACACIA_LOG);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "copper_acacia_sapling"), COPPER_ACACIA_SAPLING);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "gold_acacia_log"), GOLD_ACACIA_LOG);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "gold_acacia_sapling"), GOLD_ACACIA_SAPLING);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "iron_acacia_log"), IRON_ACACIA_LOG);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "iron_acacia_sapling"), IRON_ACACIA_SAPLING);

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "copper_spruce_log"), COPPER_SPRUCE_LOG);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "copper_spruce_sapling"), COPPER_SPRUCE_SAPLING);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "gold_spruce_log"), GOLD_SPRUCE_LOG);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "gold_spruce_sapling"), GOLD_SPRUCE_SAPLING);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "iron_spruce_log"), IRON_SPRUCE_LOG);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "iron_spruce_sapling"), IRON_SPRUCE_SAPLING);

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "copper_birch_log"), COPPER_BIRCH_LOG);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "copper_birch_sapling"), COPPER_BIRCH_SAPLING);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "gold_birch_log"), GOLD_BIRCH_LOG);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "gold_birch_sapling"), GOLD_BIRCH_SAPLING);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "iron_birch_log"), IRON_BIRCH_LOG);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "iron_birch_sapling"), IRON_BIRCH_SAPLING);

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "copper_jungle_log"), COPPER_JUNGLE_LOG);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "copper_jungle_sapling"), COPPER_JUNGLE_SAPLING);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "gold_jungle_log"), GOLD_JUNGLE_LOG);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "gold_jungle_sapling"), GOLD_JUNGLE_SAPLING);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "iron_jungle_log"), IRON_JUNGLE_LOG);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "iron_jungle_sapling"), IRON_JUNGLE_SAPLING);

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "copper_dark_oak_log"), COPPER_DARK_OAK_LOG);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "copper_dark_oak_sapling"), COPPER_DARK_OAK_SAPLING);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "gold_dark_oak_log"), GOLD_DARK_OAK_LOG);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "gold_dark_oak_sapling"), GOLD_DARK_OAK_SAPLING);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "iron_dark_oak_log"), IRON_DARK_OAK_LOG);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "iron_dark_oak_sapling"), IRON_DARK_OAK_SAPLING);

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "redstone_oak_leaves"), REDSTONE_OAK_LEAVES);
    }

    private static LeavesBlock createLeavesBlock(BlockSoundGroup soundGroup) {
        return new LeavesBlock(AbstractBlock.Settings.of(Material.LEAVES).strength(0.2F).ticksRandomly().sounds(soundGroup).nonOpaque().allowsSpawning(ModBlocks::canSpawnOnLeaves).suffocates(ModBlocks::never).blockVision(ModBlocks::never));
    }

    private static Boolean never(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    private static Boolean always(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    private static Boolean canSpawnOnLeaves(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
        return type == EntityType.OCELOT || type == EntityType.PARROT;
    }
}
