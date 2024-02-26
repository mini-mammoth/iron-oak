package com.minimammoth.ironoak;

import com.minimammoth.ironoak.helper.ImplementedInventory;
import com.minimammoth.ironoak.init.ModEntityTypes;
import com.minimammoth.ironoak.init.ModRecipes;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FireBowlEntity extends BlockEntity implements ImplementedInventory, SidedInventory {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(2, ItemStack.EMPTY);

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
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        var inputNbt = new NbtCompound();
        Inventories.writeNbt(inputNbt, DefaultedList.copyOf(ItemStack.EMPTY, getInput()));
        nbt.put("input", inputNbt);

        nbt.put("cooking_time", NbtInt.of(cookingTime));

        var outputNbt = new NbtCompound();
        Inventories.writeNbt(outputNbt, DefaultedList.copyOf(ItemStack.EMPTY, getOutput()));
        nbt.put("output", outputNbt);
    }

    /**
     * Restores state for entity from world save.
     */
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        if (nbt.contains("input", NbtElement.COMPOUND_TYPE)) {
            var inputList = DefaultedList.ofSize(1, ItemStack.EMPTY);
            Inventories.readNbt(nbt.getCompound("input"), inputList);
            items.set(INPUT_SLOT, inputList.get(0));
        }

        if (nbt.contains("cooking_time", NbtElement.INT_TYPE)) {
            cookingTime = nbt.getInt("cooking_time");
        }

        if (nbt.contains("output", NbtElement.COMPOUND_TYPE)) {
            var outputList = DefaultedList.ofSize(1, ItemStack.EMPTY);
            Inventories.readNbt(nbt.getCompound("output"), outputList);
            items.set(OUTPUT_SLOT, outputList.get(0));
        }
    }


    /**
     * Used to sync server changes to client on demand.
     *
     * @return
     */
    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    /**
     * Used to sync state to client when chunk is loaded. Necessary for custom renderer to have the same info.
     */
    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
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
        ItemScatterer.spawn(world, pos, items);
        items.clear();

        markDirty();
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, FireBowlEntity fireBowl) {
        Random random = world.random;
        if (random.nextFloat() < 0.11F) {
            for (var i = 0; i < random.nextInt(2) + 2; ++i) {
                CampfireBlock.spawnSmokeParticle(world, pos, false, false);
            }
        }
    }

    public static void litServerTick(World world, BlockPos pos, BlockState state, FireBowlEntity fireBowl) {
        ItemStack input = fireBowl.getInput();
        ItemStack output = fireBowl.getOutput();

        if (!input.isEmpty()) {
            fireBowl.cookingTime++;
            if (fireBowl.cookingTime >= fireBowl.cookingTotalTime) {
                fireBowl.setInput(ItemStack.EMPTY, DEFAULT_COOKING_TOTAL_TIME);

                Inventory inventory = new SimpleInventory(input);
                var result = world.getRecipeManager()
                        .getFirstMatch(ModRecipes.BURNING_RECIPE_TYPE, inventory, world)
                        .map(campfireCookingRecipe -> campfireCookingRecipe.value().craft(inventory, null))
                        .orElse(input);

                if (output.isEmpty()) {
                    fireBowl.items.set(OUTPUT_SLOT, result.copy());
                } else if (ItemStack.areItemsEqual(output, result) && output.getCount() < output.getMaxCount()) {
                    output.increment(1);
                } else {
                    ItemScatterer.spawn(world, pos, new SimpleInventory(result));
                }

                world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_BURN, SoundCategory.BLOCKS, 1f, 0.5f);

                // From now on the unlit timer ticks.
                fireBowl.unlitTime = 0;
                world.updateListeners(pos, state, state, 3);
            }
        } else {
            fireBowl.unlitTime++;
            if (fireBowl.unlitTime >= UNLIT_TOTAL_TIME) {
                world.setBlockState(pos, state.with(FireBowlBlock.LIT, false), 3);
            }
        }

        fireBowl.markDirty();
    }

    /**
     * If the fire does not burn the cooking time is slowly resetting.
     * <p>
     * Borrowed from {@code CampfireBlockEntity}
     */
    public static void unlitServerTick(World world, BlockPos pos, BlockState state, FireBowlEntity fireBowl) {
        if (fireBowl.cookingTime > 0) {
            fireBowl.cookingTime = MathHelper.clamp(fireBowl.cookingTime - 2, 0, fireBowl.cookingTotalTime);
            fireBowl.markDirty();
        }
    }

    public Optional<BurningRecipe> getRecipeFor(ItemStack item) {
        return !getInput().isEmpty() ? Optional.empty() : this.world.getRecipeManager().getFirstMatch(ModRecipes.BURNING_RECIPE_TYPE, new SimpleInventory(item), this.world).map(RecipeEntry::value);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return switch (side) {
            case DOWN -> new int[]{OUTPUT_SLOT};
            case UP -> new int[]{INPUT_SLOT};
            case NORTH, SOUTH, WEST, EAST -> new int[]{INPUT_SLOT, OUTPUT_SLOT};
        };
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return slot == INPUT_SLOT && getInput().isEmpty() && getRecipeFor(stack).isPresent();
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == OUTPUT_SLOT;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        ImplementedInventory.super.setStack(slot, stack);

        // As the items are used for the custom entity renderer we have trigger a sync to ensure that the items are
        // also available for the client.
        if (world != null) {
            this.markDirty();
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }
}