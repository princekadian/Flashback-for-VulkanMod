#!/usr/bin/env bash
# Builds build/flashback-for-vulkanmod-<version>.jar. Run from Git Bash on Windows.
# Compile-only inputs (not shipped): Flashback + VulkanMod jars and libraries from a Prism Launcher install.
set -euo pipefail
cd "$(dirname "$0")"

JDK="${JDK:-/c/Program Files/Java/jdk-21.0.12/bin}"
LIBS="${LIBS:-$APPDATA/PrismLauncher/libraries}"
FLASHBACK_JAR="${FLASHBACK_JAR:-../Flashback-0.39.10-for-MC1.21.4.jar}"
VULKANMOD_JAR="${VULKANMOD_JAR:-$APPDATA/PrismLauncher/instances/1.21.4 Vulkan/minecraft/mods/VulkanMod_1.21.4-0.5.6.jar}"
VERSION=$(sed -n 's/.*"version": "\(.*\)".*/\1/p' resources/fabric.mod.json)

w() { cygpath -w "$1"; }
rm -rf build && mkdir -p build/stubs build/deps/flashback build/deps/vulkanmod build/classes

# extract(jar, dest, prefixes...)
extract() {
    python -I -c 'import sys, zipfile
z = zipfile.ZipFile(sys.argv[1])
z.extractall(sys.argv[2], [n for n in z.namelist() if n.startswith(tuple(sys.argv[3:]))])' "$@"
}
extract "$FLASHBACK_JAR" build/deps/flashback com/moulberry/flashback/ imgui/
extract "$VULKANMOD_JAR" build/deps/vulkanmod net/ META-INF/jars/lwjgl-vulkan-3.3.3.jar

CP="$(w "$LIBS/org/joml/joml/1.10.8/joml-1.10.8.jar");$(w "$LIBS/org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3.jar")"
CP="$CP;$(w "$LIBS/net/fabricmc/fabric-loader/0.19.5/fabric-loader-0.19.5.jar")"
CP="$CP;$(w "$LIBS/net/fabricmc/sponge-mixin/0.17.4+mixin.0.8.7/sponge-mixin-0.17.4+mixin.0.8.7.jar")"
CP="$CP;$(w "$LIBS/org/ow2/asm/asm/9.10.1/asm-9.10.1.jar");$(w "$LIBS/org/ow2/asm/asm-tree/9.10.1/asm-tree-9.10.1.jar")"
CP="$CP;$(w build/deps/vulkanmod);$(w build/deps/vulkanmod/META-INF/jars/lwjgl-vulkan-3.3.3.jar)"

# Intermediary-named Minecraft stubs, compile-only
"$JDK/javac" --release 21 -nowarn -d "$(w build/stubs)" -cp "$CP" $(find stubs -name "*.java" | xargs -I{} cygpath -w {})
"$JDK/javac" --release 21 -proc:none -Xlint:all -d "$(w build/classes)" -cp "$(w build/stubs);$(w build/deps/flashback);$CP" \
    $(find src -name "*.java" | xargs -I{} cygpath -w {})

cp -r resources/. build/classes/
JAR="build/flashback-for-vulkanmod-$VERSION.jar"
(cd build/classes && "$JDK/jar" --create --file "../$(basename "$JAR")" .)
echo "Built $JAR"
