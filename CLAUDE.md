# CLAUDE.md

Read [`AGENTS.md`](AGENTS.md) — it is the single source of truth for working in this
repository, and it applies to you unchanged.

@AGENTS.md

## Before your first Gradle command

Fabric Loom does not run on JDK 22+, and this machine defaults to JDK 25. Every Gradle
invocation needs JDK 21:

```bash
export JAVA_HOME=~/.sdkman/candidates/java/21.0.3-ms
```

A build that fails without this looks like a broken project, not a wrong JDK. See the
JDK section of `AGENTS.md`.
