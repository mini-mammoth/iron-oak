package com.minimammoth.ironoak.requirements;

import com.minimammoth.ironoak.requirements.RequirementCatalogue.Citation;
import com.minimammoth.ironoak.requirements.RequirementCatalogue.Headline;
import com.minimammoth.ironoak.requirements.RequirementCatalogue.IndexRow;
import com.minimammoth.ironoak.requirements.RequirementCatalogue.Entry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The link between {@code docs/requirements/} and the tests, checked in both directions.
 *
 * <p>Before this existed, a test named the <em>issue</em> it came from in its javadoc — good
 * prose, and unreadable by anything. Nineteen such mentions across both layers could not
 * answer the one question worth asking of a requirements document: <em>which requirement has
 * nothing proving it?</em>
 *
 * <p>So a requirement naming {@code test} or {@code gametest} in its acceptance criteria is
 * making a claim, and this is where the claim is checked:
 *
 * <ul>
 *   <li>a citation naming an id that does not exist fails — a typo, or a renumbering the
 *       index forbids;
 *   <li>a requirement claiming a layer that nothing at that layer cites fails — deleting the
 *       last test for a requirement cannot pass silently.
 * </ul>
 *
 * <p>What it deliberately does not check is the other direction of the first rule: a test
 * without a citation is fine. {@code ImplementedInventoryTest} pins the defaults of an
 * internal helper that no requirement describes, and the nearest plausible id would be worse
 * than none — it would report a requirement as proven when nothing proves it. There is no
 * coverage percentage here either, for the reasons {@code docs/strategy/testing.md} gives: a
 * requirement with no gate token is not claiming automated coverage, and most of the
 * thirty-six honestly are not.
 *
 * <p>The rest of the class is the catalogue policing itself — the index against the domain
 * files, and the headline tally against the table it summarises. {@code docs/requirements/README.md}
 * states that rule ("they are the same fact written twice; a mismatch is a bug in the docs")
 * and this is the only thing that can enforce it.
 *
 * <p>No {@code @ExtendWith(BootstrappedGame.class)}, unlike every other test class here: this
 * one reads text files and never touches a Minecraft class, so booting the game would only
 * cost time.
 */
class RequirementTracingTest {

    private static final Map<String, Entry> REQUIREMENTS = RequirementCatalogue.requirements();

    static List<Citation> citations() {
        var citations = RequirementCatalogue.citations();
        assertFalse(citations.isEmpty(),
                "no @Requirement citation anywhere — the scanner is looking in the wrong place");
        return citations;
    }

    static List<Entry> requirementsClaimingATestLayer() {
        return REQUIREMENTS.values().stream()
                .filter(requirement -> !requirement.tracedGates().isEmpty())
                .toList();
    }

    static List<Entry> allRequirements() {
        return List.copyOf(REQUIREMENTS.values());
    }

    /**
     * A citation is a link, and a link to nothing is worse than no link: it reads as coverage
     * and is a typo.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("citations")
    void everyCitationNamesARequirementThatExists(Citation citation) {
        assertTrue(REQUIREMENTS.containsKey(citation.id()),
                () -> citation.source() + " cites " + citation.id()
                        + ", which is not in docs/requirements/. Known ids: " + REQUIREMENTS.keySet());
    }

    /**
     * The half that catches a deletion. A requirement whose acceptance criteria say
     * {@code test} claims that {@code ./gradlew build} proves part of it; if the last test
     * citing it is deleted or renamed, the claim silently becomes false. This is what makes a
     * gate token a contract rather than a comment.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("requirementsClaimingATestLayer")
    void everyRequirementClaimingATestLayerIsCitedFromIt(Entry requirement) {
        var byLayer = RequirementCatalogue.citations().stream()
                .filter(citation -> citation.id().equals(requirement.id()))
                .collect(Collectors.groupingBy(Citation::layer));

        for (String gate : requirement.tracedGates()) {
            assertTrue(byLayer.containsKey(gate),
                    () -> requirement.id() + " (" + requirement.file() + ") names `" + gate
                            + "` as a verification gate, but no test in src/" + gate
                            + " cites it. Either add @Requirement(\"" + requirement.id()
                            + "\") to the test that proves it, or drop `" + gate
                            + "` from its acceptance criteria — the gate list is a claim, not a wish.");
        }
    }

    /** A gate nobody defined is a typo that would otherwise disable the check above. */
    @ParameterizedTest(name = "{0}")
    @MethodSource("allRequirements")
    void everyGateIsOneTheIndexDefines(Entry requirement) {
        for (String gate : requirement.gates()) {
            assertTrue(RequirementCatalogue.KNOWN_GATES.contains(gate),
                    () -> requirement.id() + " verifies with `" + gate
                            + "`, which docs/requirements/README.md does not define. Known: "
                            + RequirementCatalogue.KNOWN_GATES);
        }
    }

    /**
     * The index's status matrix and the domain files are the same fact written twice, and
     * {@code docs/requirements/README.md} says so: "a mismatch is a bug in the docs". Nothing
     * but this can enforce it.
     */
    @Test
    void theIndexAgreesWithTheDomainFiles() {
        var index = RequirementCatalogue.indexRows().stream()
                .collect(Collectors.toMap(IndexRow::id, row -> row, (a, b) -> a, LinkedHashMap::new));

        assertEquals(REQUIREMENTS.keySet().stream().sorted().toList(), index.keySet().stream().sorted().toList(),
                "the index's status matrix lists different ids than the domain files declare");

        for (var row : index.values()) {
            Entry requirement = REQUIREMENTS.get(row.id());
            assertEquals(requirement.file(), row.file(),
                    () -> row.id() + " is declared in " + requirement.file() + " but the index links to " + row.file());
            assertEquals(requirement.title(), row.title(),
                    () -> row.id() + " has a different title in the index than in " + requirement.file());
            assertEquals(requirement.status(), row.status(),
                    () -> row.id() + " is `" + requirement.status() + "` in " + requirement.file()
                            + " and `" + row.status() + "` in the index. Both move in the same commit.");
        }
    }

    /**
     * The headline tally above the matrix — "36 requirements: 28 done · 4 partial · …". A
     * hand-written count of a hand-written table, which is the first thing in the catalogue to
     * rot and the last thing anyone would notice.
     */
    @Test
    void theHeadlineTallyMatchesTheMatrix() {
        Headline headline = RequirementCatalogue.statusHeadline()
                .orElseThrow(() -> new AssertionError("docs/requirements/README.md has lost its status headline"));

        var actual = new TreeMap<String, Integer>();
        REQUIREMENTS.values().forEach(requirement ->
                actual.merge(requirement.status(), 1, Integer::sum));

        assertEquals(REQUIREMENTS.size(), headline.total(),
                () -> "the headline counts " + headline.total() + " requirements; the domain files declare "
                        + REQUIREMENTS.size());
        assertEquals(actual, new TreeMap<>(headline.byStatus()),
                "the headline's per-status counts no longer match the requirements themselves");
    }
}
