@echo off
echo ========================================
echo  JAVA JDK INSTALLATION HELPER
echo ========================================
echo.

echo Checking if Java is already installed...
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ Java is already installed
    java -version
    goto :end
)

echo.
echo Java not found. Opening download page...
echo.
echo MANUAL INSTALLATION REQUIRED:
echo =============================
echo 1. Go to: https://adoptium.net/temurin/releases/?version=17
echo 2. Download: Windows x64 JDK
echo 3. Install with default settings
echo 4. Set environment variables:
echo    - JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-17.x.x.x
echo    - Add to PATH: %%JAVA_HOME%%\bin
echo.
echo Press any key after installation is complete...
pause >nul

echo.
echo Verifying installation...
java -version
if %errorlevel% equ 0 (
    echo ✓ Java installation verified!
) else (
    echo ✗ Java installation failed. Please try again.
    goto :end
)

:end
echo.
echo ========================================
echo Installation check complete.
echo ========================================