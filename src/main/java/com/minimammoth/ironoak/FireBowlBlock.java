package com.minimammoth.ironoak;

import com.minimammoth.ironoak.init.ModEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * See related {@code FireBowlEntity} and {@code FireBowlRenderer}
 */
public class FireBowlBlock extends BaseEntityBlock implements LiquidBlockContainer {
    public static final VoxelShape SHAPE = box(0, 0, 0, 16, 12, 16);
    public static final float FIRE_DAME = 1.0f;

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public FireBowlBlock(Properties settings) {
        super(settings);

        this.registerDefaultState(super.defaultBlockState()
                .setValue(FireBowlBlock.LIT, false)
                .setValue(FireBowlBlock.WATERLOGGED, false)
        );
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        // Not used yet https://fabricmc.net/2023/11/30/1203.html
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FireBowlBlock.LIT);
        // We need the waterlogged property to be recognized as a lit-able campfire. Otherwise, flint and steel will not
        // work on this item
        builder.add(FireBowlBlock.WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // With inheriting from BlockWithEntity this defaults to INVISIBLE, so we need to change that!
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FireBowlEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide()) {
            return Boolean.TRUE.equals(state.getValue(LIT)) ? createTickerHelper(type, ModEntityTypes.FIRE_BOWL_ENTITY, FireBowlEntity::clientTick) : null;
        } else {
            return Boolean.TRUE.equals(state.getValue(LIT))
                    ? createTickerHelper(type, ModEntityTypes.FIRE_BOWL_ENTITY, FireBowlEntity::litServerTick)
                    : createTickerHelper(type, ModEntityTypes.FIRE_BOWL_ENTITY, FireBowlEntity::unlitServerTick);
        }
    }

    /**
     * Right click with an empty hand to take the stored items back out.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof FireBowlEntity entity)) {
            return InteractionResult.PASS;
        }

        // Touching the fire bowl while it's on will hurt you.
        if (doFireDamage(state, world, player)) {
            return InteractionResult.FAIL;
        }

        if (entity.getInput().isEmpty() && entity.getOutput().isEmpty()) {
            return InteractionResult.FAIL;
        }

        entity.spawnContainingItems();

        world.playSound(player, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.PLAYERS, 1f, 1f);

        return InteractionResult.SUCCESS;
    }

    /**
     * You can place any log into the fire bowl. But only one at a time.
     * <p>
     * {@code ServerPlayerGameMode.useItemOn} — and its identical client twin
     * {@code MultiPlayerGameMode.performUseItemOn} — dispatches like this:
     * <ol>
     *   <li>this hook runs first, even with an empty hand;</li>
     *   <li>a consuming result ({@code SUCCESS} / {@code CONSUME} / {@code
     *       CONSUME_PARTIAL} / {@code FAIL}) ends the interaction here;</li>
     *   <li>{@code PASS_TO_DEFAULT_BLOCK_INTERACTION} on the main hand runs
     *       {@link #useWithoutItem} next, and only if <em>that</em> does not consume does
     *       the held item get a turn via {@code itemStack.useOn(...)};</li>
     *   <li>{@code SKIP_DEFAULT_BLOCK_INTERACTION} skips {@link #useWithoutItem} and goes
     *       straight to {@code itemStack.useOn(...)}.</li>
     * </ol>
     * So an empty hand has to be handed off explicitly — there is no
     * {@code TRY_WITH_EMPTY_HAND} on this version — and {@code PASS_TO_DEFAULT_BLOCK_INTERACTION}
     * is what does that.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stackInHand, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof FireBowlEntity entity)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // An empty hand is not an item interaction; let it fall through to useWithoutItem,
        // which takes the contents back out. Safe on both sides: the contents are synced,
        // so the client predicts the same answer the server will give.
        if (stackInHand.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Burning recipes only resolve on a ServerLevel, so the client genuinely cannot
        // tell an insertable log from a flint and steel. CONSUME suppresses the client's
        // own prediction of the held item without claiming a swing. This is the shape
        // CampfireBlock uses when it knows it would handle the item but is not on the
        // server. The interaction packet is sent regardless of what is returned here, so
        // the server still gets its say.
        if (!(world instanceof ServerLevel serverLevel)) {
            return ItemInteractionResult.CONSUME;
        }

        // Only asked whether a recipe exists — the duration is the entity's business, and it
        // derives it from the recipe rather than being handed it here.
        if (entity.getRecipeFor(serverLevel, stackInHand).isEmpty()) {
            // Nothing this block can do with the held item. Also covers a log offered to a
            // bowl whose input is already occupied, because getRecipeFor guards on that —
            // which is what made the old `!entity.getInput().isEmpty()` check below
            // unreachable. 1.20.4 returned PASS in exactly this situation too.
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // One log at a time; getRecipeFor already refused if the input slot is taken.
        var stackToStore = stackInHand.copy();
        stackToStore.setCount(1);

        entity.setInput(stackToStore);
        world.playSound(player, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1f, 1f);
        stackInHand.shrink(1);

        return ItemInteractionResult.SUCCESS;
    }

    /**
     * Drop the stored items before the block goes away, then hand off to vanilla's own
     * neighbour update. There is no separate {@code affectNeighborsAfterRemoval} hook on
     * this version — {@code onRemove} still does both, while the block entity is still
     * there to read.
     */
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (Boolean.TRUE.equals(state.getValue(LIT))) {
                // A burning fire bowl loses its contents. :/
                return;
            }

            if (world.getBlockEntity(pos) instanceof FireBowlEntity fireBowl) {
                fireBowl.spawnContainingItems();
            }

            super.onRemove(state, world, pos, newState, movedByPiston);
        }
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        doFireDamage(state, world, entity);
        super.entityInside(state, world, pos, entity);
    }

    /**
     * Checks all conditions (lit, immunity) and applies fire damage if they met.
     *
     * @return True, if fire damage is applied.
     */
    private boolean doFireDamage(BlockState state, Level world, Entity entity) {
        if (!entity.fireImmune() && Boolean.TRUE.equals(state.getValue(LIT))
                && entity instanceof LivingEntity living && !hasFrostWalker(world, living)) {
            entity.hurt(world.damageSources().inFire(), FIRE_DAME);
            return true;
        }

        return false;
    }

    /**
     * {@code EnchantmentHelper.hasFrostWalker} was removed, so the enchantment has to be
     * resolved from the registry to keep frost walker boots protecting the wearer.
     */
    private static boolean hasFrostWalker(Level world, LivingEntity entity) {
        return world.registryAccess()
                .lookup(Registries.ENCHANTMENT)
                .flatMap(registry -> registry.get(Enchantments.FROST_WALKER))
                .map(enchantment -> EnchantmentHelper.getEnchantmentLevel(enchantment, entity) > 0)
                .orElse(false);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // Fire should always be off if fire bowl is placed.
        return Optional.ofNullable(super.getStateForPlacement(ctx)).orElse(defaultBlockState()).setValue(LIT, false);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (Boolean.TRUE.equals(state.getValue(LIT))) {
            if (random.nextInt(10) == 0) {
                world.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
            }

            if (random.nextInt(5) == 0) {
                for (int i = 0; i < random.nextInt(1) + 1; ++i) {
                    world.addParticle(ParticleTypes.LAVA, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, random.nextFloat() / 2.0F, 5.0E-5D, (random.nextFloat() / 2.0F));
                }
            }
        }
    }

    public static void extinguish(@Nullable Entity entity, LevelAccessor world, BlockPos pos) {
        if (world.isClientSide()) {
            for (int i = 0; i < 20; ++i) {
                CampfireBlock.makeParticles((Level) world, pos, false, true);
            }
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof FireBowlEntity fireBowl) {
            fireBowl.spawnContainingItems();
        }

        world.gameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
    }

    @Override
    public boolean canPlaceLiquid(@Nullable Player placer, BlockGetter world, BlockPos pos, BlockState state, Fluid fluid) {
        return state.getValue(LIT) && fluid.is(FluidTags.WATER);
    }

    public boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (Boolean.TRUE.equals(state.getValue(LIT)) && fluidState.is(FluidTags.WATER)) {
            if (!world.isClientSide()) {
                world.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            extinguish(null, world, pos);

            world.setBlock(pos, state.setValue(LIT, false), 3);
            world.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(world));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Shooting with a burning arrow should lit the fire.
     * <p>
     * The condition is {@code CampfireBlock.onProjectileHit}'s, verbatim: the bowl must be
     * <em>un</em>lit and not waterlogged. It used to require {@code LIT} to already be true
     * before setting {@code LIT} to true, so the body was unreachable in every state where
     * it would have done anything.
     */
    @Override
    protected void onProjectileHit(Level world, BlockState state, BlockHitResult hit, Projectile projectile) {
        BlockPos blockPos = hit.getBlockPos();
        if (world instanceof ServerLevel serverLevel
                && projectile.isOnFire()
                && projectile.mayInteract(serverLevel, blockPos)
                && !state.getValue(LIT)
                && !state.getValue(WATERLOGGED)) {
            world.setBlock(blockPos, state.setValue(BlockStateProperties.LIT, true), 11);
        }
    }
}
