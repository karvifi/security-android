# Simple APK Build Script
Write-Host "=========================================" -ForegroundColor Blue
Write-Host " BUILDING FORTRESS HYPERVISOR APK" -ForegroundColor Blue
Write-Host "=========================================" -ForegroundColor Blue
Write-Host ""

# Clean and build
Write-Host "Cleaning project..." -ForegroundColor White
.\gradlew.bat clean

Write-Host "Building debug APK..." -ForegroundColor White
.\gradlew.bat assembleDebug

# Check results
Write-Host "Checking build results..." -ForegroundColor White
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    $fileSize = (Get-Item $apkPath).Length / 1MB
    Write-Host "✓ APK built successfully!" -ForegroundColor Green
    Write-Host "  Location: $apkPath" -ForegroundColor White
    Write-Host "  Size: $([math]::Round($fileSize, 2)) MB" -ForegroundColor White
} else {
    Write-Host "✗ APK build failed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Green
Write-Host " APK BUILD COMPLETED SUCCESSFULLY!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green