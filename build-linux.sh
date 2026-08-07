#!/usr/bin/env bash
# Build native Linux packages for ArboNote:
#   - .deb installer (requires dpkg-deb + fakeroot, e.g. on Debian/Ubuntu; skipped otherwise)
#   - Self-contained app image folder (target/installer/ArboNote/)
#   - Single-file portable AppImage (if appimagetool is available)
# Requires: JDK 17+ (with jpackage) and Maven on PATH.
set -euo pipefail

MVN_ARGS=(-B clean package -Ppackage-linux)

# jpackage can only build a .deb on Debian-based distros (it validates
# Debian packages via "dpkg -s", which fails on e.g. Arch even with dpkg
# installed). Skip the .deb elsewhere; it is always built on the Ubuntu CI.
if [ ! -f /etc/debian_version ]; then
  echo "== Not a Debian-based distro; skipping the .deb installer (portable AppImage still built) =="
  MVN_ARGS+=(-Dskip.deb=true)
fi

echo "== Building ArboNote native Linux package =="
mvn "${MVN_ARGS[@]}"

echo "== Building portable AppImage =="
if [ -n "${APPIMAGE:-}" ]; then
  ./build-appimage.sh
elif command -v appimagetool >/dev/null 2>&1; then
  APPIMAGE="$(command -v appimagetool)" ./build-appimage.sh
else
  echo "appimagetool not found; skipping AppImage."
  echo "Install it (see build-appimage.sh) or just use the app image folder."
fi

echo
echo "Done. Outputs:"
echo "  Runnable jar:       target/ArboNote-*-all.jar"
echo "  App image (Linux):  target/installer/ArboNote/   (launcher: target/installer/ArboNote/bin/ArboNote)"
echo "  .deb installer:     target/installer/ArboNote-*.deb   (only if dpkg-deb is available)"
echo "  Portable AppImage:  target/installer/ArboNote-*.AppImage"
echo
echo "Run the AppImage directly with:  ./target/installer/ArboNote-*.AppImage"
