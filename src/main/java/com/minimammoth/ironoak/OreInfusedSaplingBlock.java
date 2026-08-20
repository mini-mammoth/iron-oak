package com.minimammoth.ironoak;

import java.util.function.Supplier;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;

public class OreInfusedSaplingBlock extends SaplingBlock {
    public OreInfusedSaplingBlock(TreeGrower generator, Properties settings) {
        super(generator, settings);
    }
}