@echo off
REM Build a native Windows package for ArboNote (.exe installer + app image).
REM Requires: JDK 17+ (with jpackage) and Maven on PATH.

echo == Building ArboNote native Windows package ==
call mvn -B clean package -Ppackage-windows
if errorlevel 1 goto :error

echo.
echo Done. Outputs:
echo   Runnable jar:      target\ArboNote-*-all.jar
echo   App image:         target\installer\ArboNote\   (launcher: target\installer\ArboNote\ArboNote.exe)
echo   .exe installer:    target\installer\ArboNote-*.exe
goto :eof

:error
echo Build failed.
exit /b 1
