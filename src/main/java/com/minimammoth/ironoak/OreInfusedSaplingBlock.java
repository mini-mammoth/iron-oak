package com.minimammoth.ironoak;

import net.minecraft.block.SaplingBlock;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.gen.feature.ConfiguredFeature;

import java.util.function.Supplier;

public class OreInfusedSaplingBlock extends SaplingBlock {
    public OreInfusedSaplingBlock(final Supplier<RegistryEntry<? extends ConfiguredFeature<?, ?>>> treeFeature, Settings settings) {
        super(new SaplingGenerator() {
            @Override
            protected RegistryEntry<? extends ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees) {
                return treeFeature.get();
            }
        }, settings);
    }
}