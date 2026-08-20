package com.minimammoth.ironoak.init;

import com.minimammoth.ironoak.FireBowlBlock;
import com.minimammoth.ironoak.OreInfusedSaplingBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

import static com.minimammoth.ironoak.IronOak.MOD_ID;

/**
 * Since 1.21.2 a block's settings must carry its own registry key, so construction and
 * registration happen together in {@link #register} — a block can no longer be built as a
 * static constant and registered later.
 */
public class ModBlocks {
    private ModBlocks() {
    }

    public static final Block FIRE_BOWL = register("fire_bowl", FireBowlBlock::new, Blocks.CAULDRON);

    public static final Block COPPER_OAK_LOG = log("copper_oak_log", Blocks.OAK_LOG);
    public static final Block COPPER_OAK_SAPLING = sapling("copper_oak_sapling", ModSaplingGenerators.COPPER_OAK, Blocks.OAK_SAPLING);
    public static final Block GOLD_OAK_LOG = log("gold_oak_log", Blocks.OAK_LOG);
    public static final Block GOLD_OAK_SAPLING = sapling("gold_oak_sapling", ModSaplingGenerators.GOLD_OAK, Blocks.OAK_SAPLING);
    public static final Block IRON_OAK_LOG = log("iron_oak_log", Blocks.OAK_LOG);
    public static final Block IRON_OAK_SAPLING = sapling("iron_oak_sapling", ModSaplingGenerators.IRON_OAK, Blocks.OAK_SAPLING);

    public static final Block COPPER_ACACIA_LOG = log("copper_acacia_log", Blocks.ACACIA_LOG);
    public static final Block COPPER_ACACIA_SAPLING = sapling("copper_acacia_sapling", ModSaplingGenerators.COPPER_ACACIA, Blocks.ACACIA_SAPLING);
    public static final Block GOLD_ACACIA_LOG = log("gold_acacia_log", Blocks.ACACIA_LOG);
    public static final Block GOLD_ACACIA_SAPLING = sapling("gold_acacia_sapling", ModSaplingGenerators.GOLD_ACACIA, Blocks.ACACIA_SAPLING);
    public static final Block IRON_ACACIA_LOG = log("iron_acacia_log", Blocks.ACACIA_LOG);
    public static final Block IRON_ACACIA_SAPLING = sapling("iron_acacia_sapling", ModSaplingGenerators.IRON_ACACIA, Blocks.ACACIA_SAPLING);

    public static final Block COPPER_SPRUCE_LOG = log("copper_spruce_log", Blocks.SPRUCE_LOG);
    public static final Block COPPER_SPRUCE_SAPLING = sapling("copper_spruce_sapling", ModSaplingGenerators.COPPER_SPRUCE, Blocks.SPRUCE_SAPLING);
    public static final Block GOLD_SPRUCE_LOG = log("gold_spruce_log", Blocks.SPRUCE_LOG);
    public static final Block GOLD_SPRUCE_SAPLING = sapling("gold_spruce_sapling", ModSaplingGenerators.GOLD_SPRUCE, Blocks.SPRUCE_SAPLING);
    public static final Block IRON_SPRUCE_LOG = log("iron_spruce_log", Blocks.SPRUCE_LOG);
    public static final Block IRON_SPRUCE_SAPLING = sapling("iron_spruce_sapling", ModSaplingGenerators.IRON_SPRUCE, Blocks.SPRUCE_SAPLING);

    public static final Block COPPER_JUNGLE_LOG = log("copper_jungle_log", Blocks.JUNGLE_LOG);
    public static final Block COPPER_JUNGLE_SAPLING = sapling("copper_jungle_sapling", ModSaplingGenerators.COPPER_JUNGLE, Blocks.JUNGLE_SAPLING);
    public static final Block GOLD_JUNGLE_LOG = log("gold_jungle_log", Blocks.JUNGLE_LOG);
    public static final Block GOLD_JUNGLE_SAPLING = sapling("gold_jungle_sapling", ModSaplingGenerators.GOLD_JUNGLE, Blocks.JUNGLE_SAPLING);
    public static final Block IRON_JUNGLE_LOG = log("iron_jungle_log", Blocks.JUNGLE_LOG);
    public static final Block IRON_JUNGLE_SAPLING = sapling("iron_jungle_sapling", ModSaplingGenerators.IRON_JUNGLE, Blocks.JUNGLE_SAPLING);

    public static final Block COPPER_BIRCH_LOG = log("copper_birch_log", Blocks.BIRCH_LOG);
    public static final Block COPPER_BIRCH_SAPLING = sapling("copper_birch_sapling", ModSaplingGenerators.COPPER_BIRCH, Blocks.BIRCH_SAPLING);
    public static final Block GOLD_BIRCH_LOG = log("gold_birch_log", Blocks.BIRCH_LOG);
    public static final Block GOLD_BIRCH_SAPLING = sapling("gold_birch_sapling", ModSaplingGenerators.GOLD_BIRCH, Blocks.BIRCH_SAPLING);
    public static final Block IRON_BIRCH_LOG = log("iron_birch_log", Blocks.BIRCH_LOG);
    public static final Block IRON_BIRCH_SAPLING = sapling("iron_birch_sapling", ModSaplingGenerators.IRON_BIRCH, Blocks.BIRCH_SAPLING);

    public static final Block COPPER_DARK_OAK_LOG = log("copper_dark_oak_log", Blocks.DARK_OAK_LOG);
    public static final Block COPPER_DARK_OAK_SAPLING = sapling("copper_dark_oak_sapling", ModSaplingGenerators.COPPER_DARK_OAK, Blocks.DARK_OAK_SAPLING);
    public static final Block GOLD_DARK_OAK_LOG = log("gold_dark_oak_log", Blocks.DARK_OAK_LOG);
    public static final Block GOLD_DARK_OAK_SAPLING = sapling("gold_dark_oak_sapling", ModSaplingGenerators.GOLD_DARK_OAK, Blocks.DARK_OAK_SAPLING);
    public static final Block IRON_DARK_OAK_LOG = log("iron_dark_oak_log", Blocks.DARK_OAK_LOG);
    public static final Block IRON_DARK_OAK_SAPLING = sapling("iron_dark_oak_sapling", ModSaplingGenerators.IRON_DARK_OAK, Blocks.DARK_OAK_SAPLING);

    private static Block log(String name, Block copyFrom) {
        return register(name, RotatedPillarBlock::new, copyFrom);
    }

    private static Block sapling(String name, TreeGrower grower, Block copyFrom) {
        return register(name, settings -> new OreInfusedSaplingBlock(grower, settings), copyFrom);
    }

    /**
     * {@code ofLegacyCopy} — not {@code ofFullCopy} — is the equivalent of the settings
     * copy this mod used before 1.20.5. {@code ofFullCopy} would also copy the source
     * block's loot table, so an infused log would drop a plain vanilla log instead of
     * using the mod's own loot table.
     */
    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, Block copyFrom) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));
        BlockBehaviour.Properties settings = BlockBehaviour.Properties.ofLegacyCopy(copyFrom).setId(key);
        return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(settings));
    }

    public static void onInitialize() {
        // Loading this class registers every block above. Kept so the mod initializer can
        // force that to happen at a defined point.
    }
}
