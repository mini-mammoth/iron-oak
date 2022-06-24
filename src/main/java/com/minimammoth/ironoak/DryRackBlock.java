package com.minimammoth.ironoak;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class DryRackBlock extends BlockWithEntity {
    public static final EnumProperty<SlabType> TYPE = Properties.SLAB_TYPE;

    public static final VoxelShape BOTTOM_SHAPE = makeBottomShape();
    public static final VoxelShape TOP_SHAPE = makeTopShape();
    public static final VoxelShape DOUBLE_SHAPE = makeDoubleShape();


    public DryRackBlock(Settings settings) {
        super(settings);

        this.setDefaultState(super.getDefaultState()
                .with(TYPE, SlabType.BOTTOM)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TYPE);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(TYPE)) {
            case TOP -> TOP_SHAPE;
            case BOTTOM -> BOTTOM_SHAPE;
            case DOUBLE -> DOUBLE_SHAPE;
        };
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        // With inheriting from BlockWithEntity this defaults to INVISIBLE, so we need to change that!
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DryRackEntity(pos, state);
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        // Inspired from SlabBlock used to detect if two racks can merged into a double one
        ItemStack itemStack = context.getStack();
        var slabType = state.get(TYPE);
        if (slabType != SlabType.DOUBLE && itemStack.isOf(this.asItem())) {
            if (context.canReplaceExisting()) {
                boolean bl = context.getHitPos().y - context.getBlockPos().getY() > 0.5;
                Direction direction = context.getSide();
                if (slabType == SlabType.BOTTOM) {
                    return direction == Direction.UP || bl && direction.getAxis().isHorizontal();
                } else {
                    return direction == Direction.DOWN || !bl && direction.getAxis().isHorizontal();
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        // Inspired from SlabBlock
        var pos = ctx.getBlockPos();
        var state = ctx.getWorld().getBlockState(pos);

        // Merge two racks into a double one
        if (state.isOf(this)) {
            return state.with(TYPE, SlabType.DOUBLE);
        } else {
            Direction direction = ctx.getSide();

            // Check if dry rack is placed upwards
            return direction != Direction.DOWN && (direction == Direction.UP || ctx.getHitPos().y - pos.getY() <= 0.5)
                    ? this.getDefaultState().with(TYPE, SlabType.BOTTOM)
                    : this.getDefaultState().with(TYPE, SlabType.TOP);
        }
    }

    /**
     * Generated Shape Outline from BlockBench Model
     */
    public static VoxelShape makeDoubleShape() {
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.875, 0, 0, 1, 1, 0.125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.875, 0, 0.875, 1, 1, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0, 0.125, 1, 0.125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0.875, 0.125, 1, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0625, 0.1875, 0.0625, 0.9375, 0.25, 0.9375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0625, 0.6875, 0.0625, 0.9375, 0.75, 0.9375));

        return shape;
    }

    /**
     * Generated Shape Outline from BlockBench Model
     */
    public static VoxelShape makeBottomShape() {
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.875, 0, 0, 1, 0.5, 0.125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.875, 0, 0.875, 1, 0.5, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0, 0.125, 0.5, 0.125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0.875, 0.125, 0.5, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0625, 0.1875, 0.0625, 0.9375, 0.25, 0.9375));

        return shape;
    }

    public static VoxelShape makeTopShape() {
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.875, 0.5, 0, 1, 1, 0.125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.875, 0.5, 0.875, 1, 1, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0.5, 0, 0.125, 1, 0.125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0.5, 0.875, 0.125, 1, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0625, 0.6875, 0.0625, 0.9375, 0.75, 0.9375));

        return shape;
    }
}
