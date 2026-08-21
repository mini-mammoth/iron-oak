package com.minimammoth.ironoak.init;

import com.google.gson.JsonObject;
import com.minimammoth.ironoak.BootstrappedGame;
import com.minimammoth.ironoak.Resources;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.minimammoth.ironoak.IronOak.MOD_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every registered id must have the files that id needs. This is the #26 guard, and the
 * cheapest test in the repo.
 *
 * <p>In #26 every item and block rendered as the missing-texture cube and the creative tab
 * was gone. 1.21.4 had put an indirection layer between an item and its model — the game
 * reads {@code assets/iron_oak/items/&lt;id&gt;.json} and follows the reference in there — and
 * the mod shipped only the models. A missing item definition is not an error, it is an
 * absent file: nothing failed, nothing logged, CI stayed green, and 15 magenta cubes in a
 * creative search were precisely the mod's 15 {@code iron_*} entries.
 *
 * <p>The list of ids is taken from the registries, never hand-written: the 6x3 matrix grows,
 * and a hand-written list silently stops covering the newest arm. {@code ModItems.onInitialize}
 * and {@code ModModelGenerator.generateItemModels} both walk the registry the same way.
 */
@ExtendWith(BootstrappedGame.class)
class RegistryAssetsTest {

    static List<Identifier> items() {
        return modIds(BuiltInRegistries.ITEM.keySet());
    }

    static List<Identifier> blocks() {
        return modIds(BuiltInRegistries.BLOCK.keySet());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("items")
    void itemHasAClientItemDefinition(Identifier id) {
        // The layer #26 was missing entirely. ModModelGenerator emits it by walking the
        // registry, so this also guards the invariant that walk assumes.
        JsonObject definition = Resources.jsonOrFail("assets/" + MOD_ID + "/items/" + id.getPath() + ".json");
        assertEquals(MOD_ID + ":item/" + id.getPath(),
                definition.getAsJsonObject("model").get("model").getAsString(),
                () -> id + " points its client item definition at another item's model");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("items")
    void itemHasTheModelItsDefinitionPointsAt(Identifier id) {
        assertTrue(Resources.exists("assets/" + MOD_ID + "/models/item/" + id.getPath() + ".json"),
                () -> "no model for " + id);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("blocks")
    void blockHasABlockstate(Identifier id) {
        assertTrue(Resources.exists("assets/" + MOD_ID + "/blockstates/" + id.getPath() + ".json"),
                () -> "no blockstate for " + id);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("blocks")
    void blockHasTheLootTableItDeclares(Identifier id) {
        Block block = BuiltInRegistries.BLOCK.getValue(id);
        // Every one of these blocks is expected to drop something; ofLegacyCopy does not
        // copy the source block's loot table, which is why the mod has its own.
        var lootTable = block.getLootTable().orElseThrow(() -> new AssertionError(id + " declares no loot table"));
        String path = "data/" + lootTable.identifier().getNamespace()
                + "/loot_table/" + lootTable.identifier().getPath() + ".json";
        assertTrue(Resources.exists(path), () -> id + " declares a loot table with no file: " + path);
    }

    /**
     * The translation <em>key</em>, not the English text — {@code de_de} is a legitimate
     * thing to add and this test must not stand in its way. A block item keeps its block's
     * key via {@code useBlockDescriptionPrefix}, so asking the item for its own description
     * id is what catches a block item that lost that call and now shows a raw key.
     */
    @Test
    void everyRegisteredThingHasAnEnglishName() {
        Map<String, String> lang = Resources.jsonOrFail("assets/" + MOD_ID + "/lang/en_us.json")
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAsString()));

        for (Identifier id : items()) {
            Item item = BuiltInRegistries.ITEM.getValue(id);
            assertTrue(lang.containsKey(item.getDescriptionId()),
                    () -> "en_us.json has no entry for " + item.getDescriptionId() + " (" + id + ")");
        }

        for (Identifier id : blocks()) {
            Block block = BuiltInRegistries.BLOCK.getValue(id);
            assertTrue(lang.containsKey(block.getDescriptionId()),
                    () -> "en_us.json has no entry for " + block.getDescriptionId() + " (" + id + ")");
        }
    }

    /**
     * The creative tab is the other half of #26 — it went missing along with the textures,
     * and it is what a player notices first.
     */
    @Test
    void theCreativeTabIsRegistered() {
        assertTrue(BuiltInRegistries.CREATIVE_MODE_TAB.containsKey(ModItems.DEFAULT_ITEM_GROUP),
                "the mod's creative tab is not registered");
    }

    private static List<Identifier> modIds(Set<Identifier> keys) {
        return keys.stream()
                .filter(id -> id.getNamespace().equals(MOD_ID))
                .sorted(java.util.Comparator.comparing(Identifier::getPath))
                .toList();
    }
}
