package com.minimammoth.ironoak;

import com.minimammoth.ironoak.init.ModRecipes;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.server.level.ServerLevel;
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
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        var stackInHand = player.getItemInHand(hand);
        var blockHitResult = getPlayerPOVHitResult(world, player, ClipContext.Fluid.SOURCE_ONLY);

        if (blockHitResult.getType() == HitResult.Type.MISS || blockHitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }

        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        var pos = blockHitResult.getBlockPos();
        var blockState = world.getBlockState(pos);

        // Recipe resolution is server-only, so bail out early on the client.
        if (!(world instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        var input = new SingleRecipeInput(stackInHand);
        var recipe = serverLevel.recipeAccess().getRecipeFor(ModRecipes.WASHING_RECIPE_TYPE, input, serverLevel);

        if (blockState.getBlock() == Blocks.WATER && recipe.isPresent()) {
            player.awardStat(Stats.ITEM_USED.get(this));
            stackInHand.shrink(1);

            var output = recipe.get().value().assemble(input, serverLevel.registryAccess());

            var ironShard = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), output);
            ironShard.setPickUpDelay(40);
            ironShard.setThrower(player);

            world.addFreshEntity(ironShard);
            world.playSound(player, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.5f, 1.0f);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }
}
