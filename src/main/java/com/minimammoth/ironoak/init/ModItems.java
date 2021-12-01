package com.minimammoth.ironoak.init;

import com.minimammoth.ironoak.IronAsh;
import com.minimammoth.ironoak.IronBoneMeal;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
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

    public static final Item IRON_ASH = new IronAsh(new FabricItemSettings().group(DEFAULT_ITEM_GROUP));
    public static final Item IRON_SHRED = new Item(new FabricItemSettings().group(DEFAULT_ITEM_GROUP));

    public static void onInitialize() {
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "fire_bowl"), new BlockItem(ModBlocks.FIRE_BOWL, new FabricItemSettings().maxCount(1).group(DEFAULT_ITEM_GROUP)));
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "iron_bone_meal"), new IronBoneMeal(new FabricItemSettings().group(DEFAULT_ITEM_GROUP)));
        IRON_OAK_SAPLING = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "iron_oak_sapling"), new BlockItem(ModBlocks.IRON_OAK_SAPLING, new FabricItemSettings().group(DEFAULT_ITEM_GROUP)));
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "iron_oak_log"), new BlockItem(ModBlocks.IRON_OAK_LOG, new FabricItemSettings().group(DEFAULT_ITEM_GROUP)));
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "iron_ash"), IRON_ASH);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "iron_shred"), IRON_SHRED);
    }
}
