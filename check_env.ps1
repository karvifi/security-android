Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  FORTRESS HYPERVISOR - ENVIRONMENT CHECK" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[1/3] Checking Java..." -ForegroundColor Yellow
$java = Get-Command java -ErrorAction SilentlyContinue
if ($java) {
    Write-Host "✅ Java found: $($java.Source)" -ForegroundColor Green
    try {
        $javaVersion = & java -version 2>&1 | Select-String -Pattern "version" | Select-Object -First 1
        Write-Host "   Version: $javaVersion" -ForegroundColor Gray
    } catch {
        Write-Host "   Could not determine version" -ForegroundColor Yellow
    }
} else {
    Write-Host "❌ Java NOT found!" -ForegroundColor Red
    Write-Host "   Download JDK 17+ from: https://adoptium.net/" -ForegroundColor Yellow
}
Write-Host ""

Write-Host "[2/3] Checking Android SDK..." -ForegroundColor Yellow
$androidHome = $env:ANDROID_HOME
if (-not $androidHome) {
    $commonPaths = @(
        "$env:LOCALAPPDATA\Android\Sdk",
        "C:\Android\Sdk",
        "${env:ProgramFiles(x86)}\Android\android-sdk"
    )

    foreach ($path in $commonPaths) {
        if (Test-Path $path) {
            $androidHome = $path
            break
        }
    }
}

if ($androidHome -and (Test-Path $androidHome)) {
    Write-Host "✅ Android SDK found: $androidHome" -ForegroundColor Green

    if (Test-Path "$androidHome\platform-tools\adb.exe") {
        Write-Host "✅ ADB found" -ForegroundColor Green
    } else {
        Write-Host "❌ ADB not found" -ForegroundColor Red
    }

    if (Test-Path "$androidHome\build-tools") {
        Write-Host "✅ Build Tools found" -ForegroundColor Green
    } else {
        Write-Host "❌ Build Tools not found" -ForegroundColor Red
    }

    if (Test-Path "$androidHome\platforms\android-34") {
        Write-Host "✅ Android API 34 found" -ForegroundColor Green
    } else {
        Write-Host "❌ Android API 34 not found" -ForegroundColor Red
    }
} else {
    Write-Host "❌ Android SDK not found!" -ForegroundColor Red
    Write-Host "   Install Android Studio: https://developer.android.com/studio" -ForegroundColor Yellow
}
Write-Host ""

Write-Host "[3/3] Checking project files..." -ForegroundColor Yellow
$projectFiles = @(
    "app\src\main\AndroidManifest.xml",
    "app\build.gradle.kts",
    "gradlew.bat",
    "gradle\wrapper\gradle-wrapper.jar"
)

$allFilesPresent = $true
foreach ($file in $projectFiles) {
    if (Test-Path $file) {
        Write-Host "✅ $file" -ForegroundColor Green
    } else {
        Write-Host "❌ $file missing" -ForegroundColor Red
        $allFilesPresent = $false
    }
}
Write-Host ""

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  SETUP INSTRUCTIONS" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Install JDK 17+:" -ForegroundColor Yellow
Write-Host "   https://adoptium.net/" -ForegroundColor White
Write-Host ""
Write-Host "2. Install Android Studio:" -ForegroundColor Yellow
Write-Host "   https://developer.android.com/studio" -ForegroundColor White
Write-Host ""
Write-Host "3. Set environment variables:" -ForegroundColor Yellow
Write-Host "   JAVA_HOME = C:\Program Files\Java\jdk-17" -ForegroundColor White
Write-Host "   ANDROID_HOME = %LOCALAPPDATA%\Android\Sdk" -ForegroundColor White
Write-Host ""
Write-Host "4. Install SDK components via Android Studio:" -ForegroundColor Yellow
Write-Host "   - SDK Platforms: Android 14 (API 34)" -ForegroundColor White
Write-Host "   - SDK Tools: Build Tools 34.0.0+" -ForegroundColor White
Write-Host "   - SDK Tools: Platform Tools" -ForegroundColor White
Write-Host ""
Write-Host "5. After setup, run this script again to verify" -ForegroundColor Green
Write-Host ""

if ($allFilesPresent -and $java -and $androidHome) {
    Write-Host "✅ Ready to build! Run: .\build_fortress.ps1 -Build" -ForegroundColor Green
} else {
    Write-Host "❌ Setup incomplete. Please install missing components." -ForegroundColor Red
}