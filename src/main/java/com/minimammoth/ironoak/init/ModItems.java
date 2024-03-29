package com.minimammoth.ironoak.init;

import com.minimammoth.ironoak.OreInfusedAsh;
import com.minimammoth.ironoak.OreInfusedBoneMeal;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

import static com.minimammoth.ironoak.IronOak.MOD_ID;

public class ModItems {
    private ModItems() {
    }

    private static Item IRON_OAK_SAPLING = new BlockItem(ModBlocks.IRON_OAK_SAPLING, new FabricItemSettings());

    public static final RegistryKey<ItemGroup> DEFAULT_ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, new Identifier(MOD_ID, "iron_oak"));

    private static final Item COPPER_OAK_SAPLING = new BlockItem(ModBlocks.COPPER_OAK_SAPLING, new FabricItemSettings());
    private static final Item GOLD_OAK_SAPLING = new BlockItem(ModBlocks.GOLD_OAK_SAPLING, new FabricItemSettings());

    public static final Item COPPER_ASH = new OreInfusedAsh(new FabricItemSettings());
    public static final Item COPPER_BONE_MEAL = new OreInfusedBoneMeal(new FabricItemSettings(), Map.of(
            Blocks.OAK_SAPLING, ModBlocks.COPPER_OAK_SAPLING,
            Blocks.ACACIA_SAPLING, ModBlocks.COPPER_ACACIA_SAPLING,
            Blocks.BIRCH_SAPLING, ModBlocks.COPPER_BIRCH_SAPLING,
            Blocks.JUNGLE_SAPLING, ModBlocks.COPPER_JUNGLE_SAPLING,
            Blocks.SPRUCE_SAPLING, ModBlocks.COPPER_SPRUCE_SAPLING,
            Blocks.DARK_OAK_SAPLING, ModBlocks.COPPER_DARK_OAK_SAPLING
    ));

    public static final Item COPPER_SHRED = new Item(new FabricItemSettings());

    public static final Item GOLD_ASH = new OreInfusedAsh(new FabricItemSettings());
    public static final Item GOLD_BONE_MEAL = new OreInfusedBoneMeal(new FabricItemSettings(), Map.of(
            Blocks.OAK_SAPLING, ModBlocks.GOLD_OAK_SAPLING,
            Blocks.ACACIA_SAPLING, ModBlocks.GOLD_ACACIA_SAPLING,
            Blocks.BIRCH_SAPLING, ModBlocks.GOLD_BIRCH_SAPLING,
            Blocks.JUNGLE_SAPLING, ModBlocks.GOLD_JUNGLE_SAPLING,
            Blocks.SPRUCE_SAPLING, ModBlocks.GOLD_SPRUCE_SAPLING,
            Blocks.DARK_OAK_SAPLING, ModBlocks.GOLD_DARK_OAK_SAPLING
    ));
    public static final Item GOLD_SHRED = new Item(new FabricItemSettings());

    public static final Item IRON_ASH = new OreInfusedAsh(new FabricItemSettings());
    public static final Item IRON_BONE_MEAL = new OreInfusedBoneMeal(new FabricItemSettings(), Map.of(
            Blocks.OAK_SAPLING, ModBlocks.IRON_OAK_SAPLING,
            Blocks.ACACIA_SAPLING, ModBlocks.IRON_ACACIA_SAPLING,
            Blocks.BIRCH_SAPLING, ModBlocks.IRON_BIRCH_SAPLING,
            Blocks.JUNGLE_SAPLING, ModBlocks.IRON_JUNGLE_SAPLING,
            Blocks.SPRUCE_SAPLING, ModBlocks.IRON_SPRUCE_SAPLING,
            Blocks.DARK_OAK_SAPLING, ModBlocks.IRON_DARK_OAK_SAPLING
    ));
    public static final Item IRON_SHRED = new Item(new FabricItemSettings());

    private static final Item IRON_ACACIA_SAPLING = new BlockItem(ModBlocks.IRON_ACACIA_SAPLING, new FabricItemSettings());
    private static final Item COPPER_ACACIA_SAPLING = new BlockItem(ModBlocks.COPPER_ACACIA_SAPLING, new FabricItemSettings());
    private static final Item GOLD_ACACIA_SAPLING = new BlockItem(ModBlocks.GOLD_ACACIA_SAPLING, new FabricItemSettings());

    private static final Item IRON_SPRUCE_SAPLING = new BlockItem(ModBlocks.IRON_SPRUCE_SAPLING, new FabricItemSettings());
    private static final Item COPPER_SPRUCE_SAPLING = new BlockItem(ModBlocks.COPPER_SPRUCE_SAPLING, new FabricItemSettings());
    private static final Item GOLD_SPRUCE_SAPLING = new BlockItem(ModBlocks.GOLD_SPRUCE_SAPLING, new FabricItemSettings());

    private static final Item IRON_JUNGLE_SAPLING = new BlockItem(ModBlocks.IRON_JUNGLE_SAPLING, new FabricItemSettings());
    private static final Item COPPER_JUNGLE_SAPLING = new BlockItem(ModBlocks.COPPER_JUNGLE_SAPLING, new FabricItemSettings());
    private static final Item GOLD_JUNGLE_SAPLING = new BlockItem(ModBlocks.GOLD_JUNGLE_SAPLING, new FabricItemSettings());

    private static final Item IRON_BIRCH_SAPLING = new BlockItem(ModBlocks.IRON_BIRCH_SAPLING, new FabricItemSettings());
    private static final Item COPPER_BIRCH_SAPLING = new BlockItem(ModBlocks.COPPER_BIRCH_SAPLING, new FabricItemSettings());
    private static final Item GOLD_BIRCH_SAPLING = new BlockItem(ModBlocks.GOLD_BIRCH_SAPLING, new FabricItemSettings());

    private static final Item IRON_DARK_OAK_SAPLING = new BlockItem(ModBlocks.IRON_DARK_OAK_SAPLING, new FabricItemSettings());
    private static final Item COPPER_DARK_OAK_SAPLING = new BlockItem(ModBlocks.COPPER_DARK_OAK_SAPLING, new FabricItemSettings());
    private static final Item GOLD_DARK_OAK_SAPLING = new BlockItem(ModBlocks.GOLD_DARK_OAK_SAPLING, new FabricItemSettings());


    public static void onInitialize() {
        Registry.register(Registries.ITEM_GROUP, DEFAULT_ITEM_GROUP, FabricItemGroup.builder()
                .icon(() -> new ItemStack(Items.DIAMOND_PICKAXE))
                .displayName(Text.translatable("itemGroup.iron_oak.iron_oak"))
                .build()); // build() no longer registers by itself

        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "fire_bowl"), new BlockItem(ModBlocks.FIRE_BOWL, new FabricItemSettings().maxCount(1)));

        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "copper_bone_meal"), COPPER_BONE_MEAL);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "copper_ash"), COPPER_ASH);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "copper_shred"), COPPER_SHRED);

        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "gold_bone_meal"), GOLD_BONE_MEAL);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "gold_ash"), GOLD_ASH);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "gold_shred"), GOLD_SHRED);

        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "iron_bone_meal"), IRON_BONE_MEAL);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "iron_ash"), IRON_ASH);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "iron_shred"), IRON_SHRED);

        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "copper_oak_sapling"), COPPER_OAK_SAPLING);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "copper_oak_log"), new BlockItem(ModBlocks.COPPER_OAK_LOG, new FabricItemSettings()));
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "gold_oak_sapling"), GOLD_OAK_SAPLING);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "gold_oak_log"), new BlockItem(ModBlocks.GOLD_OAK_LOG, new FabricItemSettings()));
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "iron_oak_sapling"), IRON_OAK_SAPLING);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "iron_oak_log"), new BlockItem(ModBlocks.IRON_OAK_LOG, new FabricItemSettings()));

        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "copper_acacia_sapling"), COPPER_ACACIA_SAPLING);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "copper_acacia_log"), new BlockItem(ModBlocks.COPPER_ACACIA_LOG, new FabricItemSettings()));
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "gold_acacia_sapling"), GOLD_ACACIA_SAPLING);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "gold_acacia_log"), new BlockItem(ModBlocks.GOLD_ACACIA_LOG, new FabricItemSettings()));
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "iron_acacia_sapling"), IRON_ACACIA_SAPLING);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "iron_acacia_log"), new BlockItem(ModBlocks.IRON_ACACIA_LOG, new FabricItemSettings()));

        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "copper_spruce_sapling"), COPPER_SPRUCE_SAPLING);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "copper_spruce_log"), new BlockItem(ModBlocks.COPPER_SPRUCE_LOG, new FabricItemSettings()));
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "gold_spruce_sapling"), GOLD_SPRUCE_SAPLING);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "gold_spruce_log"), new BlockItem(ModBlocks.GOLD_SPRUCE_LOG, new FabricItemSettings()));
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "iron_spruce_sapling"), IRON_SPRUCE_SAPLING);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "iron_spruce_log"), new BlockItem(ModBlocks.IRON_SPRUCE_LOG, new FabricItemSettings()));

        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "copper_jungle_sapling"), COPPER_JUNGLE_SAPLING);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "copper_jungle_log"), new BlockItem(ModBlocks.COPPER_JUNGLE_LOG, new FabricItemSettings()));
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "gold_jungle_sapling"), GOLD_JUNGLE_SAPLING);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "gold_jungle_log"), new BlockItem(ModBlocks.GOLD_JUNGLE_LOG, new FabricItemSettings()));
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "iron_jungle_sapling"), IRON_JUNGLE_SAPLING);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "iron_jungle_log"), new BlockItem(ModBlocks.IRON_JUNGLE_LOG, new FabricItemSettings()));

        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "copper_birch_sapling"), COPPER_BIRCH_SAPLING);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "copper_birch_log"), new BlockItem(ModBlocks.COPPER_BIRCH_LOG, new FabricItemSettings()));
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "gold_birch_sapling"), GOLD_BIRCH_SAPLING);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "gold_birch_log"), new BlockItem(ModBlocks.GOLD_BIRCH_LOG, new FabricItemSettings()));
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "iron_birch_sapling"), IRON_BIRCH_SAPLING);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "iron_birch_log"), new BlockItem(ModBlocks.IRON_BIRCH_LOG, new FabricItemSettings()));

        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "copper_dark_oak_sapling"), COPPER_DARK_OAK_SAPLING);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "copper_dark_oak_log"), new BlockItem(ModBlocks.COPPER_DARK_OAK_LOG, new FabricItemSettings()));
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "gold_dark_oak_sapling"), GOLD_DARK_OAK_SAPLING);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "gold_dark_oak_log"), new BlockItem(ModBlocks.GOLD_DARK_OAK_LOG, new FabricItemSettings()));
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "iron_dark_oak_sapling"), IRON_DARK_OAK_SAPLING);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "iron_dark_oak_log"), new BlockItem(ModBlocks.IRON_DARK_OAK_LOG, new FabricItemSettings()));

        // Add all items to the default item group
        ItemGroupEvents.modifyEntriesEvent(DEFAULT_ITEM_GROUP).register(content -> {
            var allItemIds = Registries.ITEM.getIds().stream().filter(id -> id.getNamespace().equals(MOD_ID)).toList();
            for (Identifier id : allItemIds) {
                content.add(Registries.ITEM.get(id));
            }
        });
    }
}
