package com.minimammoth.ironoak;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * #81: datagen's own bookkeeping must not ship in the mod jar.
 *
 * <p>{@code src/main/generated} is a resource source root, so everything datagen leaves in it
 * is packaged unless excluded — including {@code .cache/}, a hash list that is meaningless
 * to players. {@code .gitignore} keeps it out of git only; it says nothing to
 * {@code processResources}. No requirement in {@code docs/requirements/} covers packaging,
 * so this test cites none.
 *
 * <p>Reads through the classpath, like {@link Resources}, so it sees what
 * {@code processResources} produced. It needs {@code runDatagen} to have run to be able to
 * fail; with no {@code .cache} on disk it passes vacuously.
 */
class ShippedResourcesTest {
    @Test
    void datagenCacheIsNotPackaged() {
        assertFalse(Resources.exists(".cache"),
                "datagen's .cache/ is on the resource classpath and would ship in the jar");
    }
}
