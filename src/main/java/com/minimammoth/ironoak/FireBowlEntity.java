package com.minimammoth.ironoak;

import com.minimammoth.ironoak.helper.ImplementedInventory;
import com.minimammoth.ironoak.init.ModEntityTypes;
import com.minimammoth.ironoak.init.ModRecipes;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FireBowlEntity extends BlockEntity implements ImplementedInventory, WorldlyContainer {
    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    /** Cook progress. Real state, so it is persisted. */
    private int cookingTime = 0;

    /**
     * Ticks a lit bowl has spent with nothing to burn. At {@link #UNLIT_TOTAL_TIME} it goes
     * out. Real state, so it is persisted — it used to reset to zero on every world load,
     * which handed an idle bowl a fresh 100 ticks every time the chunk came back.
     */
    private int unlitTime = 0;
    private static final int UNLIT_TOTAL_TIME = 100;

    /**
     * Caches the last matched burning recipe so the per-tick lookup below is a field read
     * rather than a scan. Exactly what {@code AbstractFurnaceBlockEntity} keeps for the
     * same reason. Derived, so it is neither saved nor synced.
     */
    private final RecipeManager.CachedCheck<SingleRecipeInput, BurningRecipe> quickCheck =
            RecipeManager.createCheck(ModRecipes.BURNING_RECIPE_TYPE);

    public FireBowlEntity(BlockPos pos, BlockState state) {
        super(ModEntityTypes.FIRE_BOWL_ENTITY, pos, state);
    }

    /**
     * Writes current state of the entity on world save.
     * <p>
     * Call {@code BlockEntity.markDirty} to enforce a save.
     */
    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);

        ContainerHelper.saveAllItems(out.child("input"), NonNullList.of(ItemStack.EMPTY, getInput()));
        out.putInt("cooking_time", cookingTime);
        out.putInt("unlit_time", unlitTime);
        ContainerHelper.saveAllItems(out.child("output"), NonNullList.of(ItemStack.EMPTY, getOutput()));
    }

    /**
     * Restores state for entity from world save.
     */
    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);

        in.child("input").ifPresent(child -> items.set(INPUT_SLOT, readSingle(child)));
        cookingTime = in.getIntOr("cooking_time", 0);
        // New in #28. A bowl saved before that has no such key, and 0 is the right answer
        // for one: it means the idle countdown starts fresh, which is what used to happen
        // to every bowl on every load.
        unlitTime = in.getIntOr("unlit_time", 0);
        in.child("output").ifPresent(child -> items.set(OUTPUT_SLOT, readSingle(child)));
    }

    private static ItemStack readSingle(ValueInput in) {
        var list = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, list);
        return list.get(0);
    }

    /**
     * Used to sync server changes to client on demand.
     *
     * @return
     */
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Used to sync state to client when chunk is loaded. Necessary for custom renderer to have the same info.
     */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public ItemStack getInput() {
        return items.get(INPUT_SLOT);
    }

    /**
     * Puts a stack in the input slot. No duration argument: the recipe already knows how
     * long its own input burns, so nothing needs to be told or remembered — see
     * {@link #cookingTotalTime}.
     */
    public void setInput(ItemStack input) {
        setItem(INPUT_SLOT, input);
    }

    /**
     * The burning recipe matching a stack, or empty if there is none.
     */
    private Optional<RecipeHolder<BurningRecipe>> recipeFor(ItemStack stack, ServerLevel level) {
        return stack.isEmpty()
                ? Optional.empty()
                : quickCheck.getRecipeFor(new SingleRecipeInput(stack), level);
    }

    /**
     * How long the stack currently in the input slot takes to burn.
     * <p>
     * Derived on demand and deliberately not stored. It used to be a field written in
     * exactly one place — the player insertion path — which made it wrong for a
     * hopper-inserted log and made it unrecoverable across a save, because it was never
     * persisted either. Both defects were the same defect: a value that belongs to the
     * recipe was being cached on the entity. {@code AbstractFurnaceBlockEntity} derives its
     * cook time the same way, for a block with the same shape — one input slot, fed by
     * hoppers as well as by hand.
     * <p>
     * Package-private rather than private so {@code FireBowlEntityTest} can pin the fallback
     * without a world; it is not called from anywhere else in {@code src/main}.
     */
    static int cookingTotalTime(Optional<RecipeHolder<BurningRecipe>> recipe) {
        return recipe.map(holder -> holder.value().cookingTime()).orElse(ModRecipes.DEFAULT_COOKING_TIME);
    }

    public ItemStack getOutput() {
        return items.get(OUTPUT_SLOT);
    }

    /**
     * Drops the stored items. Server-side only — {@code useWithoutItem} runs on both sides,
     * and vanilla guards the equivalent path the same way
     * ({@code JukeboxBlockEntity.popOutTheItem}). Without the guard the client spawns its
     * own item entities and wipes its copy of a container it does not own.
     */
    public void spawnContainingItems() {
        if (level == null || level.isClientSide()) {
            return;
        }

        // Drop all stored items
        Containers.dropContents(level, worldPosition, items);
        clearContent();
    }

    public static void clientTick(Level world, BlockPos pos, BlockState state, FireBowlEntity fireBowl) {
        RandomSource random = world.getRandom();
        if (random.nextFloat() < 0.11F) {
            for (var i = 0; i < random.nextInt(2) + 2; ++i) {
                CampfireBlock.makeParticles(world, pos, false, false);
            }
        }
    }

    public static void litServerTick(Level world, BlockPos pos, BlockState state, FireBowlEntity fireBowl) {
        ItemStack input = fireBowl.getInput();
        ItemStack output = fireBowl.getOutput();
        // Only registered server-side, see FireBowlBlock.getTicker.
        ServerLevel serverLevel = (ServerLevel) world;

        if (!input.isEmpty()) {
            // One lookup per tick, for both the duration and the result.
            var recipe = fireBowl.recipeFor(input, serverLevel);

            fireBowl.cookingTime++;
            if (fireBowl.cookingTime >= cookingTotalTime(recipe)) {
                fireBowl.setInput(ItemStack.EMPTY);

                var recipeInput = new SingleRecipeInput(input);
                var result = recipe
                        .map(burningRecipe -> burningRecipe.value().assemble(recipeInput))
                        .orElse(input);

                if (output.isEmpty()) {
                    fireBowl.items.set(OUTPUT_SLOT, result.copy());
                } else if (ItemStack.isSameItem(output, result) && output.getCount() < output.getMaxStackSize()) {
                    output.grow(1);
                } else {
                    Containers.dropContents(world, pos, new SimpleContainer(result.copy()));
                }

                world.playSound(null, pos, SoundEvents.GENERIC_BURN, SoundSource.BLOCKS, 1f, 0.5f);

                // From now on the unlit timer ticks.
                fireBowl.unlitTime = 0;
                fireBowl.markUpdated();
            }
        } else {
            fireBowl.unlitTime++;
            if (fireBowl.unlitTime >= UNLIT_TOTAL_TIME) {
                world.setBlock(pos, state.setValue(FireBowlBlock.LIT, false), 3);
            }
        }

        fireBowl.setChanged();
    }

    /**
     * If the fire does not burn the cooking time is slowly resetting.
     * <p>
     * Borrowed from {@code CampfireBlockEntity}
     */
    public static void unlitServerTick(Level world, BlockPos pos, BlockState state, FireBowlEntity fireBowl) {
        if (fireBowl.cookingTime > 0) {
            // Only registered server-side, see FireBowlBlock.getTicker.
            var recipe = fireBowl.recipeFor(fireBowl.getInput(), (ServerLevel) world);
            fireBowl.cookingTime = Mth.clamp(fireBowl.cookingTime - 2, 0, cookingTotalTime(recipe));
            fireBowl.setChanged();
        }
    }

    /**
     * Whether {@code item} can be put in, i.e. it has a burning recipe <em>and</em> the
     * input slot is free. Not the same question as {@link #recipeFor}, which asks only what
     * the stack already in the slot cooks into.
     */
    public Optional<BurningRecipe> getRecipeFor(ServerLevel level, ItemStack item) {
        return !getInput().isEmpty()
                ? Optional.empty()
                : recipeFor(item, level).map(RecipeHolder::value);
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return switch (side) {
            case DOWN -> new int[]{OUTPUT_SLOT};
            case UP -> new int[]{INPUT_SLOT};
            case NORTH, SOUTH, WEST, EAST -> new int[]{INPUT_SLOT, OUTPUT_SLOT};
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        // Recipes only resolve on the server; a hopper insert is a server-side action anyway.
        return slot == INPUT_SLOT && getInput().isEmpty()
                && this.level instanceof ServerLevel serverLevel
                && getRecipeFor(serverLevel, stack).isPresent();
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot == OUTPUT_SLOT;
    }

    /**
     * Pushes the container to clients.
     * <p>
     * {@code setChanged()} on its own only marks the chunk dirty for saving — it sends
     * nothing. {@code FireBowlRenderer.extractRenderState} reads the input and output
     * stacks off the <em>client's</em> block entity, so any slot change that skips this is
     * invisible in the world until the chunk reloads. Same shape as
     * {@code CampfireBlockEntity.markUpdated}.
     * <p>
     * Every write to {@link #items} outside {@code loadAdditional} has to end up here.
     */
    private void markUpdated() {
        if (level == null) {
            return;
        }

        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ImplementedInventory.super.setItem(slot, stack);

        // A new log starts from zero progress whoever put it there. Without this a
        // hopper-inserted log inherited whatever progress the previous one had left behind,
        // because only the player path reset it. Vanilla resets the same way in
        // CampfireBlockEntity.placeFood.
        if (slot == INPUT_SLOT) {
            cookingTime = 0;
        }

        markUpdated();
    }

    /**
     * The hopper's extraction path. {@code ImplementedInventory} only marks dirty here, so
     * without this a hopper pulling the output would leave it rendered in the bowl.
     */
    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack removed = ImplementedInventory.super.removeItem(slot, count);
        if (!removed.isEmpty()) {
            markUpdated();
        }

        return removed;
    }

    /**
     * Vanilla's "no update" in the name is about save-dirtying, not about the client. The
     * renderer reads this container either way, so the block update still has to go out.
     */
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = ImplementedInventory.super.removeItemNoUpdate(slot);
        if (!removed.isEmpty()) {
            markUpdated();
        }

        return removed;
    }

    @Override
    public void clearContent() {
        ImplementedInventory.super.clearContent();
        markUpdated();
    }
}