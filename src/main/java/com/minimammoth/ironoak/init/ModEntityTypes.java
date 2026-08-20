package com.minimammoth.ironoak.init;

import com.minimammoth.ironoak.FireBowlEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

import static com.minimammoth.ironoak.IronOak.MOD_ID;

public class ModEntityTypes {
    private ModEntityTypes() {
    }

    public static BlockEntityType<FireBowlEntity> FIRE_BOWL_ENTITY;

    public static void onInitialize() {
        FIRE_BOWL_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "fire_bowl"),
                FabricBlockEntityTypeBuilder.create(FireBowlEntity::new, ModBlocks.FIRE_BOWL).build(null));
    }
}
