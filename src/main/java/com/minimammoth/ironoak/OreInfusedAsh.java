package com.minimammoth.ironoak;

import com.minimammoth.ironoak.init.ModRecipes;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Drop {@code OreInfusedAsh} into water to apply a washing recipe.
 */
public class OreInfusedAsh extends Item {
    public OreInfusedAsh(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        var stackInHand = player.getItemInHand(hand);
        var blockHitResult = getPlayerPOVHitResult(world, player, ClipContext.Fluid.SOURCE_ONLY);

        if (blockHitResult.getType() == HitResult.Type.MISS || blockHitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stackInHand);
        }

        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stackInHand);
        }

        var pos = blockHitResult.getBlockPos();
        var blockState = world.getBlockState(pos);

        var input = new SimpleContainer(stackInHand);
        var recipe = world.getRecipeManager().getRecipeFor(ModRecipes.WASHING_RECIPE_TYPE, input, world);

        if (blockState.getBlock() == Blocks.WATER && recipe.isPresent()) {
            player.awardStat(Stats.ITEM_USED.get(this));
            stackInHand.shrink(1);

            var output = recipe.get().value().assemble(input, null);

            var ironShard = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), output);
            ironShard.setPickUpDelay(40);
            ironShard.setThrower(player);

            world.addFreshEntity(ironShard);
            world.playSound(player, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.5f, 1.0f);

            return InteractionResultHolder.sidedSuccess(stackInHand, world.isClientSide());
        }

        return InteractionResultHolder.fail(stackInHand);
    }
}
