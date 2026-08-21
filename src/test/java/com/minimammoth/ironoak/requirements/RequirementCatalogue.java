package com.minimammoth.ironoak.requirements;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * {@code docs/requirements/} and the {@code @Requirement} citations in both test source sets,
 * read as data.
 *
 * <p>Read off the working tree, not off the classpath — unlike {@link
 * com.minimammoth.ironoak.Resources}, which reads the mod's own resources the way the game
 * does. Neither the requirement documents nor the test sources ship in the jar, so the
 * classpath cannot see them, and the working tree is the only place they exist.
 *
 * <p>Citations are read out of the <em>source text</em> rather than reflectively. That is not
 * laziness: {@code @Requirement} is declared {@code RetentionPolicy.SOURCE} because a
 * reflective reader would need the layer-2 classes on the layer-1 classpath while layer 2
 * already needs the annotation, and there is no way to arrange those two source sets so that
 * holds. Text is also the only reader that sees both layers without running either.
 */
final class RequirementCatalogue {

    /** The gates {@code docs/requirements/README.md} defines. Anything else is a typo. */
    static final Set<String> KNOWN_GATES =
            Set.of("build", "runDatagen", "runClient", "inspect", "test", "gametest");

    /** The two gates this test traces. The other four are walked by a human. */
    static final Set<String> TRACED_GATES = Set.of("test", "gametest");

    private static final Pattern INDEX_ROW = Pattern.compile(
            "^\\| \\[(?<id>[A-Z]{3}-\\d{2})]\\((?<file>[a-z]+\\.md)#[^)]*\\) \\| (?<title>[^|]+?) \\| (?<status>[^|]+?) \\| (?<issue>[^|]*?) \\|$");
    private static final Pattern HEADING = Pattern.compile("^### (?<id>[A-Z]{3}-\\d{2}): (?<title>.+)$");
    private static final Pattern STATUS = Pattern.compile("^\\*\\*Status:\\*\\* (?<status>[^·]+?)(?: ·.*)?$");
    private static final Pattern CRITERIA = Pattern.compile("^\\*\\*Acceptance criteria\\*\\* \\(verify: (?<gates>[^)]*)\\).*$");
    private static final Pattern GATE = Pattern.compile("`([^`]+)`");
    private static final Pattern CITATION = Pattern.compile("@Requirement\\(\"(?<id>[^\"]*)\"\\)");
    private static final Pattern HEADLINE = Pattern.compile(
            "^(?<total>\\d+) requirements: \\*\\*(?<breakdown>.+?)\\*\\*$");
    private static final Pattern TALLY = Pattern.compile("(?<count>\\d+) (?<status>[a-z]+)");

    private RequirementCatalogue() {
    }

    /**
     * One requirement, as its domain file states it.
     *
     * <p>Named {@code Entry} rather than {@code Requirement} for one reason: the annotation in
     * this same package is {@code Requirement}, and two types with that simple name here would
     * make every reference to either of them a puzzle.
     */
    record Entry(String id, String file, String title, String status, Set<String> gates) {

        /** The gates this test can check — {@code test}, {@code gametest}, or neither. */
        Set<String> tracedGates() {
            return gates.stream().filter(TRACED_GATES::contains).collect(java.util.stream.Collectors.toUnmodifiableSet());
        }

        @Override
        public String toString() {
            return id;
        }
    }

    /** One {@code @Requirement} citation, and where it was written. */
    record Citation(String id, String layer, Path source) {

        @Override
        public String toString() {
            return id + " in " + source.getFileName();
        }
    }

    /** A row of the index's status matrix, which must agree with the domain file. */
    record IndexRow(String id, String file, String title, String status) {

        @Override
        public String toString() {
            return id;
        }
    }

    /** The index's headline tally: a total, and a count per status. */
    record Headline(int total, Map<String, Integer> byStatus) {
    }

    /** The repo root, found by walking up until {@code docs/requirements/} is under foot. */
    static Path repoRoot() {
        for (var candidate = Path.of("").toAbsolutePath(); candidate != null; candidate = candidate.getParent()) {
            if (Files.isDirectory(candidate.resolve("docs/requirements"))) {
                return candidate;
            }
        }
        throw new IllegalStateException("no docs/requirements above " + Path.of("").toAbsolutePath()
                + " — this test reads the working tree, so it needs the repo to be it");
    }

