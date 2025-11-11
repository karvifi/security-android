param(
    [switch]$Build,
    [switch]$Install,
    [switch]$CheckOnly
)

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  FORTRESS HYPERVISOR - SAFE BUILD TOOL" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Function to check prerequisites
function Test-Prerequisites {
    Write-Host "[1/4] Checking Prerequisites..." -ForegroundColor Yellow
    Write-Host ""

    $prerequisites = @()
    $allGood = $true

    # Check Java
    $java = Get-Command java -ErrorAction SilentlyContinue
    if ($java) {
        Write-Host "✅ Java found: $($java.Source)" -ForegroundColor Green
        $javaVersion = & java -version 2>&1 | Select-String -Pattern "version" | Select-Object -First 1
        Write-Host "   Version: $javaVersion" -ForegroundColor Gray
    } else {
        Write-Host "❌ Java NOT found!" -ForegroundColor Red
        Write-Host "   Download JDK 17+ from: https://adoptium.net/" -ForegroundColor Yellow
        $allGood = $false
    }
    Write-Host ""

    # Check Android SDK
    $androidHome = $env:ANDROID_HOME
    if (-not $androidHome) {
        # Try common locations
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

        # Check platform tools
        if (Test-Path "$androidHome\platform-tools\adb.exe") {
            Write-Host "✅ ADB found" -ForegroundColor Green
        } else {
            Write-Host "❌ ADB not found in platform-tools" -ForegroundColor Red
            $allGood = $false
        }

        # Check build tools
        if (Test-Path "$androidHome\build-tools") {
            Write-Host "✅ Build Tools found" -ForegroundColor Green
        } else {
            Write-Host "❌ Build Tools not found" -ForegroundColor Red
            $allGood = $false
        }

        # Check platform
        if (Test-Path "$androidHome\platforms\android-34") {
            Write-Host "✅ Android API 34 found" -ForegroundColor Green
        } else {
            Write-Host "❌ Android API 34 not found" -ForegroundColor Red
            Write-Host "   Install via Android Studio SDK Manager" -ForegroundColor Yellow
            $allGood = $false
        }
    } else {
        Write-Host "❌ Android SDK not found!" -ForegroundColor Red
        Write-Host "   Install Android Studio: https://developer.android.com/studio" -ForegroundColor Yellow
        $allGood = $false
    }
    Write-Host ""

    # Check project files
    Write-Host "Checking project files..." -ForegroundColor Gray
    $projectFiles = @(
        "app\src\main\AndroidManifest.xml",
        "app\build.gradle.kts",
        "gradlew.bat",
        "gradle\wrapper\gradle-wrapper.jar"
    )

    foreach ($file in $projectFiles) {
        if (Test-Path $file) {
            Write-Host "✅ $file" -ForegroundColor Green
        } else {
            Write-Host "❌ $file missing" -ForegroundColor Red
            $allGood = $false
        }
    }
    Write-Host ""

    return $allGood
}

