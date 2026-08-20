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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FireBowlEntity extends BlockEntity implements ImplementedInventory, WorldlyContainer {
    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    private int cookingTime = 0;
    private int cookingTotalTime = 200;
    private static final int DEFAULT_COOKING_TOTAL_TIME = 150;

    private int unlitTime = 0;
    private static final int UNLIT_TOTAL_TIME = 100;


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

    public void setInput(ItemStack input, int cookingTotalTime) {
        items.set(INPUT_SLOT, input);
        this.cookingTime = 0;
        this.cookingTotalTime = cookingTotalTime;
    }

    public ItemStack getOutput() {
        return items.get(OUTPUT_SLOT);
    }

    public void spawnContainingItems() {
        // Drop all stored items
        Containers.dropContents(level, worldPosition, items);
        items.clear();

        setChanged();
    }

    public static void clientTick(Level world, BlockPos pos, BlockState state, FireBowlEntity fireBowl) {
        RandomSource random = world.random;
        if (random.nextFloat() < 0.11F) {
            for (var i = 0; i < random.nextInt(2) + 2; ++i) {
                CampfireBlock.makeParticles(world, pos, false, false);
            }
        }
    }

    public static void litServerTick(Level world, BlockPos pos, BlockState state, FireBowlEntity fireBowl) {
        ItemStack input = fireBowl.getInput();
        ItemStack output = fireBowl.getOutput();

        if (!input.isEmpty()) {
            fireBowl.cookingTime++;
            if (fireBowl.cookingTime >= fireBowl.cookingTotalTime) {
                fireBowl.setInput(ItemStack.EMPTY, DEFAULT_COOKING_TOTAL_TIME);

                var recipeInput = new SingleRecipeInput(input);
                // Recipe lookup lives on ServerLevel; this ticker only runs server-side.
                var result = ((ServerLevel) world).recipeAccess()
                        .getRecipeFor(ModRecipes.BURNING_RECIPE_TYPE, recipeInput, world)
                        .map(burningRecipe -> burningRecipe.value().assemble(recipeInput, world.registryAccess()))
                        .orElse(input);

                if (output.isEmpty()) {
                    fireBowl.items.set(OUTPUT_SLOT, result.copy());
                } else if (ItemStack.isSameItem(output, result) && output.getCount() < output.getMaxStackSize()) {
                    output.grow(1);
                } else {
                    Containers.dropContents(world, pos, new SimpleContainer(result));
                }

                world.playSound(null, pos, SoundEvents.GENERIC_BURN, SoundSource.BLOCKS, 1f, 0.5f);

                // From now on the unlit timer ticks.
                fireBowl.unlitTime = 0;
                world.sendBlockUpdated(pos, state, state, 3);
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
            fireBowl.cookingTime = Mth.clamp(fireBowl.cookingTime - 2, 0, fireBowl.cookingTotalTime);
            fireBowl.setChanged();
        }
    }

    public Optional<BurningRecipe> getRecipeFor(ServerLevel level, ItemStack item) {
        return !getInput().isEmpty()
                ? Optional.empty()
                : level.recipeAccess()
                        .getRecipeFor(ModRecipes.BURNING_RECIPE_TYPE, new SingleRecipeInput(item), level)
                        .map(RecipeHolder::value);
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

    @Override
    public void setItem(int slot, ItemStack stack) {
        ImplementedInventory.super.setItem(slot, stack);

        // As the items are used for the custom entity renderer we have trigger a sync to ensure that the items are
        // also available for the client.
        if (level != null) {
            this.setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}