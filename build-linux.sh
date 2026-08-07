#!/usr/bin/env bash
# Build a native Linux package for ArboNote (.deb installer + app image).
# Requires: JDK 17+ (with jpackage) and Maven on PATH.
set -euo pipefail

echo "== Building ArboNote native Linux package =="
mvn -B clean package -Ppackage-linux

echo
echo "Done. Outputs:"
echo "  Runnable jar:       target/ArboNote-*-all.jar"
echo "  App image (Linux):  target/installer/ArboNote/   (launcher: target/installer/ArboNote/bin/ArboNote)"
echo "  .deb installer:     target/installer/ArboNote-*.deb"
echo
echo "Run the app image directly with:  target/installer/ArboNote/bin/ArboNote"
