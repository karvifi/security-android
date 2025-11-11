# Fortress Hypervisor - Environment Verification Script
# Version: 1.0.0
# Description: Quick environment check for APK building

Write-Host "=========================================" -ForegroundColor Blue
Write-Host " FORTRESS HYPERVISOR - ENVIRONMENT CHECK" -ForegroundColor Blue
Write-Host "=========================================" -ForegroundColor Blue
Write-Host ""

# Check Java
Write-Host "Checking Java..." -ForegroundColor White
try {
    $javaVersion = java -version 2>&1 | Select-String -Pattern "version" | Select-Object -First 1
    Write-Host "✓ Java found: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Java not found. Install JDK 17+ from https://adoptium.net/" -ForegroundColor Red
}

# Check Android SDK
Write-Host "Checking Android SDK..." -ForegroundColor White
try {
    $adbVersion = adb version 2>&1 | Select-String -Pattern "Android Debug Bridge" | Select-Object -First 1
    Write-Host "✓ ADB found: $adbVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Android SDK not found. Install Android Studio." -ForegroundColor Red
}

# Check Gradle wrapper
Write-Host "Checking Gradle wrapper..." -ForegroundColor White
if (Test-Path "gradlew.bat") {
    try {
        $gradleVersion = .\gradlew.bat --version 2>&1 | Select-String -Pattern "Gradle" | Select-Object -First 1
        Write-Host "✓ Gradle wrapper: $gradleVersion" -ForegroundColor Green
    } catch {
        Write-Host "✗ Gradle wrapper not working" -ForegroundColor Red
    }
} else {
    Write-Host "✗ gradlew.bat not found" -ForegroundColor Red
}

# Check project files
Write-Host "Checking project files..." -ForegroundColor White
$requiredFiles = @(
    "app\build.gradle",
    "app\src\main\AndroidManifest.xml",
    "gradle\wrapper\gradle-wrapper.properties"
)

$allFilesPresent = $true
foreach ($file in $requiredFiles) {
    if (Test-Path $file) {
        Write-Host "✓ $file" -ForegroundColor Green
    } else {
        Write-Host "✗ $file missing" -ForegroundColor Red
        $allFilesPresent = $false
    }
}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Blue
if ($allFilesPresent) {
    Write-Host "✓ READY TO BUILD APK" -ForegroundColor Green
    Write-Host "Run: .\build_safe.ps1" -ForegroundColor White
} else {
    Write-Host "✗ ENVIRONMENT NOT READY" -ForegroundColor Red
    Write-Host "Fix the issues above first" -ForegroundColor Red
}
Write-Host "=========================================" -ForegroundColor Blue