# Function to build APK
function Build-APK {
    Write-Host "[2/4] Building APK..." -ForegroundColor Yellow
    Write-Host ""

    try {
        Write-Host "Cleaning project..." -ForegroundColor Gray
        $cleanResult = & .\gradlew.bat clean 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ Clean failed!" -ForegroundColor Red
            Write-Host $cleanResult -ForegroundColor Red
            return $false
        }

        Write-Host "Building debug APK..." -ForegroundColor Gray
        $buildResult = & .\gradlew.bat assembleDebug 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ Build failed!" -ForegroundColor Red
            Write-Host $buildResult -ForegroundColor Red
            return $false
        }

        $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
        if (Test-Path $apkPath) {
            Write-Host "✅ APK built successfully!" -ForegroundColor Green
            Write-Host "   Location: $(Resolve-Path $apkPath)" -ForegroundColor Cyan
            $apkInfo = Get-Item $apkPath
            Write-Host "   Size: $([math]::Round($apkInfo.Length / 1MB, 2)) MB" -ForegroundColor Cyan
            return $true
        } else {
            Write-Host "❌ APK file not found after build!" -ForegroundColor Red
            return $false
        }

    } catch {
        Write-Host "❌ Build error: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Function to install APK
function Install-APK {
    Write-Host "[3/4] Installing APK..." -ForegroundColor Yellow
    Write-Host ""

    $apkPath = "app\build\outputs\apk\debug\app-debug.apk"

    if (-not (Test-Path $apkPath)) {
        Write-Host "❌ APK not found: $apkPath" -ForegroundColor Red
        return $false
    }

    try {
        Write-Host "Checking for connected devices..." -ForegroundColor Gray
        $devices = & adb devices 2>&1 | Where-Object { $_ -match '\tdevice$' }
        if (-not $devices) {
            Write-Host "❌ No devices connected!" -ForegroundColor Red
            Write-Host "   Connect device with USB debugging enabled" -ForegroundColor Yellow
            Write-Host "   Enable Developer Options → USB Debugging" -ForegroundColor Yellow
            return $false
        }

        Write-Host "Found devices:" -ForegroundColor Green
        $devices | ForEach-Object { Write-Host "   $_" -ForegroundColor Cyan }

        Write-Host ""
        $install = Read-Host "Install APK on connected device? (y/N)"
        if ($install -ne 'y' -and $install -ne 'Y') {
            Write-Host "Installation cancelled." -ForegroundColor Yellow
            return $false
        }

        Write-Host "Installing APK..." -ForegroundColor Gray
        $installResult = & adb install -r $apkPath 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ APK installed successfully!" -ForegroundColor Green
            Write-Host ""
            Write-Host "📋 NEXT STEPS:" -ForegroundColor Cyan
            Write-Host "1. Open the Fortress Hypervisor app" -ForegroundColor White
            Write-Host "2. Grant ALL requested permissions" -ForegroundColor White
            Write-Host "3. Enable accessibility service in Settings" -ForegroundColor White
            Write-Host "4. Set as device administrator" -ForegroundColor White
            Write-Host "5. Monitor app behavior closely" -ForegroundColor White
            return $true
        } else {
            Write-Host "❌ Installation failed!" -ForegroundColor Red
            Write-Host $installResult -ForegroundColor Red
            return $false
        }

    } catch {
        Write-Host "❌ Install error: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Function to show safety warnings
function Show-SafetyWarnings {
    Write-Host "[4/4] Safety Information" -ForegroundColor Yellow
    Write-Host ""

    Write-Host "🚨 CRITICAL SAFETY NOTICE 🚨" -ForegroundColor Red
    Write-Host ""
    Write-Host "This APK contains ADVANCED SECURITY FEATURES that may:" -ForegroundColor Yellow
    Write-Host "• Monitor system activity continuously" -ForegroundColor White
    Write-Host "• Access device sensors and hardware" -ForegroundColor White
    Write-Host "• Use accessibility services for monitoring" -ForegroundColor White
    Write-Host "• Act as device administrator" -ForegroundColor White
    Write-Host "• Intercept network traffic via VPN" -ForegroundColor White
    Write-Host ""

    Write-Host "⚠️  SAFETY RECOMMENDATIONS:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "1. 📱 TEST ON EMULATOR FIRST:" -ForegroundColor Cyan
    Write-Host "   - Use Android Studio AVD Manager" -ForegroundColor White
    Write-Host "   - Create Android 14 emulator (API 34)" -ForegroundColor White
    Write-Host "   - Install and test APK safely" -ForegroundColor White
    Write-Host ""

    Write-Host "2. 🔒 BACKUP YOUR DEVICE:" -ForegroundColor Cyan
    Write-Host "   - Backup important data" -ForegroundColor White
    Write-Host "   - Note current settings" -ForegroundColor White
    Write-Host ""

    Write-Host "3. 📋 REQUIRED PERMISSIONS:" -ForegroundColor Cyan
    Write-Host "   - Grant ALL requested permissions" -ForegroundColor White
    Write-Host "   - Enable accessibility service" -ForegroundColor White
    Write-Host "   - Set as device administrator" -ForegroundColor White
    Write-Host ""

    Write-Host "4. 🆘 EMERGENCY RECOVERY:" -ForegroundColor Cyan
    Write-Host "   - Know device admin password" -ForegroundColor White
    Write-Host "   - Have factory reset option ready" -ForegroundColor White
    Write-Host "   - Keep recovery mode access" -ForegroundColor White
    Write-Host ""

    Write-Host "5. 📊 MONITORING:" -ForegroundColor Cyan
    Write-Host "   - Watch battery usage closely" -ForegroundColor White
    Write-Host "   - Monitor system performance" -ForegroundColor White
    Write-Host "   - Check for unusual behavior" -ForegroundColor White
    Write-Host ""

    Write-Host "❓ CONTINUE?" -ForegroundColor Red
    $confirm = Read-Host "Do you understand these risks and want to proceed? (yes/no)"
    if ($confirm -ne 'yes') {
        Write-Host "Operation cancelled for safety." -ForegroundColor Yellow
        exit 1
    }
}

# Main execution
try {
    if ($CheckOnly) {
        $prereqCheck = Test-Prerequisites
        if (-not $prereqCheck) {
            Write-Host ""
            Write-Host "❌ Prerequisites not met. Please install missing components." -ForegroundColor Red
            exit 1
        } else {
            Write-Host "✅ All prerequisites met!" -ForegroundColor Green
            exit 0
        }
    }

    Show-SafetyWarnings

    $prereqCheck = Test-Prerequisites
    if (-not $prereqCheck) {
        Write-Host ""
        Write-Host "❌ Prerequisites not met. Cannot proceed with build." -ForegroundColor Red
        exit 1
    }

    if ($Build -or -not $Install) {
        $buildSuccess = Build-APK
        if (-not $buildSuccess) {
            Write-Host ""
            Write-Host "❌ Build failed. Check error messages above." -ForegroundColor Red
            exit 1
        }
    }

    if ($Install) {
        $installSuccess = Install-APK
        if (-not $installSuccess) {
            Write-Host ""
            Write-Host "❌ Installation failed or cancelled." -ForegroundColor Red
            exit 1
        }
    }

    Write-Host ""
    Write-Host "=========================================" -ForegroundColor Green
    Write-Host "  OPERATION COMPLETED SUCCESSFULLY!" -ForegroundColor Green
    Write-Host "=========================================" -ForegroundColor Green

} catch {
    Write-Host ""
    Write-Host "❌ ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Stack Trace:" -ForegroundColor Red
    Write-Host $_.ScriptStackTrace -ForegroundColor Red
    exit 1
}