package com.minimammoth.ironoak;

import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

/**
 * Drop {@code IronAsh} into clear water to receive {@code IronSheds}.
 */
public class IronAsh extends Item {
    public IronAsh(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        var itemStack = player.getStackInHand(hand);
        var blockHitResult = raycast(world, player, RaycastContext.FluidHandling.SOURCE_ONLY);

        if (blockHitResult.getType() == HitResult.Type.MISS || blockHitResult.getType() != HitResult.Type.BLOCK) {
            return TypedActionResult.pass(itemStack);
        }

        var pos = blockHitResult.getBlockPos();

        var blockState = world.getBlockState(pos);
        if (blockState.getBlock() == Blocks.WATER) {
            player.incrementStat(Stats.USED.getOrCreateStat(this));
            itemStack.decrement(1);

            var ironShard = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(IronOak.IRON_SHRED, 1));
            ironShard.setPickupDelay(40);
            ironShard.setThrower(player.getUuid());

            world.spawnEntity(ironShard);
            world.playSound(player, pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 0.5f, 1.0f);

            return TypedActionResult.success(itemStack, world.isClient());
        }

        return TypedActionResult.fail(itemStack);
    }
}
