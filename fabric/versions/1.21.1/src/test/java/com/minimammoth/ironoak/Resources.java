package com.minimammoth.ironoak;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Reads the mod's own resources the way the game does — off the classpath, not off a path
 * under {@code src/}.
 *
 * <p>That matters here. {@code src/main/generated} is wired into the main source set as a
 * resource directory, so what a test sees through the classpath is what {@code processResources}
 * put in the jar. A test reading {@code src/main/resources/...} directly would pass for a
 * file that never ships.
 */
public final class Resources {
    private Resources() {
    }

    /** The resource at {@code path}, or empty if it does not exist. */
    public static Optional<JsonObject> json(String path) {
        try (InputStream in = Resources.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                return Optional.empty();
            }
            JsonElement parsed = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            return Optional.of(parsed.getAsJsonObject());
        } catch (IOException e) {
            return fail("could not read " + path, e);
        }
    }

    /** The resource at {@code path}, failing the test if it is missing. */
    public static JsonObject jsonOrFail(String path) {
        return json(path).orElseGet(() -> fail("missing resource: " + path));
    }

    public static boolean exists(String path) {
        return Resources.class.getClassLoader().getResource(path) != null;
    }
}
