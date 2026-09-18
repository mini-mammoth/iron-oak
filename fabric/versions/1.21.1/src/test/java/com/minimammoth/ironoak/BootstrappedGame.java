package com.minimammoth.ironoak;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Boots vanilla and then this mod, once per JVM.
 *
 * <p>Layer 1 of {@code docs/strategy/testing.md} is <em>no world</em>, not <em>no
 * Minecraft</em>: {@code Blocks.OAK_SAPLING}, {@code BuiltInRegistries.ITEM} and every
 * {@code ItemStack} need a bootstrapped game. {@code fabric-loader-junit} has already
 * booted Knot by the time JUnit gets here — mixins are applied and the mod's classes are
 * loadable — but nothing has called Minecraft's own bootstrap and nothing has invoked the
 * mod's entrypoint, because no game was started.
 *
 * <p>The mod is initialised through {@link IronOak#onInitialize()} rather than by poking
 * the {@code Mod*} classes one at a time, so the tests see the same registration order the
 * game does.
 *
 * <p>Use it with {@code @ExtendWith(BootstrappedGame.class)}.
 */
public class BootstrappedGame implements BeforeAllCallback {
    private static boolean booted;

    @Override
    public void beforeAll(ExtensionContext context) {
        ensure();
    }

    public static synchronized void ensure() {
        if (booted) {
            return;
        }

        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        new IronOak().onInitialize();
        booted = true;
    }
}
