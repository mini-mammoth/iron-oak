package com.minimammoth.ironoak;

import com.minimammoth.ironoak.init.ModConfiguredFeatures;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class IronOakSaplingBlock extends SaplingBlock {
    public IronOakSaplingBlock(Settings settings) {
        super(new SaplingGenerator() {
            @Nullable
            @Override
            protected ConfiguredFeature<?, ?> getTreeFeature(Random random, boolean bees) {
                return ModConfiguredFeatures.IRON_OAK_TREE;
            }
        }, settings);
    }
}