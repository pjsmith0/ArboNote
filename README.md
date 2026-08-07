# ArboNote

A JavaFX desktop note-taking app built around a tree of notes:
- **Left panel** – a `TreeView` of your notes (loaded from `tree.json` in `~/arbonote_data/`). Each node is an HTML note saved under `~/arbonote_data/`.
- **Right panel** – a rich-text editor (Quill.js running in `WebView`) with formatting, tables, images, and search.

Notes and their HTML content are stored as plain files on disk under `~/arbonote_data/`, so your data is easy to back up or edit by hand.

## Requirements

- JDK **17** or newer (JDK 17+ ships `jpackage`, used for native packages)
- Maven **3.8+**
- Internet access on first build (Maven downloads JavaFX and plugins)
- To build the Linux `.deb` locally: `fakeroot` and `dpkg` (`sudo apt-get install fakeroot` on Debian/Ubuntu)
- To build the Windows `.exe`: no extra tools (the `.msi` would need WiX; this project produces `.exe` instead)

## Run during development

```bash
mvn javafx:run
```

## Build a runnable jar (any OS)

```bash
mvn clean package
java -jar target/ArboNote-1.0-SNAPSHOT-all.jar
```

## Build a native executable

`jpackage` always builds for the OS it's running on — there is no cross-compiling a Windows `.exe` from Linux or vice versa. Two Maven profiles and two convenience scripts are provided, one per platform. Each produces:

- an **app image** (a portable folder with a native launcher you can run directly), and
- an **installer** (`.deb` on Linux, `.exe` on Windows).

**Linux** (outputs under `target/installer/`):
```bash
./build-linux.sh
# or directly:
mvn clean package -Ppackage-linux
```

**Windows** (outputs under `target\installer\`):
```bat
build-windows.bat
:: or directly:
mvn clean package -Ppackage-windows
```

## Building both automatically (CI)

`.github/workflows/build.yml` runs on GitHub Actions with a matrix of `ubuntu-latest` and `windows-latest`, building the `.deb` and `.exe` (plus portable app images) on every push to `main` and uploading them as workflow artifacts.

Pushing a version tag (e.g. `git tag v1.0.0 && git push origin v1.0.0`) also creates a **GitHub Release** with all four artifacts attached automatically.

## Project layout

```
pom.xml                          Maven build (JavaFX deps + jpackage profiles)
src/main/java/module-info.java   JavaFX module descriptor
src/main/java/com/pjs/App.java   Entry point
src/main/java/com/pjs/ui/…       Tree UI, web editor, dialogs, context menu
src/main/resources/com/pjs/…     Quill.js editor assets, styles
src/main/resources/icons/…       Application icons (arbonote.png)
build-linux.sh / build-windows.bat               One-shot native build scripts
.github/workflows/build.yml      CI matrix build for both OSes + auto-release
```
