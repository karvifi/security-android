@echo off
echo Setting up Java environment for Android build...
set JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"
set PATH=%JAVA_HOME%\bin;%PATH%

echo.
echo Building Fortress Hypervisor (100%% Stable Version)...
echo.

call gradlew.bat clean assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ BUILD SUCCESSFUL!
    echo.
    echo APK location: app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo Install on Pixel 7 Pro using: adb install -r app\build\outputs\apk\debug\app-debug.apk
    echo Or transfer the APK file to your Pixel 7 Pro and install it manually
) else (
    echo.
    echo ❌ BUILD FAILED!
    echo.
    echo Check the error messages above and ensure Android SDK is properly configured.
)