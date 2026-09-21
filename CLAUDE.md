# CLAUDE.md

Read [`AGENTS.md`](AGENTS.md) — it is the single source of truth for working in this
repository, and it applies to you unchanged.

@AGENTS.md

## Before your first Gradle command

This machine's default JDK is 25, and Fabric Loom 1.17 launches Gradle fine on it for
every line — 26.x and 1.21.x alike. No `JAVA_HOME` export is needed.

What differs per line is the build regime, not the launcher JDK: the 1.21.x lines apply
Loom's `-remap` plugin, need `mappings loom.officialMojangMappings()`, and compile at
`options.release = 21`. See the JDK section of `AGENTS.md` for the details.
