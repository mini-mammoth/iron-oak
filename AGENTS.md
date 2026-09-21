# Agent Instructions — Iron Oak (`v26.2`)

This branch is the **`v26.2`** line of Iron Oak. It is **supported**: it has its own CI
and its own releases — unlike an archived branch such as `1.19` or `v1.18.x`, which gets
neither.

## Version facts for this line

Read from this branch's own `gradle.properties`:

- `minecraft_version=26.2`
- `loader_version=0.19.3`
- `fabric_version=0.158.0+26.2`
- `mod_version=1.3.0+26.2`

## Toolchain for this line

- Loom plugin id: `net.fabricmc.fabric-loom` (unobfuscated Minecraft, so no `mappings`
  block and no `-remap` variant).
- Compiles at `options.release = 25` (`build.gradle`).
- `.github/workflows/main.yml` pins CI to JDK 25 for this line — a CI pin, not a Loom requirement.

## Everything else lives on `main`

The product, the requirements, the strategy docs and the orchestration process are not
forked per branch. Read them at:
https://github.com/mini-mammoth/iron-oak/tree/main/docs

**Documentation changes belong on `main`, never on this branch.** If you are about to edit
a doc here, stop — make the change on `main` instead.
