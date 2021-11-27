package com.minimammoth.ironoak;

import net.minecraft.block.SaplingBlock;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class IronOakSaplingBlock extends SaplingBlock {
    protected IronOakSaplingBlock(Settings settings) {
        super(new SaplingGenerator() {
            @Nullable
            @Override
            protected ConfiguredFeature<?, ?> getTreeFeature(Random random, boolean bees) {
                return IronOak.IRON_OAK_TREE;
            }
        }, settings);
    }
}