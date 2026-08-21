# CLAUDE.md

Read [`AGENTS.md`](AGENTS.md) — it is the single source of truth for working in this
repository, and it applies to you unchanged.

@AGENTS.md

## Before your first Gradle command

**On the 26.x line:** Fabric Loom requires JDK 25, which is this machine's default.
No setup needed.

**On the 1.21.x line:** Fabric Loom does not run on JDK 22+, so every Gradle invocation
needs JDK 21:

```bash
export JAVA_HOME=~/.sdkman/candidates/java/21.0.3-ms
```

A build that fails without this looks like a broken project, not a wrong JDK. See the
JDK section of `AGENTS.md` for line-specific requirements.
