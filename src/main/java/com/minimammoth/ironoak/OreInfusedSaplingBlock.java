package com.minimammoth.ironoak;

import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;

public class OreInfusedSaplingBlock extends SaplingBlock {
    public OreInfusedSaplingBlock(TreeGrower generator, Properties settings) {
        super(generator, settings);
    }

    /**
     * The generator this sapling grows with. {@code SaplingBlock} keeps it {@code protected},
     * so a subclass is the only thing that can hand it out — and #30 is the reason to: the
     * sapling's registered id and the generator it was handed disagreed, and no code could
     * ask about it from outside.
     */
    public TreeGrower treeGrower() {
        return treeGrower;
    }
}
