# FileTreeEditor

A JavaFX desktop app with:
- **Left panel** – a `TreeView<File>` browsing the local filesystem (lazily loaded, starts at your home directory; use "Open Folder…" to change the root).
- **Right panel** – a rich-text editor (`HTMLEditor`) with a full formatting toolbar: bold/italic/underline, font & color, alignment, bullet/numbered lists, indent, and hyperlinks.

Double-click a file in the tree (`.txt`, `.html`, `.htm`, `.md`, `.log`, `.xml`, `.css`, `.csv`) to load it into the editor. Use **Save** / **Save As…** to write the editor's HTML content back to disk.

## Requirements

- JDK **17** or newer (JDK 17+ ships `jpackage`, used for native installers)
- Maven **3.8+**
- Internet access on first build (Maven needs to download the JavaFX artifacts and plugins)
- Windows only, for building the `.msi`: [WiX Toolset](https://wixtoolset.org/) v3

## Run during development

```bash
mvn javafx:run
```

## Build a runnable jar (any OS)

```bash
mvn clean package
java -jar target/FileTreeEditor.jar
```

## Build a native installer

jpackage always builds for the OS it's *running on* — there is no cross-compiling a Windows `.exe`/`.msi` from Linux or vice versa. Two Maven profiles and two convenience scripts are provided, one per platform:

**Linux** (produces a `.deb` under `target/installer/`):
```bash
./build-linux.sh
# or directly:
mvn clean package -Ppackage-linux
```

**Windows** (produces a `.msi` under `target\installer\`):
```bat
build-windows.bat
:: or directly:
mvn clean package -Ppackage-windows
```

## Building both automatically (CI)

`.github/workflows/build.yml` runs on GitHub Actions with a matrix of `ubuntu-latest` and `windows-latest`, building both the `.deb` and `.msi` installers on every push to `main` and uploading them as workflow artifacts. This is the recommended way to produce both platform builds from a single trigger without owning both a Linux and Windows machine.

## Project layout

```
pom.xml                          Maven build (JavaFX deps + jpackage profiles)
src/main/java/module-info.java   JavaFX module descriptor
src/main/java/com/fileeditor/app/MainApp.java   Application (tree + editor UI)
src/main/resources/.../styles.css               Tree view styling
build-linux.sh / build-windows.bat               One-shot native build scripts
.github/workflows/build.yml                      CI matrix build for both OSes
```
