package com.minimammoth.ironoak;

import com.minimammoth.ironoak.init.ModRecipes;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

/**
 * Drop {@code OreInfusedAsh} into water to apply a washing recipe.
 */
public class OreInfusedAsh extends Item {
    public OreInfusedAsh(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        var stackInHand = player.getStackInHand(hand);
        var blockHitResult = raycast(world, player, RaycastContext.FluidHandling.SOURCE_ONLY);

        if (blockHitResult.getType() == HitResult.Type.MISS || blockHitResult.getType() != HitResult.Type.BLOCK) {
            return TypedActionResult.pass(stackInHand);
        }

        if (hand != Hand.MAIN_HAND) {
            return TypedActionResult.pass(stackInHand);
        }

        var pos = blockHitResult.getBlockPos();
        var blockState = world.getBlockState(pos);

        var input = new SimpleInventory(stackInHand);
        var recipe = world.getRecipeManager().getFirstMatch(ModRecipes.WASHING_RECIPE_TYPE, input, world);

        if (blockState.getBlock() == Blocks.WATER && recipe.isPresent()) {
            player.incrementStat(Stats.USED.getOrCreateStat(this));
            stackInHand.decrement(1);

            var output = recipe.get().craft(input);

            var ironShard = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), output);
            ironShard.setPickupDelay(40);
            ironShard.setThrower(player.getUuid());

            world.spawnEntity(ironShard);
            world.playSound(player, pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 0.5f, 1.0f);

            return TypedActionResult.success(stackInHand, world.isClient());
        }

        return TypedActionResult.fail(stackInHand);
    }
}
