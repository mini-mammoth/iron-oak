package com.minimammoth.ironoak.init;

import com.minimammoth.ironoak.BootstrappedGame;
import com.minimammoth.ironoak.Resources;
import com.minimammoth.ironoak.requirements.Requirement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.minimammoth.ironoak.IronOak.MOD_ID;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every registered id must have the files that id needs. This is the #26 guard, and the
 * cheapest test in the repo.
 *
 * <p>In #26 every item and block rendered as the missing-texture cube and the creative tab
 * was gone, because the mod shipped only the models. On 1.21.4+ that bug route runs through
 * an indirection layer between an item and its model ({@code assets/iron_oak/items/&lt;id&gt;.json})
 * that does not exist on this version — 1.21.1 reads
 * {@code assets/iron_oak/models/item/&lt;id&gt;.json} directly, so that is the file this test
 * checks for instead.
 *
 * <p>The list of ids is taken from the registries, never hand-written: the 6x3 matrix grows,
 * and a hand-written list silently stops covering the newest arm. {@code ModItems.onInitialize}
 * walks the registry the same way.
 */
@ExtendWith(BootstrappedGame.class)
class RegistryAssetsTest {

    static List<ResourceLocation> items() {
        return modIds(BuiltInRegistries.ITEM.keySet());
    }

    static List<ResourceLocation> blocks() {
        return modIds(BuiltInRegistries.BLOCK.keySet());
    }

    @Requirement("MAT-02")
    @ParameterizedTest(name = "{0}")
    @MethodSource("items")
    void itemHasAModel(ResourceLocation id) {
        assertTrue(Resources.exists("assets/" + MOD_ID + "/models/item/" + id.getPath() + ".json"),
                () -> "no model for " + id);
    }

    @Requirement("MAT-02")
    @ParameterizedTest(name = "{0}")
    @MethodSource("blocks")
    void blockHasABlockstate(ResourceLocation id) {
        assertTrue(Resources.exists("assets/" + MOD_ID + "/blockstates/" + id.getPath() + ".json"),
                () -> "no blockstate for " + id);
    }

    @Requirement("MAT-02")
    @Requirement("TRE-06")
    @ParameterizedTest(name = "{0}")
    @MethodSource("blocks")
    void blockHasTheLootTableItDeclares(ResourceLocation id) {
        Block block = BuiltInRegistries.BLOCK.get(id);
        // Every one of these blocks is expected to drop something; ofLegacyCopy does not
        // copy the source block's loot table, which is why the mod has its own. On this
        // version getLootTable() always returns a key — falling back to a computed default
        // rather than signalling "none" — so there is nothing to unwrap.
        ResourceKey<LootTable> lootTable = block.getLootTable();
        String path = "data/" + lootTable.location().getNamespace()
                + "/loot_table/" + lootTable.location().getPath() + ".json";
        assertTrue(Resources.exists(path), () -> id + " declares a loot table with no file: " + path);
    }

    /**
     * The translation <em>key</em>, not the English text — {@code de_de} is a legitimate
     * thing to add and this test must not stand in its way. On this version a block item's
     * description id always defers to its block, so asking the item for its own description
     * id is what catches a block whose lang entry went missing.
     */
    @Requirement("MAT-05")
    @Test
    void everyRegisteredThingHasAnEnglishName() {
        Map<String, String> lang = Resources.jsonOrFail("assets/" + MOD_ID + "/lang/en_us.json")
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAsString()));

        for (ResourceLocation id : items()) {
            Item item = BuiltInRegistries.ITEM.get(id);
            assertTrue(lang.containsKey(item.getDescriptionId()),
                    () -> "en_us.json has no entry for " + item.getDescriptionId() + " (" + id + ")");
        }

        for (ResourceLocation id : blocks()) {
            Block block = BuiltInRegistries.BLOCK.get(id);
            assertTrue(lang.containsKey(block.getDescriptionId()),
                    () -> "en_us.json has no entry for " + block.getDescriptionId() + " (" + id + ")");
        }
    }

    /**
     * The creative tab is the other half of #26 — it went missing along with the textures,
     * and it is what a player notices first.
     */
    @Requirement("MAT-02")
    @Test
    void theCreativeTabIsRegistered() {
        assertTrue(BuiltInRegistries.CREATIVE_MODE_TAB.containsKey(ModItems.DEFAULT_ITEM_GROUP),
                "the mod's creative tab is not registered");
    }

    private static List<ResourceLocation> modIds(Set<ResourceLocation> keys) {
        return keys.stream()
                .filter(id -> id.getNamespace().equals(MOD_ID))
                .sorted(java.util.Comparator.comparing(ResourceLocation::getPath))
                .toList();
    }
}
