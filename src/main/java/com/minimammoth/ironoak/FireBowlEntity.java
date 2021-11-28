package com.minimammoth.ironoak;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FireBowlEntity extends BlockEntity {
    private ItemStack input = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;

    public FireBowlEntity(BlockPos pos, BlockState state) {
        super(IronOak.FIRE_BOWL_ENTITY, pos, state);
    }

    public void tick(World world, BlockPos pos, BlockState state) {

    }

    /**
     * Writes current state of the entity on world save.
     * <p>
     * Call {@code BlockEntity.markDirty} to enforce a save.
     */
    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.put("input", input.getNbt());
        nbt.put("output", output.getNbt());
    }

    /**
     * Restores state for entity from world save.
     */
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        if (nbt.contains("input", NbtElement.COMPOUND_TYPE)) {
            input = ItemStack.fromNbt(nbt.getCompound("input"));
        }

        if (nbt.contains("output", NbtElement.COMPOUND_TYPE)) {
            output = ItemStack.fromNbt(nbt.getCompound("output"));
        }
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
    }

    public ItemStack getOutput() {
        return output;
    }

    public void spawnContainingItems() {
        // Drop all stored items
        ItemScatterer.spawn(world, pos, new SimpleInventory(input, output));
    }
}