package com.minimammoth.ironoak.requirements;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The requirement this test proves, by its id in {@code docs/requirements/} — for example
 * {@code @Requirement("MAT-02")}.
 *
 * <p>Before this annotation existed a test named the <em>issue</em> it came from, in prose, in
 * its javadoc. That is still worth doing and still asked for by
 * {@code docs/strategy/testing.md}: an issue says why the test was written, and a test whose
 * reason is recorded survives a refactor that would otherwise delete it as pointless. But an
 * issue is a moment and a requirement is a contract, and only the contract can answer the
 * question the prose never could — <em>which requirement has nothing proving it?</em>
 * {@code RequirementTracingTest} answers it by reading these annotations back.
 *
 * <p>Three consequences of how it is declared, all deliberate:
 *
 * <ul>
 *   <li>{@link RetentionPolicy#SOURCE}. Nothing reads it reflectively — the tracing test reads
 *       the source text, because a reflective reader would need the gametest classes on the
 *       layer-1 classpath and the annotation on the gametest classpath, which is a cycle. So
 *       the annotation is erased at compile time and cannot reach a runtime classpath or an
 *       artefact.
 *   <li>It lives in its own {@code testsupport} source set, which depends on nothing. Layer 1
 *       is {@code src/test} and layer 2 is {@code src/gametest} — two source sets that share
 *       no classpath, and this has to compile in both.
 *   <li>{@link Repeatable}. A test that proves two requirements says so twice rather than
 *       picking the closer one.
 * </ul>
 *
 * <p>Not every test has a requirement, and that is not a gap to be filled with the nearest
 * plausible id. {@code ImplementedInventoryTest} pins the defaults of an internal helper that
 * no requirement describes; a false citation there would be worse than none, because the
 * tracing test would then report a requirement as proven when nothing proves it.
 *
 * @see <a href="../../../../../../../docs/requirements/README.md">docs/requirements/README.md</a>
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(Requirement.Requirements.class)
public @interface Requirement {

    /** A requirement id, {@code <DOMAIN>-<NN>}, as it appears in {@code docs/requirements/}. */
    String value();

    /** Container for repeated {@link Requirement} citations. Never written by hand. */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @interface Requirements {
        Requirement[] value();
    }
}
