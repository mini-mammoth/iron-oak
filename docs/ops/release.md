---
domain: Operations
domain_code: OPS
status: active
last_updated: 2026-08-20
related:
  - ../../AGENTS.md
  - ../../build.gradle
  - ../../.github/workflows/release.yml
---

# Releasing — Modrinth and CurseForge

Publishing is automated. Cutting a release is: bump the version, tag it, write the
release notes, hit publish. The upload happens in CI.

Before this existed, the upload was manual — which is why Modrinth sat on Minecraft
**1.19** while `main` had long moved to 1.20.4. If you find yourself uploading a jar by
hand, something below is broken; fix it rather than working around it.

---

## One-time setup

Two repository secrets, under *Settings → Secrets and variables → Actions*:

| Secret | Where to get it | Scope needed |
|---|---|---|
| `MODRINTH_API_KEY` | https://modrinth.com/settings/pats | *Create versions* **and** *Write versions* |
| `CURSEFORGE_API_KEY` | https://legacy.curseforge.com/account/api-tokens | full token, CurseForge has no scopes |

Neither token is in the repository, and neither is needed to run a dry run. **A missing
token does not fail the build** — it downgrades that one platform to a dry run. That is
deliberate: a wrong token must not be able to publish to one platform and skip the other.

The project ids are already in `gradle.properties` (`modrinth_id`, `curseforge_id`) and
are not secret.

---

## Cutting a release

1. **Bump `mod_version`** in `gradle.properties`. The format is `<mod>+<mc>`, e.g.
   `1.3.0+1.21.11` — bump the Minecraft half in the same commit, never separately.
2. **Check `publish_game_versions`** in `gradle.properties`. This is *not* derived from
   `minecraft_version`: a jar usually loads on more versions than it was built against,
   and the announced range is a judgement call. Comma-separated, no spaces.
3. **Commit and push** to `main`.
4. **Create a GitHub Release** against the new tag. Its **body is the changelog** that
   both platforms will show — Markdown, written for players, not a commit log.
5. **Publish the release.** The `Release` workflow builds and uploads.

A *draft* release triggers nothing. Only *published* does. So a draft is the safe place
to write and review notes.

### Release channel

`publish_type` in `gradle.properties` (`STABLE` | `BETA` | `ALPHA`) is the default. A
manual run can override it without a commit — see below.

---

## Dry run — do this before the first real release

*Actions → Release → Run workflow*, leave **dry run** checked. Nothing is published. The
run does three useful things:

1. **Preflight** — checks both tokens against the live APIs and verifies that every entry
   in `publish_game_versions` actually exists on both platforms. This is the only place
   the tokens get exercised without publishing; a dry run withholds them from Gradle, so
   without this step an expired token would first surface during a real release.
2. Builds the jar.
3. Writes the payloads it *would* have uploaded to `build/publishMods/`, attached to the
   run as an artifact.

Preflight also runs on a real release, before the upload — so a bad game version string
fails the run instead of landing on the project page.

Locally the same thing, since no tokens are set in your shell:

```bash
export JAVA_HOME=~/.sdkman/candidates/java/21.0.3-ms
./gradlew publishMods
```

The console prints the display name, version, changelog and resolved dependencies per
platform. Read them. Note that a local run does **not** include the preflight checks —
those live in the workflow, because they need the tokens.

---

## What is where

| Thing | Lives in |
|---|---|
| Project ids, game versions, release channel | `gradle.properties` |
| `publishMods` configuration | `build.gradle` |
| Trigger, tokens, artifacts | `.github/workflows/release.yml` |
| Plugin docs | https://modmuss50.github.io/mod-publish-plugin/ |

---

## Things that will bite you

- **The Gradle wrapper is 9.5.1 on this line. Do not re-pin it to 8.11.1.**
  That old pin belonged to the 1.20.4 toolchain and both of its bounds are now history:
  - *Not lower:* the publish plugin needs `ConfigurableFileCollection.convention`, which
    does not exist before Gradle 8.11. Every plugin version — 0.8.4 through 2.2.0 —
    fails on an older wrapper with `Could not create domain object 'modrinth'`.
  - *Not higher:* on Gradle **8.12**, **Loom 1.5** produced an **empty 261-byte jar and
    still reported `BUILD SUCCESSFUL`**. Nothing in the log hinted at it.

  Both bounds were properties of *Loom 1.5*. This line runs **Loom 1.17**, which requires
  Gradle 9.x — the 8.11.1 pin does not merely stop being necessary, it stops working.

  What survives the toolchain change is the rule the pin existed to enforce: when you
  touch the wrapper, Loom or Gradle, **`BUILD SUCCESSFUL` is not the check**. The check is
  the artefact:

  ```bash
  ./gradlew clean build
  # Not iron-oak-*.jar: that glob also matches the sources jar, and unzip -l over two
  # archives prints "0 files" — which reads like the empty-jar bug you are checking for.
  unzip -l "$(find build/libs -name '*.jar' ! -name '*-sources.jar')" | tail -1
  ```

  Expect **hundreds** of files. The number grows with every arm of the matrix, so it is the
  order of magnitude that carries the signal — the bug shipped a jar of **2**.
- **Neither API is idempotent.** Uploading the same version twice creates two files.
  The workflow has a `concurrency` group with `cancel-in-progress: false` for that
  reason; do not "fix" it into cancelling.
- **CurseForge needs `client` or `server` set.** A release declaring neither is rejected
  by their API.
- **Version branches.** This workflow runs on published releases regardless of branch. A
  release cut from `v1.18.x` publishes with that branch's `gradle.properties`, which is
  what you want — but check `publish_game_versions` there too.
- **`curseforge_id` empty disables CurseForge** rather than failing. Handy while a
  project does not exist yet, easy to overlook when wondering where the upload went.

---

## Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-20 | 1.0 | Initial version. Automated publishing via `mod-publish-plugin` 2.2.0 (#22). |
| 2026-08-21 | 1.1 | The jar check no longer names a file count (#47). "~347" had drifted to 395 while sitting in three documents; the empty-jar bug shipped 2 files, so the order of magnitude is the whole signal. The command is also fixed to exclude the sources jar. |

*Last updated: 2026-08-21*
