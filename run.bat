@echo off

cd /d "%~dp0"

echo Compiling Automatic Timetable Generator...

javac --add-modules jdk.httpserver TimetableServer.java

if errorlevel 1 (
    echo.
    echo Compilation failed.
    pause
    exit /b 1
)

echo.
echo Starting website at http://localhost:8080

java --add-modules jdk.httpserver TimetableServer

pause