# Agent Instructions — Iron Oak (`v1.21.11`)

This branch is the **`v1.21.11`** line of Iron Oak. It is **supported**: it has its own CI
and its own releases — unlike an archived branch such as `1.19` or `v1.18.x`, which gets
neither.

## Version facts for this line

Read from this branch's own `gradle.properties`:

- `minecraft_version=1.21.11`
- `loader_version=0.19.3`
- `fabric_version=0.141.6+1.21.11`
- `mod_version=1.3.0+1.21.11`

## Toolchain for this line

- Loom plugin id: `net.fabricmc.fabric-loom-remap` (Minecraft is still obfuscated here).
- Mappings: `loom.officialMojangMappings()`.
- Compiles at `options.release = 21` (`build.gradle`).
- `.github/workflows/main.yml` pins CI to JDK 21 for this line — a CI pin, not a Loom requirement.

## Everything else lives on `main`

The product, the requirements, the strategy docs and the orchestration process are not
forked per branch. Read them at:
https://github.com/mini-mammoth/iron-oak/tree/main/docs

**Documentation changes belong on `main`, never on this branch.** If you are about to edit
a doc here, stop — make the change on `main` instead.
