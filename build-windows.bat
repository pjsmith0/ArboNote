@echo off
REM Build a native Windows package (.msi installer) for FileTreeEditor.
REM Requires: JDK 17+ (with jpackage) and Maven on PATH.
REM Note: the WiX Toolset must also be installed for jpackage to produce an .msi.

echo == Building runnable jar ==
call mvn -B clean package
if errorlevel 1 goto :error

echo == Building native Windows installer (.msi) via jpackage ==
call mvn -B package -Ppackage-windows -DskipTests
if errorlevel 1 goto :error

echo.
echo Done. Outputs:
echo   Runnable jar:      target\FileTreeEditor.jar
echo   Native installer:  target\installer\
echo.
echo Run directly with:  java -jar target\FileTreeEditor.jar
goto :eof

:error
echo Build failed.
exit /b 1
