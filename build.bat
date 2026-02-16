@echo off
REM AlgoLang Native Build Script for Windows
REM This script builds native executables for Windows

setlocal enabledelayedexpansion

echo =========================================
echo   AlgoLang Native Build Script
echo =========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven not found!
    echo.
    echo Please install Maven:
    echo   Download from: https://maven.apache.org/download.cgi
    echo.
    pause
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java not found!
    echo.
    echo Please install GraalVM:
    echo   Download from: https://github.com/graalvm/graalvm-ce-builds/releases
    echo.
    pause
    exit /b 1
)

echo [OK] Maven found
echo [OK] Java found
echo.

REM Show Java version
echo Java version:
java -version
echo.

REM Ask what to build
echo What would you like to build?
echo   1) JAR file (fast, requires Java to run)
echo   2) Native executable (slow build, standalone binary)
echo   3) Both
echo.
set /p choice="Enter choice [1-3]: "

if "%choice%"=="1" (
    echo.
    echo Building JAR...
    call mvn clean package
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Build failed!
        pause
        exit /b 1
    )
    echo.
    echo [OK] JAR built successfully!
    echo   Location: target\algoLang-1.0.0-jar-with-dependencies.jar
    echo   Run with: java -jar target\algoLang-1.0.0-jar-with-dependencies.jar test.al
) else if "%choice%"=="2" (
    echo.
    echo Building native executable...
    echo This will take 2-5 minutes...
    echo.
    echo IMPORTANT: Make sure you're running this from:
    echo   "x64 Native Tools Command Prompt for VS 2022"
    echo.
    pause
    call mvn clean package -Pnative
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Build failed!
        echo.
        echo Make sure you have:
        echo   1. Visual Studio 2022 Build Tools installed
        echo   2. Opened "x64 Native Tools Command Prompt for VS 2022"
        echo.
        pause
        exit /b 1
    )
    echo.
    echo [OK] Native executable built successfully!
    echo   Location: target\algolang.exe
    echo   Run with: target\algolang.exe test.al
) else if "%choice%"=="3" (
    echo.
    echo Building JAR...
    call mvn clean package
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] JAR build failed!
        pause
        exit /b 1
    )
    echo.
    echo Building native executable...
    echo This will take 2-5 minutes...
    echo.
    echo IMPORTANT: Make sure you're running this from:
    echo   "x64 Native Tools Command Prompt for VS 2022"
    echo.
    pause
    call mvn package -Pnative
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Native build failed!
        pause
        exit /b 1
    )
    echo.
    echo [OK] Both built successfully!
    echo   JAR: target\algoLang-1.0.0-jar-with-dependencies.jar
    echo   Native: target\algolang.exe
) else (
    echo Invalid choice!
    pause
    exit /b 1
)

echo.
echo =========================================
echo   Build Complete!
echo =========================================

REM Ask if user wants to test
echo.
set /p test_choice="Would you like to run a test? [y/N]: "

if /i "%test_choice%"=="y" (
    REM Create a test file
    (
        echo Algorithm: test;
        echo Variables:
        echo     name : string;
        echo     age : integer;
        echo Begin:
        echo     name ^<- "AlgoLang";
        echo     age ^<- 1;
        echo     write^("Name: " + name^);
        echo     write^("Age: " + age^);
        echo     write^("Build successful!"^);
        echo End
    ) > test_algolang.al

    echo.
    echo Running test program...
    echo.

    if exist target\algolang.exe (
        target\algolang.exe test_algolang.al --language=1
    ) else if exist target\algoLang-1.0.0-jar-with-dependencies.jar (
        java -jar target\algoLang-1.0.0-jar-with-dependencies.jar test_algolang.al --language=1
    )

    del test_algolang.al
)

echo.
echo Done! 🎉
echo.
pause