package com.minimammoth.ironoak.init;

import com.minimammoth.ironoak.OreInfusedAsh;
import com.minimammoth.ironoak.OreInfusedBoneMeal;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static com.minimammoth.ironoak.IronOak.MOD_ID;

public class ModItems {
    private ModItems() {
    }

    private static Item IRON_OAK_SAPLING;

    public static final ItemGroup DEFAULT_ITEM_GROUP = FabricItemGroupBuilder
            .create(new Identifier(MOD_ID, "iron_oak"))
            .icon(() -> new ItemStack(IRON_OAK_SAPLING))
            .build();

    private static final Item COPPER_OAK_SAPLING = new BlockItem(ModBlocks.COPPER_OAK_SAPLING, new FabricItemSettings().group(DEFAULT_ITEM_GROUP));
    public static final Item COPPER_ASH = new OreInfusedAsh(new FabricItemSettings().group(DEFAULT_ITEM_GROUP));
    public static final Item COPPER_BONE_MEAL = new OreInfusedBoneMeal(new FabricItemSettings().group(DEFAULT_ITEM_GROUP)) {
        @Override
        protected BlockState getOreSapling() {
            return ModBlocks.COPPER_OAK_SAPLING.getDefaultState();
        }
    };
    public static final Item COPPER_SHRED = new Item(new FabricItemSettings().group(DEFAULT_ITEM_GROUP));

    private static final Item GOLD_OAK_SAPLING = new BlockItem(ModBlocks.GOLD_OAK_SAPLING, new FabricItemSettings().group(DEFAULT_ITEM_GROUP));
    public static final Item GOLD_ASH = new OreInfusedAsh(new FabricItemSettings().group(DEFAULT_ITEM_GROUP));
    public static final Item GOLD_BONE_MEAL = new OreInfusedBoneMeal(new FabricItemSettings().group(DEFAULT_ITEM_GROUP)) {
        @Override
        protected BlockState getOreSapling() {
            return ModBlocks.GOLD_OAK_SAPLING.getDefaultState();
        }
    };
    public static final Item GOLD_SHRED = new Item(new FabricItemSettings().group(DEFAULT_ITEM_GROUP));

    public static final Item IRON_ASH = new OreInfusedAsh(new FabricItemSettings().group(DEFAULT_ITEM_GROUP));
    public static final Item IRON_BONE_MEAL = new OreInfusedBoneMeal(new FabricItemSettings().group(DEFAULT_ITEM_GROUP)) {
        @Override
        protected BlockState getOreSapling() {
            return ModBlocks.IRON_OAK_SAPLING.getDefaultState();
        }
    };
    public static final Item IRON_SHRED = new Item(new FabricItemSettings().group(DEFAULT_ITEM_GROUP));

    public static void onInitialize() {
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "fire_bowl"), new BlockItem(ModBlocks.FIRE_BOWL, new FabricItemSettings().maxCount(1).group(DEFAULT_ITEM_GROUP)));

        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "copper_bone_meal"), COPPER_BONE_MEAL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "copper_oak_sapling"), COPPER_OAK_SAPLING);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "copper_oak_log"), new BlockItem(ModBlocks.COPPER_OAK_LOG, new FabricItemSettings().group(DEFAULT_ITEM_GROUP)));
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "copper_ash"), COPPER_ASH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "copper_shred"), COPPER_SHRED);

        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "gold_bone_meal"), GOLD_BONE_MEAL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "gold_oak_sapling"), GOLD_OAK_SAPLING);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "gold_oak_log"), new BlockItem(ModBlocks.GOLD_OAK_LOG, new FabricItemSettings().group(DEFAULT_ITEM_GROUP)));
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "gold_ash"), GOLD_ASH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "gold_shred"), GOLD_SHRED);

        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "iron_bone_meal"), IRON_BONE_MEAL);
        IRON_OAK_SAPLING = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "iron_oak_sapling"), new BlockItem(ModBlocks.IRON_OAK_SAPLING, new FabricItemSettings().group(DEFAULT_ITEM_GROUP)));
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "iron_oak_log"), new BlockItem(ModBlocks.IRON_OAK_LOG, new FabricItemSettings().group(DEFAULT_ITEM_GROUP)));
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "iron_ash"), IRON_ASH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "iron_shred"), IRON_SHRED);
    }
}
