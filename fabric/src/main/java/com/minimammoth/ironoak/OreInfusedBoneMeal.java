package com.minimammoth.ironoak;

import java.util.Collections;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * If {@code IronBoneMeal} is used on an oak sapling, it has a chance to convert the sapling into an iron oak sapling.
 * If used anywhere else it acts like normal {@code BoneMeal}.
 */
public class OreInfusedBoneMeal extends BoneMealItem {
    private final Map<Block, Block> infusionMap;

    public OreInfusedBoneMeal(Properties settings, Map<Block, Block> infusionMap) {
        super(settings);
        this.infusionMap = infusionMap;
    }

    /**
     * Which infused sapling each vanilla sapling turns into. This is the item side of the
     * 6x3 matrix — three of these maps are hand-built in {@code ModItems}, six entries each,
     * and a rotated or missing entry is the #30 failure class with no compiler to catch it.
     */
    public Map<Block, Block> infusionMap() {
        return Collections.unmodifiableMap(infusionMap);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (useOnOakSapling(context)) {
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }

    private boolean useOnOakSapling(UseOnContext context) {
        var stack = context.getItemInHand();
        var pos = context.getClickedPos();
        var world = context.getLevel();

        var state = world.getBlockState(pos);

        if (infusionMap.containsKey(state.getBlock())) {
            world.setBlockAndUpdate(pos, infusionMap.get(state.getBlock()).defaultBlockState());

            BoneMealItem.addGrowthParticles(world, pos, 15);
            world.playSound(context.getPlayer(), pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.3f, 1.0f);

            stack.shrink(1);

            return true;
        }

        return false;
    }
}
