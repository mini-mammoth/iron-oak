package com.minimammoth.ironoak;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class FireBowlEntity extends BlockEntity {
    private ItemStack input = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    private int cookingTime = 0;
    private static final int COOKING_TOTAL_TIME = 150;

    private int unlitTime = 0;
    private static final int UNLIT_TOTAL_TIME = 100;


    public FireBowlEntity(BlockPos pos, BlockState state) {
        super(IronOak.FIRE_BOWL_ENTITY, pos, state);
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
        Inventories.writeNbt(inputNbt, DefaultedList.copyOf(ItemStack.EMPTY, input));
        nbt.put("input", inputNbt);

        nbt.put("cooking_time", NbtInt.of(cookingTime));

        var outputNbt = new NbtCompound();
        Inventories.writeNbt(outputNbt, DefaultedList.copyOf(ItemStack.EMPTY, output));
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
            input = inputList.get(0);
        }

        if (nbt.contains("cooking_time", NbtElement.INT_TYPE)) {
            cookingTime = nbt.getInt("cooking_time");
        }

        if (nbt.contains("output", NbtElement.COMPOUND_TYPE)) {
            var outputList = DefaultedList.ofSize(1, ItemStack.EMPTY);
            Inventories.readNbt(nbt.getCompound("output"), outputList);
            output = outputList.get(0);
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
        return input;
    }

    public void setInput(ItemStack input) {
        this.input = input;
        cookingTime = 0;
    }

    public ItemStack getOutput() {
        return output;
    }

    public void spawnContainingItems() {
        // Drop all stored items
        ItemScatterer.spawn(world, pos, new SimpleInventory(input, output));
        input = ItemStack.EMPTY;
        output = ItemStack.EMPTY;

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
        ItemStack input = fireBowl.input;
        ItemStack output = fireBowl.output;

        if (!input.isEmpty()) {
            fireBowl.cookingTime++;
            if (fireBowl.cookingTime >= COOKING_TOTAL_TIME) {
                fireBowl.setInput(ItemStack.EMPTY);
                var result = new ItemStack(IronOak.IRON_ASH, 1);

                if (output.isEmpty()) {
                    fireBowl.output = result.copy();
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
            fireBowl.cookingTime = MathHelper.clamp(fireBowl.cookingTime - 2, 0, COOKING_TOTAL_TIME);
            fireBowl.markDirty();
        }
    }
}