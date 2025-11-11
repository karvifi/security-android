# PowerShell script to build Fortress Hypervisor (100% Stable Version)

Write-Host "Setting up Java environment for Android build..." -ForegroundColor Yellow
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host ""
Write-Host "Building Fortress Hypervisor (100% Stable Version)..." -ForegroundColor Green
Write-Host ""

# Try to build
& ".\gradlew.bat" clean assembleDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ BUILD SUCCESSFUL!" -ForegroundColor Green
    Write-Host ""
    Write-Host "APK location: app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Install on Pixel 7 Pro using: adb install -r app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Cyan
    Write-Host "Or transfer the APK file to your Pixel 7 Pro and install it manually" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "❌ BUILD FAILED!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Check the error messages above and ensure Android SDK is properly configured." -ForegroundColor Yellow
}