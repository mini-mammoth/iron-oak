package com.minimammoth.ironoak;

import com.minimammoth.ironoak.init.ModEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class DryRackEntity extends BlockEntity {
    public DryRackEntity(BlockPos pos, BlockState state) {
        super(ModEntityTypes.DRY_RACK_ENTITY, pos, state);
    }

    /**
     * Writes current state of the entity on world save.
     * <p>
     * Call {@code BlockEntity.markDirty} to enforce a save.
     */
    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
    }

    /**
     * Restores state for entity from world save.
     */
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
    }

    /**
     * Used to sync server changes to client on demand.
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
}
