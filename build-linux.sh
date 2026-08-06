#!/usr/bin/env bash
# Build a native Linux package (.deb app image) for FileTreeEditor.
# Requires: JDK 17+ (with jpackage) and Maven on PATH.
set -euo pipefail

echo "== Building runnable jar =="
mvn -B clean package

echo "== Building native Linux installer (.deb) via jpackage =="
mvn -B package -Ppackage-linux -DskipTests

echo
echo "Done. Outputs:"
echo "  Runnable jar:        target/FileTreeEditor.jar"
echo "  Native installer:    target/installer/"
echo
echo "Run directly with:  java -jar target/FileTreeEditor.jar"
