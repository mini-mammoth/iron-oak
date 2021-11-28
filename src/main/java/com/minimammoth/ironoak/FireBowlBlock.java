package com.minimammoth.ironoak;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FireBowlBlock extends BlockWithEntity {
    public static final VoxelShape SHAPE = createCuboidShape(0, 0, 0, 16, 12, 16);

    public FireBowlBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        // With inheriting from BlockWithEntity this defaults to INVISIBLE, so we need to change that!
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FireBowlEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, IronOak.FIRE_BOWL_ENTITY, (world1, pos, state1, be) -> be.tick(world1, pos, state1));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stackInHand = player.getStackInHand(hand);

        // Shift right click to remove stored items
        if (player.isSneaking()) {
            if (!(world.getBlockEntity(pos) instanceof FireBowlEntity entity)) {
                return ActionResult.PASS;
            }

            if (entity.inventory.isEmpty()) {
                return ActionResult.FAIL;
            }

            var storedStack = entity.inventory.removeStack(0);
            if (!player.giveItemStack(storedStack)) {
                player.dropItem(storedStack, true);
            }

            world.playSound(player, pos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.PLAYERS, 1f, 1f);

            entity.markDirty();

            return ActionResult.success(world.isClient);
        }

        // You can place any log into the fire bowl. But only one at a time.
        if (!stackInHand.isEmpty() && ItemTags.LOGS_THAT_BURN.contains(stackInHand.getItem())) {
            if (!(world.getBlockEntity(pos) instanceof FireBowlEntity entity)) {
                return ActionResult.PASS;
            }

            var stackToStore = stackInHand.copy();
            stackToStore.setCount(1);

            // Use isEmpty instead of canInsert to ensure that we only have exactly one wood block to process
            if (!entity.inventory.isEmpty()) {
                return ActionResult.FAIL;
            }

            entity.inventory.addStack(stackToStore);
            world.playSound(player, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1f, 1f);
            stackInHand.decrement(1);

            entity.markDirty();

            return ActionResult.success(world.isClient);
        }

        return ActionResult.PASS;
    }
}
