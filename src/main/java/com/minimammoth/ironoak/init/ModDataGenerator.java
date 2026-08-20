package com.minimammoth.ironoak.init;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

import static com.minimammoth.ironoak.IronOak.LOGGER;

public class ModDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        LOGGER.info("Init data generators");
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ModWorldGenerator::new);
        // Emits assets/iron_oak/items/*.json only. Requires the datagen run to inherit
        // `client`: Fabric routes FabricModelProvider through a client-only mixin, so on
        // a server-side datagen run this provider is never called. See build.gradle.
        pack.addProvider(ModModelGenerator::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        LOGGER.info("Build registries");
        registryBuilder.add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap);
    }
}
