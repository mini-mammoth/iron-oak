package com.minimammoth.ironoak;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Binds the mod's own tags from the committed JSON, so tests can decode a tag ingredient
 * without a running server.
 *
 * <p>Nothing loads a data pack at layer 1, so every tag is unbound and touching one throws
 * {@code "Tags not bound"} — which is what a burning recipe's {@code #iron_oak:*_infused_logs}
 * ingredient does the moment it decodes. Binding is not a workaround for that: resolving
 * every entry against the registry is the step that proves those files name items which
 * actually exist, and a JSON-shape check cannot see it.
 *
 * <p>Only the mod's three log tags are bound. Vanilla's stay unbound, so a test that needs
 * one has to say so rather than inherit it by accident.
 */
public final class TagBinding {
    private static boolean bound;

    private TagBinding() {
    }

    public static synchronized void bindInfusedLogTags() {
        if (bound) {
            return;
        }

        bind(BuiltInRegistries.ITEM, Registries.ITEM, "item");
        bind(BuiltInRegistries.BLOCK, Registries.BLOCK, "block");
        bound = true;
    }

    /**
     * Ops that decode against the built-in registries and can see the tags bound above.
     *
     * <p>The obvious {@code RegistryOps.create(ops, registryAccess)} cannot: a registry's
     * public tag lookup reads a tag set that is only populated when the registry freezes,
     * and these never do. {@code createRegistrationLookup} is the lookup vanilla itself uses
     * while a registry is still open, and it hands back the very {@code HolderSet} that
     * {@code bindTag} filled.
     */
    public static RegistryOps<JsonElement> ops() {
        return RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE, new RegistryOps.RegistryInfoLookup() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> key) {
                return BuiltInRegistries.REGISTRY.getOptional(key.location())
                        .map(registry -> (WritableRegistry<T>) registry)
                        .map(registry -> new RegistryOps.RegistryInfo<>(
                                registry.holderOwner(), registry.createRegistrationLookup(), Lifecycle.stable()));
            }
        });
    }

    private static <T> void bind(Registry<T> registry, ResourceKey<? extends Registry<T>> registryKey, String directory) {
        for (String metal : Matrix.METALS) {
            String name = metal + "_infused_logs";
            JsonObject json = Resources.jsonOrFail("data/iron_oak/tags/" + directory + "/" + name + ".json");

            List<Holder<T>> entries = new ArrayList<>();
            for (JsonElement value : json.getAsJsonArray("values")) {
                ResourceLocation id = ResourceLocation.parse(value.getAsString());
                entries.add(registry.getHolder(id).orElseGet(() ->
                        fail("tag iron_oak:" + name + " (" + directory + ") lists " + id + ", which is not registered")));
            }

            TagKey<T> tag = TagKey.create(registryKey, ResourceLocation.fromNamespaceAndPath("iron_oak", name));
            ((WritableRegistry<T>) registry).bindTags(Map.of(tag, entries));
        }
    }
}
