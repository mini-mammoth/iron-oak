package com.minimammoth.ironoak.init;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import static com.minimammoth.ironoak.IronOak.MOD_ID;

/**
 * Emits the {@code assets/iron_oak/items/<id>.json} client item definitions.
 *
 * <p>1.21.4 put an indirection layer between an item and its model: the game no longer
 * looks up {@code models/item/<id>.json} by name, it reads {@code items/<id>.json} and
 * follows the model reference in there. A mod that ships only the models resolves nothing
 * and every stack renders as the missing-texture cube — with no build failure and nothing
 * in the log, because a missing item definition is not an error, it is an absent file.
 *
 * <h2>Why this provider only emits the {@code items/} layer</h2>
 *
 * {@code assets/iron_oak/models/} and {@code assets/iron_oak/blockstates/} are
 * hand-written and stay authoritative. This provider deliberately adds the one layer that
 * was missing rather than taking the models over as well, because:
 *
 * <ul>
 *   <li>the fire bowl's blockstate is a hand-authored {@code multipart} that layers six
 *       vanilla fire models at four rotations — {@link BlockModelGenerators} has no
 *       template that reproduces it;</li>
 *   <li>the log blockstates rotate a separate {@code _horizontal} model per axis, and the
 *       ore-infused logs mix a vanilla top texture with a mod side texture;</li>
 *   <li>nobody can verify a regenerated model without launching the game, and this fix has
 *       to be reviewable without that.</li>
 * </ul>
 *
 * So there is exactly one owner per file: {@code items/} is generated here,
 * {@code models/} and {@code blockstates/} are hand-written. Nothing is emitted twice.
 *
 * <h2>Why it walks the registry instead of listing the items</h2>
 *
 * The 6×3 matrix is all-or-nothing — 18 logs, 18 saplings, the fire bowl and 9 items — and
 * a hand-written list is one more place to forget an arm when a metal or a wood type is
 * added. Walking {@link BuiltInRegistries#ITEM} for the mod's namespace is complete by
 * construction and is the same shape {@code ModItems.onInitialize()} already uses to fill
 * the creative tab.
 *
 * <p>Every item resolves to {@code iron_oak:item/<id>}, which is the invariant the
 * hand-written models already satisfy: block items parent their block model there, plain
 * items are {@code item/generated} with their texture.
 */
public class ModModelGenerator extends FabricModelProvider {
    public ModModelGenerator(FabricDataOutput output) {
        super(output);
    }

    /**
     * Intentionally empty — see the class comment. Emitting anything here would collide
     * with the hand-written {@code blockstates/} and {@code models/block/}.
     */
    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        BuiltInRegistries.ITEM.entrySet().stream()
                .filter(entry -> entry.getKey().identifier().getNamespace().equals(MOD_ID))
                .forEach(entry -> {
                    Identifier id = entry.getKey().identifier();
                    Identifier model = Identifier.fromNamespaceAndPath(MOD_ID, "item/" + id.getPath());
                    itemModelGenerator.itemModelOutput.accept(entry.getValue(), ItemModelUtils.plainModel(model));
                });
    }
}
