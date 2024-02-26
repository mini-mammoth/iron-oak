package com.minimammoth.ironoak;

import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SaplingGenerator;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;

import java.util.function.Supplier;

public class OreInfusedSaplingBlock extends SaplingBlock {
    public OreInfusedSaplingBlock(SaplingGenerator generator, Settings settings) {
        super(generator, settings);
    }
}