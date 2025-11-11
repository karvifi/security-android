@echo off
echo ========================================
echo  FORTRESS HYPERVISOR - ENVIRONMENT CHECK
echo ========================================
echo.

echo Checking Java installation...
java -version 2>nul
if %errorlevel% neq 0 (
    echo ❌ Java NOT found!
    echo Please install JDK 17+ from: https://adoptium.net/
) else (
    echo ✅ Java found
)
echo.

echo Checking Android SDK...
if defined ANDROID_HOME (
    echo ✅ ANDROID_HOME set: %ANDROID_HOME%
    if exist "%ANDROID_HOME%" (
        echo ✅ Android SDK directory exists
    ) else (
        echo ❌ Android SDK directory not found at ANDROID_HOME
    )
) else (
    echo ❌ ANDROID_HOME not set
    echo Common locations to check:
    echo   %LOCALAPPDATA%\Android\Sdk
    echo   C:\Android\Sdk
    echo   C:\Users\%USERNAME%\AppData\Local\Android\Sdk
)
echo.

echo Checking Android tools...
where adb 2>nul >nul
if %errorlevel% neq 0 (
    echo ❌ ADB not found in PATH
) else (
    echo ✅ ADB found
)
echo.

echo Checking project files...
if exist "app\src\main\AndroidManifest.xml" (
    echo ✅ AndroidManifest.xml found
) else (
    echo ❌ AndroidManifest.xml missing
)

if exist "app\build.gradle.kts" (
    echo ✅ App build.gradle.kts found
) else (
    echo ❌ App build.gradle.kts missing
)

if exist "gradlew.bat" (
    echo ✅ Gradle wrapper found
) else (
    echo ❌ Gradle wrapper missing
)
echo.

echo Checking Gradle wrapper...
if exist "gradle\wrapper\gradle-wrapper.jar" (
    echo ✅ Gradle wrapper JAR found
) else (
    echo ❌ Gradle wrapper JAR missing
)
echo.

echo ========================================
echo  RECOMMENDED SETUP STEPS:
echo ========================================
echo.
echo 1. Install JDK 17+:
echo    https://adoptium.net/
echo.
echo 2. Install Android Studio:
echo    https://developer.android.com/studio
echo.
echo 3. Set environment variables:
echo    JAVA_HOME = "C:\Program Files\Java\jdk-17"
echo    ANDROID_HOME = "%%LOCALAPPDATA%%\Android\Sdk"
echo.
echo 4. Add to PATH:
echo    %%JAVA_HOME%%\bin
echo    %%ANDROID_HOME%%\platform-tools
echo    %%ANDROID_HOME%%\tools
echo    %%ANDROID_HOME%%\tools\bin
echo.
echo 5. Install SDK components via Android Studio:
echo    - SDK Platforms: Android 14 (API 34)
echo    - SDK Tools: Build Tools 34.0.0+
echo    - SDK Tools: Platform Tools
echo.
echo ========================================
echo.
pause