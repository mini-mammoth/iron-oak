package com.minimammoth.ironoak;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Random;

/**
 * See related {@code FireBowlEntity} and {@code FireBowlRenderer}
 */
public class FireBowlBlock extends BlockWithEntity implements FluidFillable {
    public static final VoxelShape SHAPE = createCuboidShape(0, 0, 0, 16, 12, 16);
    public static final float FIRE_DAME = 1.0f;

    public static final BooleanProperty LIT = Properties.LIT;
    private static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public FireBowlBlock(Settings settings) {
        super(settings);

        this.setDefaultState(super.getDefaultState()
                .with(FireBowlBlock.LIT, false)
                .with(FireBowlBlock.WATERLOGGED, false)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FireBowlBlock.LIT);
        // We need the waterlogged property to be recognized as a lit-able campfire. Otherwise, flint and steel will not
        // work on this item
        builder.add(FireBowlBlock.WATERLOGGED);
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

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient) {
            return Boolean.TRUE.equals(state.get(LIT)) ? checkType(type, IronOak.FIRE_BOWL_ENTITY, FireBowlEntity::clientTick) : null;
        } else {
            return Boolean.TRUE.equals(state.get(LIT)) ? checkType(type, IronOak.FIRE_BOWL_ENTITY, FireBowlEntity::litServerTick) : checkType(type, IronOak.FIRE_BOWL_ENTITY, FireBowlEntity::unlitServerTick);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stackInHand = player.getStackInHand(hand);

        // Right click with empty hand to remove stored items
        if (stackInHand.isEmpty() && hand == Hand.MAIN_HAND) {
            if (!(world.getBlockEntity(pos) instanceof FireBowlEntity entity)) {
                return ActionResult.PASS;
            }

            // Touching the fire bowl while it's on will hurt you.
            if (doFireDamage(state, player)) {
                return ActionResult.FAIL;
            }

            if (entity.getInput().isEmpty() && entity.getOutput().isEmpty()) {
                return ActionResult.FAIL;
            }

            entity.spawnContainingItems();

            world.playSound(player, pos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.PLAYERS, 1f, 1f);

            return ActionResult.success(world.isClient);
        }

        // You can place any log into the fire bowl. But only one at a time.
        if (!stackInHand.isEmpty() && stackInHand.isIn(ItemTags.LOGS_THAT_BURN)) {
            if (!(world.getBlockEntity(pos) instanceof FireBowlEntity entity)) {
                return ActionResult.FAIL;
            }

            var stackToStore = stackInHand.copy();
            stackToStore.setCount(1);

            // Use isEmpty instead of canInsert to ensure that we only have exactly one wood block to process
            if (!entity.getInput().isEmpty()) {
                return ActionResult.FAIL;
            }

            entity.setInput(stackToStore);
            world.playSound(player, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1f, 1f);
            stackInHand.decrement(1);

            entity.markDirty();

            return ActionResult.success(world.isClient);
        }

        return ActionResult.PASS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (Boolean.TRUE.equals(state.get(LIT))) {
                // If a burning fire bowl is destroyed, all items get lost. :/
                return;
            }

            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof FireBowlEntity fireBowl) {
                fireBowl.spawnContainingItems();
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        doFireDamage(state, entity);
        super.onEntityCollision(state, world, pos, entity);
    }

    /**
     * Checks all conditions (lit, immunity) and applies fire damage if they met.
     *
     * @return True, if fire damage is applied.
     */
    private boolean doFireDamage(BlockState state, Entity entity) {
        if (!entity.isFireImmune() && Boolean.TRUE.equals(state.get(LIT)) && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity) entity)) {
            entity.damage(DamageSource.IN_FIRE, FIRE_DAME);
            return true;
        }

        return false;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        // Fire should always be off if fire bowl is placed.
        return Optional.ofNullable(super.getPlacementState(ctx)).orElse(getDefaultState()).with(LIT, false);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (Boolean.TRUE.equals(state.get(LIT))) {
            if (random.nextInt(10) == 0) {
                world.playSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
            }

            if (random.nextInt(5) == 0) {
                for (int i = 0; i < random.nextInt(1) + 1; ++i) {
                    world.addParticle(ParticleTypes.LAVA, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, random.nextFloat() / 2.0F, 5.0E-5D, (random.nextFloat() / 2.0F));
                }
            }
        }
    }

    public static void extinguish(@Nullable Entity entity, WorldAccess world, BlockPos pos) {
        if (world.isClient()) {
            for (int i = 0; i < 20; ++i) {
                CampfireBlock.spawnSmokeParticle((World) world, pos, false, true);
            }
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof FireBowlEntity fireBowl) {
            fireBowl.spawnContainingItems();
        }

        world.emitGameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
    }

    @Override
    public boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return state.get(LIT) && fluid.isIn(FluidTags.WATER);
    }

    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (Boolean.TRUE.equals(state.get(LIT)) && fluidState.isIn(FluidTags.WATER)) {
            if (!world.isClient()) {
                world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            extinguish(null, world, pos);

            world.setBlockState(pos, state.with(LIT, false), 3);
            world.createAndScheduleFluidTick(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Shooting with a burning arrow should lit the fire.
     */
    @Override
    public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        BlockPos blockPos = hit.getBlockPos();
        if (!world.isClient && projectile.isOnFire() && projectile.canModifyAt(world, blockPos) && Boolean.TRUE.equals(state.get(LIT))) {
            world.setBlockState(blockPos, state.with(Properties.LIT, true), 11);
        }
    }
}