    /** Every requirement in every domain file, keyed by id, in file order. */
    static Map<String, Entry> requirements() {
        var byId = new LinkedHashMap<String, Entry>();

        for (var file : domainFiles()) {
            String name = file.getFileName().toString();
            String id = null;
            String title = null;
            String status = null;

            for (var line : lines(file)) {
                var heading = HEADING.matcher(line);
                if (heading.matches()) {
                    // A requirement with no acceptance criteria never reaches the flush below,
                    // so nothing is lost by flushing on the next heading instead of at it.
                    id = heading.group("id");
                    title = heading.group("title");
                    status = null;
                    continue;
                }
                if (id == null) {
                    continue;
                }

                var statusLine = STATUS.matcher(line);
                if (statusLine.matches()) {
                    status = statusLine.group("status").trim();
                    continue;
                }

                var criteria = CRITERIA.matcher(line);
                if (criteria.matches()) {
                    var gates = new LinkedHashSet<String>();
                    var gate = GATE.matcher(criteria.group("gates"));
                    while (gate.find()) {
                        gates.add(gate.group(1));
                    }
                    var previous = byId.put(id, new Entry(id, name, title.trim(), plain(status), gates));
                    if (previous != null) {
                        throw new IllegalStateException(id + " is declared twice: " + previous.file() + " and " + name);
                    }
                    id = null;
                }
            }
        }
        return byId;
    }

    /** The index's status matrix, as rows. */
    static List<IndexRow> indexRows() {
        var rows = new ArrayList<IndexRow>();
        for (var line : lines(repoRoot().resolve("docs/requirements/README.md"))) {
            var row = INDEX_ROW.matcher(line);
            if (row.matches()) {
                rows.add(new IndexRow(row.group("id"), row.group("file"),
                        row.group("title").trim(), plain(row.group("status"))));
            }
        }
        return rows;
    }

    /**
     * The index's {@code "36 requirements: **28 done · 4 partial · …**"} headline.
     *
     * <p>A hand-maintained tally of a hand-maintained table, one screen above the table it
     * counts. It is the first thing in the catalogue to go stale and the last thing anyone
     * would notice, which is exactly why it is worth a test.
     */
    static Optional<Headline> statusHeadline() {
        return lines(repoRoot().resolve("docs/requirements/README.md")).stream()
                .map(HEADLINE::matcher)
                .filter(Matcher::matches)
                .findFirst()
                .map(headline -> {
                    var byStatus = new LinkedHashMap<String, Integer>();
                    var tally = TALLY.matcher(headline.group("breakdown"));
                    while (tally.find()) {
                        byStatus.put(tally.group("status"), Integer.parseInt(tally.group("count")));
                    }
                    return new Headline(Integer.parseInt(headline.group("total")), byStatus);
                });
    }

    /** Every {@code @Requirement} citation in both test source sets. */
    static List<Citation> citations() {
        var citations = new ArrayList<Citation>();
        for (var layer : List.of("test", "gametest")) {
            var root = repoRoot().resolve("src/" + layer + "/java");
            try (var java = Files.walk(root).filter(p -> p.toString().endsWith(".java"))) {
                for (var source : java.toList()) {
                    for (var line : lines(source)) {
                        var citation = CITATION.matcher(line);
                        while (citation.find()) {
                            citations.add(new Citation(citation.group("id"), layer, source));
                        }
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException("cannot walk " + root, e);
            }
        }
        return citations;
    }

    private static List<Path> domainFiles() {
        try (Stream<Path> files = Files.list(repoRoot().resolve("docs/requirements"))) {
            return files.filter(p -> p.toString().endsWith(".md"))
                    .filter(p -> !p.getFileName().toString().equals("README.md"))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("cannot list docs/requirements", e);
        }
    }

    private static List<String> lines(Path file) {
        try {
            return Files.readAllLines(file);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + file, e);
        }
    }

    /**
     * A status reduced to the one word the vocabulary defines.
     *
     * <p>The same status is written three ways across the catalogue: plain ({@code done}),
     * emphasised where the index wants it to stand out ({@code **broken**}), flagged as
     * provisional ({@code wontfix*} in the index against {@code wontfix (provisional)} in the
     * domain file). All five statuses are single words, so the first one is the fact and the
     * rest is presentation.
     */
    private static String plain(String markdown) {
        if (markdown == null) {
            return null;
        }
        var stripped = markdown.replace("*", "").replace("~", "").trim();
        int space = stripped.indexOf(' ');
        return space < 0 ? stripped : stripped.substring(0, space);
    }
}
