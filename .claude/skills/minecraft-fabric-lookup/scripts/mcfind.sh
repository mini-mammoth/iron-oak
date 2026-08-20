#!/usr/bin/env bash
# Resolve Minecraft API questions against the jar Loom actually put on the classpath.
# No hardcoded paths and no hardcoded version — everything is discovered, so this keeps
# working across Minecraft versions and across the obfuscated/unobfuscated split.
set -uo pipefail

usage() {
  cat <<'EOF'
mcfind.sh — look things up in the mapped Minecraft jar

  mcfind.sh class <SimpleName>...   which package is this class in now?
  mcfind.sh sig <fully.qualified>   exact signatures (javap)
  mcfind.sh pkg <package.prefix>    list classes under a package
  mcfind.sh data <path-prefix>      list data/asset paths inside the jar
  mcfind.sh jar                     print the jar being used

Examples
  mcfind.sh class ResourceLocation Identifier ItemStack
  mcfind.sh sig net.minecraft.world.item.Item\$Properties
  mcfind.sh pkg net.minecraft.world.item.crafting
  mcfind.sh data data/minecraft/tags
EOF
}

mc_version() {
  # Single source of truth for the version, same as the build uses.
  awk -F= '/^minecraft_version=/{gsub(/[ \t\r]/,"",$2); print $2}' gradle.properties 2>/dev/null
}

find_jar() {
  local ver; ver="$(mc_version)"
  [ -z "$ver" ] && { echo "mcfind: no minecraft_version in gradle.properties — run me from the repo root" >&2; return 1; }
  # Project-local Loom cache first, then the shared one. minecraft-merged covers both the
  # remapped line (<=1.21.11) and the -deobf line (26.1+).
  local jar
  jar=$(find .gradle/loom-cache/minecraftMaven "$HOME/.gradle/caches/fabric-loom/minecraftMaven" \
          -type f -name "minecraft-merged*${ver}*.jar" ! -name "*sources*" 2>/dev/null \
        | sort | tail -1)
  if [ -z "$jar" ]; then
    echo "mcfind: no mapped jar for Minecraft $ver." >&2
    echo "        Run './gradlew build' once so Loom sets Minecraft up, then retry." >&2
    return 1
  fi
  printf '%s\n' "$jar"
}

cache_for() {
  # Class list is stable per jar, so cache it next to the jar's basename.
  local jar="$1" cache
  cache="${TMPDIR:-/tmp}/mcfind-$(basename "$jar").classes"
  if [ ! -s "$cache" ]; then
    unzip -Z1 "$jar" 2>/dev/null | grep '\.class$' | sed 's/\.class$//' > "$cache"
  fi
  printf '%s\n' "$cache"
}

[ $# -eq 0 ] && { usage; exit 2; }
cmd="$1"; shift
jar="$(find_jar)" || exit 1

case "$cmd" in
  jar) printf '%s\n' "$jar" ;;

  class)
    [ $# -eq 0 ] && { usage; exit 2; }
    list="$(cache_for "$jar")"
    for n in "$@"; do
      printf '%-34s ' "$n"
      # top-level classes first, then nested (Outer$Nested)
      r=$(grep -E "/${n}$" "$list" | tr '/' '.' | head -3 | paste -sd' ' -)
      [ -z "$r" ] && r=$(grep -E "/[A-Za-z0-9_]+\\\$${n}$" "$list" | tr '/' '.' | head -3 | paste -sd' ' -)
      if [ -z "$r" ]; then
        echo "NOT FOUND — renamed or removed in this version"
      else
        echo "$r"
      fi
    done
    ;;

  sig)
    [ $# -eq 0 ] && { usage; exit 2; }
    for c in "$@"; do
      echo "===== $c ====="
      "${JAVA_HOME:?set JAVA_HOME to the JDK this version needs}/bin/javap" -cp "$jar" "$c" 2>&1
    done
    ;;

  pkg)
    [ $# -eq 0 ] && { usage; exit 2; }
    list="$(cache_for "$jar")"
    for p in "$@"; do
      grep -E "^$(printf '%s' "$p" | tr '.' '/')/[A-Za-z0-9_]+$" "$list" | tr '/' '.' | sort
    done
    ;;

  data)
    [ $# -eq 0 ] && { usage; exit 2; }
    for p in "$@"; do
      unzip -Z1 "$jar" 2>/dev/null | grep "^$p" | sort
    done
    ;;

  *) usage; exit 2 ;;
esac
