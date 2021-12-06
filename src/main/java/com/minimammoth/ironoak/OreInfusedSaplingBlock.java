package com.minimammoth.ironoak;

import net.minecraft.block.SaplingBlock;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Supplier;

public class OreInfusedSaplingBlock extends SaplingBlock {
    public OreInfusedSaplingBlock(final Supplier<ConfiguredFeature<?, ?>> treeFeature, Settings settings) {
        super(new SaplingGenerator() {
            @Nullable
            @Override
            protected ConfiguredFeature<?, ?> getTreeFeature(Random random, boolean bees) {
                return treeFeature.get();
            }
        }, settings);
    }
}