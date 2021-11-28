package com.minimammoth.ironoak;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FireBowlEntity extends BlockEntity {
    public final SimpleInventory inventory = new SimpleInventory(1);

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

        nbt.put("item", inventory.toNbtList());
    }

    /**
     * Restores state for entity from world save.
     */
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        if (nbt.contains("item", NbtElement.LIST_TYPE)) {
            inventory.readNbtList(nbt.getList("item", NbtElement.COMPOUND_TYPE));
        }
    }

    /**
     * Used to sync state to client when chunk is loaded. Necessary for custom renderer to have the same info.
     */
    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}