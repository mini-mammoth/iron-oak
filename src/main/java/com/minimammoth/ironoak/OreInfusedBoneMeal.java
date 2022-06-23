package com.minimammoth.ironoak;

import net.minecraft.block.Block;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;

import java.util.Map;

/**
 * If {@code IronBoneMeal} is used on an oak sapling, it has a chance to convert the sapling into an iron oak sapling.
 * If used anywhere else it acts like normal {@code BoneMeal}.
 */
public class OreInfusedBoneMeal extends BoneMealItem {
    private final Map<Block, Block> infusionMap;

    public OreInfusedBoneMeal(Settings settings, Map<Block, Block> infusionMap) {
        super(settings);
        this.infusionMap = infusionMap;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (useOnOakSapling(context)) {
            return ActionResult.success(context.getWorld().isClient);
        }

        return super.useOnBlock(context);
    }

    private boolean useOnOakSapling(ItemUsageContext context) {
        var stack = context.getStack();
        var pos = context.getBlockPos();
        var world = context.getWorld();

        var state = world.getBlockState(pos);

        if (infusionMap.containsKey(state.getBlock())) {
            world.setBlockState(pos, infusionMap.get(state.getBlock()).getDefaultState());

            BoneMealItem.createParticles(world, pos, 15);
            world.playSound(context.getPlayer(), pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 0.3f, 1.0f);

            stack.decrement(1);

            return true;
        }

        return false;
    }
}
