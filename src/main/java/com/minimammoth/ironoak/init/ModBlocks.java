package com.minimammoth.ironoak.init;

import com.minimammoth.ironoak.FireBowlBlock;
import com.minimammoth.ironoak.OreInfusedSaplingBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.PillarBlock;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static com.minimammoth.ironoak.IronOak.MOD_ID;

public class ModBlocks {
    private ModBlocks() {
    }

    public static final Block FIRE_BOWL = new FireBowlBlock(FabricBlockSettings.copyOf(Blocks.CAULDRON));

    public static final Block COPPER_OAK_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.OAK_LOG));
    public static final Block COPPER_OAK_SAPLING = new OreInfusedSaplingBlock(ModSaplingGenerators.COPPER_OAK, FabricBlockSettings.copyOf(Blocks.OAK_SAPLING));

    public static final Block GOLD_OAK_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.OAK_LOG));
    public static final Block GOLD_OAK_SAPLING = new OreInfusedSaplingBlock(ModSaplingGenerators.GOLD_OAK, FabricBlockSettings.copyOf(Blocks.OAK_SAPLING));

    public static final Block IRON_OAK_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.OAK_LOG));
    public static final Block IRON_OAK_SAPLING = new OreInfusedSaplingBlock(ModSaplingGenerators.IRON_OAK, FabricBlockSettings.copyOf(Blocks.OAK_SAPLING));

    public static final Block COPPER_ACACIA_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.ACACIA_LOG));
    public static final Block COPPER_ACACIA_SAPLING = new OreInfusedSaplingBlock(ModSaplingGenerators.COPPER_ACACIA, FabricBlockSettings.copyOf(Blocks.ACACIA_SAPLING));

    public static final Block GOLD_ACACIA_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.ACACIA_LOG));
    public static final Block GOLD_ACACIA_SAPLING = new OreInfusedSaplingBlock(ModSaplingGenerators.GOLD_ACACIA, FabricBlockSettings.copyOf(Blocks.ACACIA_SAPLING));

    public static final Block IRON_ACACIA_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.ACACIA_LOG));
    public static final Block IRON_ACACIA_SAPLING = new OreInfusedSaplingBlock(ModSaplingGenerators.IRON_ACACIA, FabricBlockSettings.copyOf(Blocks.ACACIA_SAPLING));

    public static final Block COPPER_SPRUCE_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.SPRUCE_LOG));
    public static final Block COPPER_SPRUCE_SAPLING = new OreInfusedSaplingBlock(ModSaplingGenerators.COPPER_SPRUCE, FabricBlockSettings.copyOf(Blocks.SPRUCE_SAPLING));

    public static final Block GOLD_SPRUCE_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.SPRUCE_LOG));
    public static final Block GOLD_SPRUCE_SAPLING = new OreInfusedSaplingBlock(ModSaplingGenerators.GOLD_SPRUCE, FabricBlockSettings.copyOf(Blocks.SPRUCE_SAPLING));

    public static final Block IRON_SPRUCE_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.SPRUCE_LOG));
    public static final Block IRON_SPRUCE_SAPLING = new OreInfusedSaplingBlock(ModSaplingGenerators.IRON_SPRUCE, FabricBlockSettings.copyOf(Blocks.SPRUCE_SAPLING));

    public static final Block COPPER_JUNGLE_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.JUNGLE_LOG));
    public static final Block COPPER_JUNGLE_SAPLING = new OreInfusedSaplingBlock(ModSaplingGenerators.COPPER_JUNGLE, FabricBlockSettings.copyOf(Blocks.JUNGLE_SAPLING));

    public static final Block GOLD_JUNGLE_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.JUNGLE_LOG));
    public static final Block GOLD_JUNGLE_SAPLING = new OreInfusedSaplingBlock(ModSaplingGenerators.GOLD_JUNGLE, FabricBlockSettings.copyOf(Blocks.JUNGLE_SAPLING));

    public static final Block IRON_JUNGLE_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.JUNGLE_LOG));
    public static final Block IRON_JUNGLE_SAPLING = new OreInfusedSaplingBlock(ModSaplingGenerators.IRON_JUNGLE, FabricBlockSettings.copyOf(Blocks.JUNGLE_SAPLING));

    public static final Block COPPER_BIRCH_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.BIRCH_LOG));
    public static final Block COPPER_BIRCH_SAPLING = new OreInfusedSaplingBlock(ModSaplingGenerators.COPPER_BIRCH, FabricBlockSettings.copyOf(Blocks.BIRCH_SAPLING));

    public static final Block GOLD_BIRCH_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.BIRCH_LOG));
    public static final Block GOLD_BIRCH_SAPLING = new OreInfusedSaplingBlock(ModSaplingGenerators.GOLD_BIRCH, FabricBlockSettings.copyOf(Blocks.BIRCH_SAPLING));

    public static final Block IRON_BIRCH_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.BIRCH_LOG));
    public static final Block IRON_BIRCH_SAPLING = new OreInfusedSaplingBlock(ModSaplingGenerators.IRON_BIRCH, FabricBlockSettings.copyOf(Blocks.BIRCH_SAPLING));

    public static final Block COPPER_DARK_OAK_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.DARK_OAK_LOG));
    public static final Block COPPER_DARK_OAK_SAPLING = new OreInfusedSaplingBlock(ModSaplingGenerators.COPPER_DARK_OAK, FabricBlockSettings.copyOf(Blocks.DARK_OAK_SAPLING));

    public static final Block GOLD_DARK_OAK_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.DARK_OAK_LOG));
    public static final Block GOLD_DARK_OAK_SAPLING = new OreInfusedSaplingBlock(ModSaplingGenerators.GOLD_DARK_OAK, FabricBlockSettings.copyOf(Blocks.DARK_OAK_SAPLING));

    public static final Block IRON_DARK_OAK_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.DARK_OAK_LOG));
    public static final Block IRON_DARK_OAK_SAPLING = new OreInfusedSaplingBlock(ModSaplingGenerators.IRON_DARK_OAK, FabricBlockSettings.copyOf(Blocks.DARK_OAK_SAPLING));

    public static void onInitialize() {
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "fire_bowl"), FIRE_BOWL);

        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "copper_oak_log"), COPPER_OAK_LOG);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "copper_oak_sapling"), COPPER_OAK_SAPLING);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "gold_oak_log"), GOLD_OAK_LOG);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "gold_oak_sapling"), GOLD_OAK_SAPLING);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "iron_oak_log"), IRON_OAK_LOG);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "iron_oak_sapling"), IRON_OAK_SAPLING);

        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "copper_acacia_log"), COPPER_ACACIA_LOG);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "copper_acacia_sapling"), COPPER_ACACIA_SAPLING);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "gold_acacia_log"), GOLD_ACACIA_LOG);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "gold_acacia_sapling"), GOLD_ACACIA_SAPLING);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "iron_acacia_log"), IRON_ACACIA_LOG);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "iron_acacia_sapling"), IRON_ACACIA_SAPLING);

        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "copper_spruce_log"), COPPER_SPRUCE_LOG);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "copper_spruce_sapling"), COPPER_SPRUCE_SAPLING);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "gold_spruce_log"), GOLD_SPRUCE_LOG);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "gold_spruce_sapling"), GOLD_SPRUCE_SAPLING);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "iron_spruce_log"), IRON_SPRUCE_LOG);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "iron_spruce_sapling"), IRON_SPRUCE_SAPLING);

        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "copper_birch_log"), COPPER_BIRCH_LOG);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "copper_birch_sapling"), COPPER_BIRCH_SAPLING);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "gold_birch_log"), GOLD_BIRCH_LOG);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "gold_birch_sapling"), GOLD_BIRCH_SAPLING);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "iron_birch_log"), IRON_BIRCH_LOG);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "iron_birch_sapling"), IRON_BIRCH_SAPLING);

        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "copper_jungle_log"), COPPER_JUNGLE_LOG);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "copper_jungle_sapling"), COPPER_JUNGLE_SAPLING);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "gold_jungle_log"), GOLD_JUNGLE_LOG);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "gold_jungle_sapling"), GOLD_JUNGLE_SAPLING);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "iron_jungle_log"), IRON_JUNGLE_LOG);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "iron_jungle_sapling"), IRON_JUNGLE_SAPLING);

        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "copper_dark_oak_log"), COPPER_DARK_OAK_LOG);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "copper_dark_oak_sapling"), COPPER_DARK_OAK_SAPLING);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "gold_dark_oak_log"), GOLD_DARK_OAK_LOG);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "gold_dark_oak_sapling"), GOLD_DARK_OAK_SAPLING);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "iron_dark_oak_log"), IRON_DARK_OAK_LOG);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "iron_dark_oak_sapling"), IRON_DARK_OAK_SAPLING);
    }
}
