package com.minimammoth.ironoak.init;

import com.minimammoth.ironoak.FireBowlBlock;
import com.minimammoth.ironoak.IronOakSaplingBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.PillarBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static com.minimammoth.ironoak.IronOak.MOD_ID;

public class ModBlocks {
    private ModBlocks() {
    }

    public static final Block FIRE_BOWL = new FireBowlBlock(FabricBlockSettings.copyOf(Blocks.CAULDRON));
    public static final Block IRON_OAK_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.OAK_LOG));

    public static final Block IRON_OAK_SAPLING = new IronOakSaplingBlock(FabricBlockSettings.copyOf(Blocks.OAK_SAPLING));

    public static void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "fire_bowl"), FIRE_BOWL);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "iron_oak_log"), IRON_OAK_LOG);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "iron_oak_sapling"), IRON_OAK_SAPLING);
    }
}
