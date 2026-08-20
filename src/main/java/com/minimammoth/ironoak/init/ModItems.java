package com.minimammoth.ironoak.init;

import com.minimammoth.ironoak.OreInfusedAsh;
import com.minimammoth.ironoak.OreInfusedBoneMeal;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Map;
import java.util.function.Function;

import static com.minimammoth.ironoak.IronOak.MOD_ID;

/**
 * Since 1.21.2 an item's settings must carry its own registry key, so construction and
 * registration happen together in {@link #register}.
 */
public class ModItems {
    private ModItems() {
    }

    public static final ResourceKey<CreativeModeTab> DEFAULT_ITEM_GROUP =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, "iron_oak"));

    public static final Item COPPER_ASH = register("copper_ash", OreInfusedAsh::new);
    public static final Item COPPER_SHRED = register("copper_shred", Item::new);
    public static final Item COPPER_BONE_MEAL = register("copper_bone_meal", settings -> new OreInfusedBoneMeal(settings, Map.of(
            Blocks.OAK_SAPLING, ModBlocks.COPPER_OAK_SAPLING,
            Blocks.ACACIA_SAPLING, ModBlocks.COPPER_ACACIA_SAPLING,
            Blocks.BIRCH_SAPLING, ModBlocks.COPPER_BIRCH_SAPLING,
            Blocks.JUNGLE_SAPLING, ModBlocks.COPPER_JUNGLE_SAPLING,
            Blocks.SPRUCE_SAPLING, ModBlocks.COPPER_SPRUCE_SAPLING,
            Blocks.DARK_OAK_SAPLING, ModBlocks.COPPER_DARK_OAK_SAPLING
    )));

    public static final Item GOLD_ASH = register("gold_ash", OreInfusedAsh::new);
    public static final Item GOLD_SHRED = register("gold_shred", Item::new);
    public static final Item GOLD_BONE_MEAL = register("gold_bone_meal", settings -> new OreInfusedBoneMeal(settings, Map.of(
            Blocks.OAK_SAPLING, ModBlocks.GOLD_OAK_SAPLING,
            Blocks.ACACIA_SAPLING, ModBlocks.GOLD_ACACIA_SAPLING,
            Blocks.BIRCH_SAPLING, ModBlocks.GOLD_BIRCH_SAPLING,
            Blocks.JUNGLE_SAPLING, ModBlocks.GOLD_JUNGLE_SAPLING,
            Blocks.SPRUCE_SAPLING, ModBlocks.GOLD_SPRUCE_SAPLING,
            Blocks.DARK_OAK_SAPLING, ModBlocks.GOLD_DARK_OAK_SAPLING
    )));

    public static final Item IRON_ASH = register("iron_ash", OreInfusedAsh::new);
    public static final Item IRON_SHRED = register("iron_shred", Item::new);
    public static final Item IRON_BONE_MEAL = register("iron_bone_meal", settings -> new OreInfusedBoneMeal(settings, Map.of(
            Blocks.OAK_SAPLING, ModBlocks.IRON_OAK_SAPLING,
            Blocks.ACACIA_SAPLING, ModBlocks.IRON_ACACIA_SAPLING,
            Blocks.BIRCH_SAPLING, ModBlocks.IRON_BIRCH_SAPLING,
            Blocks.JUNGLE_SAPLING, ModBlocks.IRON_JUNGLE_SAPLING,
            Blocks.SPRUCE_SAPLING, ModBlocks.IRON_SPRUCE_SAPLING,
            Blocks.DARK_OAK_SAPLING, ModBlocks.IRON_DARK_OAK_SAPLING
    )));

    // The fire bowl is a block you place, so it does not stack.
    public static final Item FIRE_BOWL = registerBlockItem("fire_bowl", ModBlocks.FIRE_BOWL, new Item.Properties().stacksTo(1));

    public static final Item COPPER_OAK_LOG = registerBlockItem("copper_oak_log", ModBlocks.COPPER_OAK_LOG);
    public static final Item COPPER_OAK_SAPLING = registerBlockItem("copper_oak_sapling", ModBlocks.COPPER_OAK_SAPLING);
    public static final Item GOLD_OAK_LOG = registerBlockItem("gold_oak_log", ModBlocks.GOLD_OAK_LOG);
    public static final Item GOLD_OAK_SAPLING = registerBlockItem("gold_oak_sapling", ModBlocks.GOLD_OAK_SAPLING);
    public static final Item IRON_OAK_LOG = registerBlockItem("iron_oak_log", ModBlocks.IRON_OAK_LOG);
    public static final Item IRON_OAK_SAPLING = registerBlockItem("iron_oak_sapling", ModBlocks.IRON_OAK_SAPLING);

    public static final Item COPPER_ACACIA_LOG = registerBlockItem("copper_acacia_log", ModBlocks.COPPER_ACACIA_LOG);
    public static final Item COPPER_ACACIA_SAPLING = registerBlockItem("copper_acacia_sapling", ModBlocks.COPPER_ACACIA_SAPLING);
    public static final Item GOLD_ACACIA_LOG = registerBlockItem("gold_acacia_log", ModBlocks.GOLD_ACACIA_LOG);
    public static final Item GOLD_ACACIA_SAPLING = registerBlockItem("gold_acacia_sapling", ModBlocks.GOLD_ACACIA_SAPLING);
    public static final Item IRON_ACACIA_LOG = registerBlockItem("iron_acacia_log", ModBlocks.IRON_ACACIA_LOG);
    public static final Item IRON_ACACIA_SAPLING = registerBlockItem("iron_acacia_sapling", ModBlocks.IRON_ACACIA_SAPLING);

    public static final Item COPPER_SPRUCE_LOG = registerBlockItem("copper_spruce_log", ModBlocks.COPPER_SPRUCE_LOG);
    public static final Item COPPER_SPRUCE_SAPLING = registerBlockItem("copper_spruce_sapling", ModBlocks.COPPER_SPRUCE_SAPLING);
    public static final Item GOLD_SPRUCE_LOG = registerBlockItem("gold_spruce_log", ModBlocks.GOLD_SPRUCE_LOG);
    public static final Item GOLD_SPRUCE_SAPLING = registerBlockItem("gold_spruce_sapling", ModBlocks.GOLD_SPRUCE_SAPLING);
    public static final Item IRON_SPRUCE_LOG = registerBlockItem("iron_spruce_log", ModBlocks.IRON_SPRUCE_LOG);
    public static final Item IRON_SPRUCE_SAPLING = registerBlockItem("iron_spruce_sapling", ModBlocks.IRON_SPRUCE_SAPLING);

    public static final Item COPPER_JUNGLE_LOG = registerBlockItem("copper_jungle_log", ModBlocks.COPPER_JUNGLE_LOG);
    public static final Item COPPER_JUNGLE_SAPLING = registerBlockItem("copper_jungle_sapling", ModBlocks.COPPER_JUNGLE_SAPLING);
    public static final Item GOLD_JUNGLE_LOG = registerBlockItem("gold_jungle_log", ModBlocks.GOLD_JUNGLE_LOG);
    public static final Item GOLD_JUNGLE_SAPLING = registerBlockItem("gold_jungle_sapling", ModBlocks.GOLD_JUNGLE_SAPLING);
    public static final Item IRON_JUNGLE_LOG = registerBlockItem("iron_jungle_log", ModBlocks.IRON_JUNGLE_LOG);
    public static final Item IRON_JUNGLE_SAPLING = registerBlockItem("iron_jungle_sapling", ModBlocks.IRON_JUNGLE_SAPLING);

    public static final Item COPPER_BIRCH_LOG = registerBlockItem("copper_birch_log", ModBlocks.COPPER_BIRCH_LOG);
    public static final Item COPPER_BIRCH_SAPLING = registerBlockItem("copper_birch_sapling", ModBlocks.COPPER_BIRCH_SAPLING);
    public static final Item GOLD_BIRCH_LOG = registerBlockItem("gold_birch_log", ModBlocks.GOLD_BIRCH_LOG);
    public static final Item GOLD_BIRCH_SAPLING = registerBlockItem("gold_birch_sapling", ModBlocks.GOLD_BIRCH_SAPLING);
    public static final Item IRON_BIRCH_LOG = registerBlockItem("iron_birch_log", ModBlocks.IRON_BIRCH_LOG);
    public static final Item IRON_BIRCH_SAPLING = registerBlockItem("iron_birch_sapling", ModBlocks.IRON_BIRCH_SAPLING);

    public static final Item COPPER_DARK_OAK_LOG = registerBlockItem("copper_dark_oak_log", ModBlocks.COPPER_DARK_OAK_LOG);
    public static final Item COPPER_DARK_OAK_SAPLING = registerBlockItem("copper_dark_oak_sapling", ModBlocks.COPPER_DARK_OAK_SAPLING);
    public static final Item GOLD_DARK_OAK_LOG = registerBlockItem("gold_dark_oak_log", ModBlocks.GOLD_DARK_OAK_LOG);
    public static final Item GOLD_DARK_OAK_SAPLING = registerBlockItem("gold_dark_oak_sapling", ModBlocks.GOLD_DARK_OAK_SAPLING);
    public static final Item IRON_DARK_OAK_LOG = registerBlockItem("iron_dark_oak_log", ModBlocks.IRON_DARK_OAK_LOG);
    public static final Item IRON_DARK_OAK_SAPLING = registerBlockItem("iron_dark_oak_sapling", ModBlocks.IRON_DARK_OAK_SAPLING);

    private static Item register(String name, Function<Item.Properties, Item> factory) {
        return register(name, factory, new Item.Properties());
    }

    private static Item registerBlockItem(String name, Block block) {
        return registerBlockItem(name, block, new Item.Properties());
    }

    /**
     * {@code useBlockDescriptionPrefix} keeps block items on their {@code block.iron_oak.*}
     * translation keys. Without it they look for {@code item.iron_oak.*}, which the lang
     * file does not define for blocks — every block item would show a raw key.
     */
    private static Item registerBlockItem(String name, Block block, Item.Properties settings) {
        return register(name, s -> new BlockItem(block, s), settings.useBlockDescriptionPrefix());
    }

    private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(settings.setId(key)));
    }

    public static void onInitialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, DEFAULT_ITEM_GROUP, FabricItemGroup.builder()
                .icon(() -> new ItemStack(Items.DIAMOND_PICKAXE))
                .title(Component.translatable("itemGroup.iron_oak.iron_oak"))
                .build());

        // Everything this mod registers goes into the mod's own tab.
        ItemGroupEvents.modifyEntriesEvent(DEFAULT_ITEM_GROUP).register(content -> BuiltInRegistries.ITEM.entrySet().stream()
                .filter(entry -> entry.getKey().identifier().getNamespace().equals(MOD_ID))
                .forEach(entry -> content.accept(entry.getValue())));
    }
}
