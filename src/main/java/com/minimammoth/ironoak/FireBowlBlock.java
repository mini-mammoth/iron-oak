package com.minimammoth.ironoak;

import com.minimammoth.ironoak.init.ModEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
     * The three return values are not interchangeable, and this block needs all three
     * because it is both a container you empty by hand and a block you can set alight.
     * {@code ServerPlayerGameMode.useItemOn} — and its identical client twin
     * {@code MultiPlayerGameMode.performUseItemOn} — dispatches like this:
     * <ol>
     *   <li>a {@code Success} ({@code SUCCESS} / {@code SUCCESS_SERVER} / {@code CONSUME})
     *       ends the interaction here;</li>
     *   <li>{@code TRY_WITH_EMPTY_HAND} runs {@link #useWithoutItem} next, and only if
     *       <em>that</em> does not consume does the held item get a turn;</li>
     *   <li>{@code PASS} skips {@link #useWithoutItem} entirely and goes straight to
     *       {@code itemStack.useOn(...)}.</li>
     * </ol>
     * So {@code PASS}, not {@code TRY_WITH_EMPTY_HAND}, is what hands an item this block
     * has no use for back to that item — it is the direct successor of 1.20.4's
     * {@code ActionResult.PASS}. {@code TRY_WITH_EMPTY_HAND} sent flint and steel into
     * {@link #useWithoutItem}, which consumed the interaction whenever the bowl had
     * something to remove, so a loaded bowl could not be lit.
     */
    @Override
    protected InteractionResult useItemOn(ItemStack stackInHand, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof FireBowlEntity entity)) {
            return InteractionResult.PASS;
        }

        // An empty hand is not an item interaction. TRY_WITH_EMPTY_HAND is what routes it
        // on to useWithoutItem, which takes the contents back out. Safe on both sides: the
        // contents are synced, so the client predicts the same answer the server will give.
        if (stackInHand.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        // Burning recipes only resolve on a ServerLevel, so the client genuinely cannot
        // tell an insertable log from a flint and steel. CONSUME suppresses the client's
        // own prediction of the held item without claiming a swing — the swing then comes
        // from whatever the server decides. This is the shape CampfireBlock uses when it
        // knows it would handle the item but is not on the server. The interaction packet
        // is sent regardless of what is returned here, so the server still gets its say.
        if (!(world instanceof ServerLevel serverLevel)) {
            return InteractionResult.CONSUME;
        }

        var recipe = entity.getRecipeFor(serverLevel, stackInHand);
        if (recipe.isEmpty()) {
            // Nothing this block can do with the held item. Also covers a log offered to a
            // bowl whose input is already occupied, because getRecipeFor guards on that —
            // which is what made the old `!entity.getInput().isEmpty()` check below
            // unreachable. 1.20.4 returned PASS in exactly this situation too.
            return InteractionResult.PASS;
        }

        // One log at a time; getRecipeFor already refused if the input slot is taken.
        var stackToStore = stackInHand.copy();
        stackToStore.setCount(1);

        entity.setInput(stackToStore, recipe.get().cookingTime());
        world.playSound(player, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1f, 1f);
        stackInHand.shrink(1);

        // SUCCESS_SERVER, not SUCCESS: the client returned CONSUME above and therefore did
        // not swing, so the swing has to be driven from here. Only a Success whose
        // swingSource is SERVER makes ServerGamePacketListenerImpl swing the arm.
        return InteractionResult.SUCCESS_SERVER;
    }

    /**
     * Drop the stored items before the block goes away. {@code onRemove} was replaced by
     * {@code affectNeighborsAfterRemoval}, which runs after the block entity is gone — so
     * the contents have to be spawned here, while it still exists.
     */
    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (!Boolean.TRUE.equals(state.getValue(LIT))
                && world.getBlockEntity(pos) instanceof FireBowlEntity fireBowl) {
            // A burning fire bowl loses its contents. :/
            fireBowl.spawnContainingItems();
        }

        return super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean movedByPiston) {
        Containers.updateNeighboursAfterDestroy(state, world, pos);
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean bl) {
        doFireDamage(state, world, entity);
        super.entityInside(state, world, pos, entity, effectApplier, bl);
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
    public boolean canPlaceLiquid(@Nullable LivingEntity placer, BlockGetter world, BlockPos pos, BlockState state, Fluid fluid) {
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
     */
    @Override
    protected void onProjectileHit(Level world, BlockState state, BlockHitResult hit, Projectile projectile) {
        BlockPos blockPos = hit.getBlockPos();
        if (world instanceof ServerLevel serverLevel && projectile.isOnFire() && projectile.mayInteract(serverLevel, blockPos) && Boolean.TRUE.equals(state.getValue(LIT))) {
            world.setBlock(blockPos, state.setValue(BlockStateProperties.LIT, true), 11);
        }
    }
}
