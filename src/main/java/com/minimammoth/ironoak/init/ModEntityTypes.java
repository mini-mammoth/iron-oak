package com.minimammoth.ironoak.init;

import com.minimammoth.ironoak.FireBowlEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static com.minimammoth.ironoak.IronOak.MOD_ID;

public class ModEntityTypes {
    private ModEntityTypes() {
    }

    public static BlockEntityType<FireBowlEntity> FIRE_BOWL_ENTITY;

    public static void onInitialize() {
        FIRE_BOWL_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "fire_bowl"),
                FabricBlockEntityTypeBuilder.create(FireBowlEntity::new, ModBlocks.FIRE_BOWL).build(null));
    }
}
