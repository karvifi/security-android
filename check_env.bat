@echo off
echo ========================================
echo  FORTRESS HYPERVISOR - ENVIRONMENT CHECK
echo ========================================
echo.

echo Checking Java...
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ Java found
) else (
    echo ✗ Java not found. Install JDK 17+ from https://adoptium.net/
)

echo.
echo Checking Android SDK...
adb version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ Android SDK found
) else (
    echo ✗ Android SDK not found. Install Android Studio.
)

echo.
echo Checking Gradle wrapper...
if exist "gradlew.bat" (
    echo ✓ Gradle wrapper found
) else (
    echo ✗ gradlew.bat not found
)

echo.
echo Checking project files...
set MISSING_FILES=0

if exist "app\build.gradle.kts" (
    echo ✓ app\build.gradle.kts
) else (
    echo ✗ app\build.gradle.kts missing
    set MISSING_FILES=1
)

if exist "app\src\main\AndroidManifest.xml" (
    echo ✓ app\src\main\AndroidManifest.xml
) else (
    echo ✗ app\src\main\AndroidManifest.xml missing
    set MISSING_FILES=1
)

if exist "gradle\wrapper\gradle-wrapper.properties" (
    echo ✓ gradle\wrapper\gradle-wrapper.properties
) else (
    echo ✗ gradle\wrapper\gradle-wrapper.properties missing
    set MISSING_FILES=1
)

if exist "build.gradle.kts" (
    echo ✓ build.gradle.kts
) else (
    echo ✗ build.gradle.kts missing
    set MISSING_FILES=1
)

echo.
echo ========================================
if %MISSING_FILES% equ 0 (
    echo ✓ READY TO BUILD APK
    echo Run: .\build_safe.ps1
) else (
    echo ✗ ENVIRONMENT NOT READY
    echo Fix the issues above first
)
echo ========================================