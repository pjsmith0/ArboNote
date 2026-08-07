@echo off
REM Build native Windows packages for ArboNote:
REM   - Single-file portable exe (Launch4j; embeds the fat jar, needs Java 25+ installed)
REM   - Self-contained app image folder (bundled JRE, no Java needed)
REM Requires: JDK 17+ (with jpackage) and Maven on PATH.

echo == Building ArboNote native Windows packages ==
call mvn -B clean package -Ppackage-windows
if errorlevel 1 goto :error

echo.
echo Done. Outputs:
echo   Runnable jar:             target\ArboNote-*-all.jar
echo   Portable exe (single file, needs Java 25+):  target\ArboNote-portable.exe
echo   App image (self-contained): target\installer\ArboNote\   (launcher: target\installer\ArboNote\ArboNote.exe)
goto :eof

:error
echo Build failed.
exit /b 1
