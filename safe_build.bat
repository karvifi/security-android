@echo off
echo ========================================
echo  FORTRESS HYPERVISOR - SAFE APK BUILD
echo ========================================
echo.

echo [1/6] Checking prerequisites...
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ ERROR: Java JDK is not installed!
    echo.
    echo 📥 INSTALLATION REQUIRED:
    echo 1. Download JDK 17+ from: https://adoptium.net/
    echo 2. Install JDK and set JAVA_HOME environment variable
    echo 3. Add JDK bin folder to PATH
    echo.
    echo Example: set JAVA_HOME="C:\Program Files\Java\jdk-17"
    echo Example: set PATH=%PATH%;"C:\Program Files\Java\jdk-17\bin"
    echo.
    pause
    exit /b 1
) else (
    echo ✅ Java found:
    java -version
)

echo.
echo [2/6] Checking Android SDK...
echo.

REM Check if Android SDK exists (common locations)
set ANDROID_SDK_FOUND=0
if exist "%ANDROID_HOME%" (
    set ANDROID_SDK_FOUND=1
    echo ✅ Android SDK found at: %ANDROID_HOME%
) else if exist "%LOCALAPPDATA%\Android\Sdk" (
    set ANDROID_SDK_FOUND=1
    set ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk
    echo ✅ Android SDK found at: %LOCALAPPDATA%\Android\Sdk
) else if exist "C:\Android\Sdk" (
    set ANDROID_SDK_FOUND=1
    set ANDROID_HOME=C:\Android\Sdk
    echo ✅ Android SDK found at: C:\Android\Sdk
) else (
    echo ❌ ERROR: Android SDK not found!
    echo.
    echo 📥 INSTALLATION REQUIRED:
    echo 1. Download Android Studio: https://developer.android.com/studio
    echo 2. Install Android Studio (includes SDK)
    echo 3. Set ANDROID_HOME environment variable
    echo.
    echo Example: set ANDROID_HOME="C:\Users\%USERNAME%\AppData\Local\Android\Sdk"
    echo.
    pause
    exit /b 1
)

echo.
echo [3/6] Checking Android SDK components...
echo.

REM Check for required SDK components
if not exist "%ANDROID_HOME%\platform-tools\adb.exe" (
    echo ❌ ERROR: Android SDK Platform Tools not found!
    echo Please install via Android Studio SDK Manager
    pause
    exit /b 1
) else (
    echo ✅ Platform Tools found
)

if not exist "%ANDROID_HOME%\build-tools" (
    echo ❌ ERROR: Android Build Tools not found!
    echo Please install Build Tools 34.0.0+ via Android Studio SDK Manager
    pause
    exit /b 1
) else (
    echo ✅ Build Tools found
)

if not exist "%ANDROID_HOME%\platforms\android-34" (
    echo ❌ ERROR: Android API 34 Platform not found!
    echo Please install Android 14 (API 34) via Android Studio SDK Manager
    pause
    exit /b 1
) else (
    echo ✅ Android API 34 Platform found
)

echo.
echo [4/6] Checking project integrity...
echo.

REM Check if required files exist
if not exist "app\src\main\AndroidManifest.xml" (
    echo ❌ ERROR: AndroidManifest.xml not found!
    pause
    exit /b 1
) else (
    echo ✅ AndroidManifest.xml found
)

if not exist "app\build.gradle.kts" (
    echo ❌ ERROR: app build.gradle.kts not found!
    pause
    exit /b 1
) else (
    echo ✅ App build.gradle.kts found
)

if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo ❌ ERROR: Gradle wrapper not found!
    pause
    exit /b 1
) else (
    echo ✅ Gradle wrapper found
)

echo.
echo [5/6] Building APK (Debug - SAFE VERSION)...
echo.

REM Clean and build
call gradlew clean
if %errorlevel% neq 0 (
    echo ❌ ERROR: Clean failed!
    pause
    exit /b 1
)

call gradlew assembleDebug
if %errorlevel% neq 0 (
    echo ❌ ERROR: Build failed!
    echo.
    echo 🔧 TROUBLESHOOTING:
    echo 1. Check internet connection for dependencies
    echo 2. Verify all SDK components are installed
    echo 3. Check Java version compatibility
    echo 4. Review build error messages above
    echo.
    pause
    exit /b 1
)

echo.
echo [6/6] Build completed successfully!
echo.

REM Check if APK was created
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo ✅ APK created successfully:
    dir "app\build\outputs\apk\debug\app-debug.apk"
    echo.
    echo 📱 APK Location: %CD%\app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo ========================================
    echo  🚨 IMPORTANT SAFETY NOTICE 🚨
    echo ========================================
    echo.
    echo This APK contains ADVANCED SECURITY FEATURES that may:
    echo • Monitor system activity
    echo • Access device sensors
    echo • Use accessibility services
    echo • Act as device administrator
    echo • Intercept network traffic
    echo.
    echo ⚠️  SAFETY RECOMMENDATIONS:
    echo.
    echo 1. 📱 TEST ON EMULATOR FIRST:
    echo    - Use Android Studio AVD Manager
    echo    - Create Android 14 emulator
    echo    - Install and test APK safely
    echo.
    echo 2. 🔒 BACKUP YOUR DEVICE:
    echo    - Backup important data
    echo    - Note current settings
    echo.
    echo 3. 📋 REQUIRED PERMISSIONS:
    echo    - Grant ALL requested permissions
    echo    - Enable accessibility service
    echo    - Set as device administrator
    echo.
    echo 4. 🆘 EMERGENCY RECOVERY:
    echo    - Know device admin password
    echo    - Have factory reset option ready
    echo    - Keep recovery mode access
    echo.
    echo 5. 📊 MONITORING:
    echo    - Watch battery usage
    echo    - Monitor system performance
    echo    - Check for unusual behavior
    echo.
    echo ========================================
    echo.
    set /p choice="Do you want to install on a connected device? (y/N): "
    if /i "!choice!"=="y" (
        echo.
        echo Installing APK...
        adb install -r "app\build\outputs\apk\debug\app-debug.apk"
        if %errorlevel% equ 0 (
            echo ✅ APK installed successfully!
            echo.
            echo 📋 NEXT STEPS:
            echo 1. Open the Fortress Hypervisor app
            echo 2. Grant all requested permissions
            echo 3. Enable accessibility service in Settings
            echo 4. Set as device administrator
            echo 5. Monitor app behavior
        ) else (
            echo ❌ Installation failed!
            echo Make sure device is connected and USB debugging is enabled
        )
    ) else (
        echo.
        echo 📁 APK saved for manual installation
        echo Location: %CD%\app\build\outputs\apk\debug\app-debug.apk
    )
) else (
    echo ❌ ERROR: APK file not found after build!
    dir app\build\outputs\apk\debug\ 2>nul
    pause
    exit /b 1
)

echo.
echo Build script completed.
pause