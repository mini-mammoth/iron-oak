package com.minimammoth.ironoak;

import com.minimammoth.ironoak.init.ModEntityTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SeparatorBlock extends BlockWithEntity {
    public static final VoxelShape SHAPE = makeShape();


    public SeparatorBlock(Settings settings) {
        super(settings);

        this.setDefaultState(super.getDefaultState()
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
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
        return new SeparatorEntity(pos, state);
    }


    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient) {
            return checkType(type, ModEntityTypes.SEPARATOR_ENTITY, SeparatorEntity::clientTick);
        } else {
            return checkType(type, ModEntityTypes.SEPARATOR_ENTITY, SeparatorEntity::serverTick);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stackInHand = player.getStackInHand(hand);

        if (!(world.getBlockEntity(pos) instanceof SeparatorEntity entity)) {
            return ActionResult.PASS;
        }

        // Right click with empty hand to remove stored items
        if (stackInHand.isEmpty() && hand == Hand.MAIN_HAND) {
            if (entity.getInput().isEmpty() && entity.getOutput().isEmpty()) {
                return ActionResult.FAIL;
            }

            entity.spawnContainingItems();

            world.playSound(player, pos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.PLAYERS, 1f, 1f);

            return ActionResult.success(world.isClient);
        }


        // You can place anything with is dry able into the bowl. But only one at a time.
        if (!stackInHand.isEmpty()) {
            var recipe = entity.getRecipeFor(stackInHand);
            if (recipe.isPresent()) {
                var stackToStore = stackInHand.copy();
                stackToStore.setCount(1);

                // Use isEmpty instead of canInsert to ensure that we only have exactly one wood block to process
                if (!entity.getInput().isEmpty() || !entity.getOutput().isEmpty()) {
                    return ActionResult.FAIL;
                }

                entity.setInput(stackToStore, recipe.get().getCookTime());
                world.playSound(player, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.PLAYERS, 1f, 1f);
                stackInHand.decrement(1);

                entity.markDirty();

                return ActionResult.success(world.isClient);
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SeparatorEntity dryRackEntity) {
                dryRackEntity.spawnContainingItems();
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    /**
     * Generated Shape Outline from BlockBench Model
     */
    public static VoxelShape makeShape() {
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.125, 0, 0.0625, 0.5, 0.0625, 0.4375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.125, 0, 0.5625, 0.5, 0.0625, 0.9375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0.0625, 0.0625, 0.5625, 0.25, 0.4375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0625, 0.0625, 0.0625, 0.125, 0.25, 0.4375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.125, 0.8125, 0.125, 0.875, 0.8125, 0.875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.125, 0.75, 0, 0.875, 1, 0.125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0.75, 0.125, 0.125, 1, 0.875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.875, 0.75, 0.125, 1, 1, 0.875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.125, 0.5, 0.125, 0.5, 0.75, 0.875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.125, 0.0625, 0, 0.5, 0.25, 0.0625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.125, 0, 0.4375, 0.5, 0.5, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.125, 0.0625, 0.9375, 0.5, 0.25, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0625, 0.0625, 0.5625, 0.125, 0.25, 0.9375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0.0625, 0.5625, 0.5625, 0.25, 0.9375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.625, 0, 0.125, 0.875, 0.625, 0.375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.125, 0.75, 0.875, 0.875, 1, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.625, 0, 0.625, 0.875, 0.625, 0.875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0.625, 0.125, 0.875, 0.75, 0.875));

        return shape;
    }
}
